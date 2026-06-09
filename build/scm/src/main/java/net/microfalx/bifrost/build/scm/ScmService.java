package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.stream.Collectors.joining;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.FileUtils.validateDirectoryExists;

@Service
public class ScmService implements InitializingBean {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ResourceService resourceService;

    private final Map<String, Scm> toolsById = new ConcurrentHashMap<>();
    private final Collection<Scm> tools = new CopyOnWriteArraySet<>();

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
                    return ClassUtils.create(tool.getClass());
                } catch (Exception e) {
                    throw new ScmException("Could not create build tool: " + tool.getName(), e);
                }
            }
        }
        String supportedBuildTools = getTools().stream()
                .map(Scm::getName).collect(joining(", "));
        throw new ScmException("A suitable SCM for project '" + directory.getAbsolutePath()
                + "' is not registered or the directory does not contain a project. Supported build tools: " + supportedBuildTools);
    }

    /**
     * Returns a workspace for a give project.
     *
     * @param project the project
     * @return the file
     */
    public File getWorkspace(Project project) {
        Resource resource = resourceService.getPersisted("scm").resolve(project.getId(), Resource.Type.DIRECTORY);
        return validateDirectoryExists(ResourceUtils.toFile(resource));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverTools();
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
