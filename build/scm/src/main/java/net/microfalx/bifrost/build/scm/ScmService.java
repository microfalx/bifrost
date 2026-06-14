package net.microfalx.bifrost.build.scm;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.stream.Collectors.joining;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;

@Slf4j
@Service
public class ScmService implements InitializingBean {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ResourceService resourceService;

    private final Map<String, Scm> toolsById = new ConcurrentHashMap<>();
    private final Collection<Scm> tools = new CopyOnWriteArraySet<>();
    private final Map<String, File> projectWorkspaces = new ConcurrentHashMap<>();
    private final Set<String> workspaceRequests = new CopyOnWriteArraySet<>();

    private Function<File, Project> projectLoader;

    /**
     * Provides a function to load project from directories.
     *
     * @param projectLoader the loader
     */
    public void setProjectLoader(Function<File, Project> projectLoader) {
        this.projectLoader = requireNonNull(projectLoader);
    }

    /**
     * Returns the registered tools.
     *
     * @return a non-null instance
     */
    public Collection<Scm> getTools() {
        List<Scm> commandList = new ArrayList<>(tools);
        commandList.sort(Comparator.comparing(Scm::getName));
        return commandList;
    }

    /**
     * Returns a tool by identifier or name.
     *
     * @param idOrName the identifier or name
     * @return a non-null instance
     */
    public Scm getTool(String idOrName) {
        requireNotEmpty(idOrName);
        Scm command = toolsById.getOrDefault(StringUtils.toIdentifier(idOrName), toolsById.get(idOrName));
        if (command == null) {
            throw new IllegalArgumentException("Unknown build tool: " + idOrName);
        }
        return command;
    }

    /**
     * Returns the SCM tool suitable to handle the given project.
     *
     * @param directory the directory
     * @return a non-null instance
     */
    public Scm detect(File directory) {
        requireNonNull(directory);
        for (Scm tool : getTools()) {
            if (tool.accept(directory)) {
                try {
                    Scm newTool = ClassUtils.create(tool.getClass());
                    newTool.scmService = this;
                    newTool.setWorkingDirectory(directory);
                    return newTool;
                } catch (Exception e) {
                    throw new ScmException("Could not create build tool: " + tool.getName(), e);
                }
            }
        }
        throw new ScmException("A suitable SCM for project '" + directory.getAbsolutePath()
                + "' is not registered or the directory does not contain a project. Supported build tools: " + getSupportedTools());
    }

    /**
     * Returns the SCM tool suitable to handle the given project.
     *
     * @param project the directory
     * @return a non-null instance
     */
    public Scm detect(Project project) {
        requireNonNull(project);
        Project.Repository repository = project.getRepository();
        if (repository.getType() == Project.Repository.Type.UNKNOWN) {
            throw new ScmException("Unknown repository type for project '"
                    + project.getName() + "', uri '" + repository.getUri() + "'");
        }
        if (StringUtils.isEmpty(repository.getUri())) {
            throw new ScmException("No repository found for project '" + project.getName() + "'");
        }
        if (repository.getType() == Project.Repository.Type.GIT) {
            return getTool("git");
        } else {
            throw new ScmException("A suitable SCM for project '" + project.getName() + "' (" + project.getRepository() + ")"
                    + "' is not registered . Supported build tools: " + getSupportedTools());
        }
    }

    /**
     * Loads a project from a directory.
     *
     * @param directory the directory
     * @return a non-null instance
     */
    public Project getProject(File directory) {
        requireNonNull(directory);
        if (projectLoader == null) throw new ScmException("A project loader is required");
        return projectLoader.apply(directory);
    }

    /**
     * Returns a workspace for a give project.
     *
     * @param project the project
     * @return the file
     */
    public File getWorkspace(Project project) {
        requireNonNull(project);
        Resource resource = resourceService.getPersisted("scm").resolve(project.getId(), Resource.Type.DIRECTORY);
        File workspace = validateDirectoryExists(ResourceUtils.toFile(resource));
        if (workspaceRequests.add(project.getId())) cleanupWorkspace(workspace);
        projectWorkspaces.put(project.getId(), workspace);
        return workspace;
    }

    /**
     * Cleans up the state.
     */
    public void cleanup() {
        cleanupWorkspaces();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverTools();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tools", tools)
                .toString();
    }

    private void cleanupWorkspaces() {
        LOGGER.info("Cleanup workspaces");
        for (File workspace : projectWorkspaces.values()) {
            cleanupWorkspace(workspace);
        }
    }

    private void cleanupWorkspace(File workspace) {
        try {
            FileUtils.deleteDirectory(workspace);
        } catch (IOException e) {
            LOGGER.atWarn().setCause(e).log("Failed to cleanup workspace: {}", workspace);
        }
    }

    private String getSupportedTools() {
        return getTools().stream().map(Scm::getName).collect(joining(", "));
    }

    private void discoverTools() {
        String[] beanNames = applicationContext.getBeanNamesForType(Scm.class);
        for (String beanName : beanNames) {
            Scm scm = (Scm) applicationContext.getBean(beanName);
            scm.scmService = this;
            toolsById.put(scm.getName(), scm);
            toolsById.put(scm.getId(), scm);
            tools.add(scm);
        }
    }
}
