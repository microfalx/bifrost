package net.microfalx.bifrost.build;

import com.google.common.base.MoreObjects;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.ScmException;
import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
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

@Service
public class BuildService implements InitializingBean {

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ScmService scmService;

    private final Map<String, BuildTool> toolsById = new ConcurrentHashMap<>();
    private final Collection<BuildTool> tools = new CopyOnWriteArraySet<>();

    /**
     * Returns the registered tools.
     *
     * @return a non-null instance
     */
    public Collection<BuildTool> getTools() {
        List<BuildTool> commandList = new ArrayList<>(tools);
        commandList.sort(Comparator.comparing(BuildTool::getName));
        return commandList;
    }

    /**
     * Returns a tool by identifier or name.
     *
     * @param idOrName the identifier or name
     * @return a non-null instance
     */
    public BuildTool getTool(String idOrName) {
        requireNotEmpty(idOrName);
        BuildTool command = toolsById.getOrDefault(StringUtils.toIdentifier(idOrName), toolsById.get(idOrName));
        if (command == null) {
            throw new IllegalArgumentException("Unknown build tool: " + idOrName);
        }
        return command;
    }

    /**
     * Returns the build tool suitable to handle the given project.
     *
     * @param directory the directory
     * @return a non-null instance
     */
    public BuildTool detect(File directory) {
        requireNonNull(directory);
        for (BuildTool tool : getTools()) {
            if (tool.accept(directory)) {
                try {
                    BuildTool newTool = ClassUtils.create(tool.getClass());
                    newTool.scmService = scmService;
                    newTool.setWorkingDirectory(directory);
                    return newTool;
                } catch (Exception e) {
                    throw new BuildException("Could not create build tool: " + tool.getName(), e);
                }
            }
        }
        String supportedBuildTools = getTools().stream()
                .map(BuildTool::getName).collect(joining(", "));
        throw new BuildException("A suitable SCM tool for project '" + directory.getAbsolutePath()
                + "' is not registered or the directory does not contain a project. Supported build tools: " + supportedBuildTools);
    }

    /**
     * Loads a project from a directory.
     *
     * @param directory the directory
     * @return a non-null instance
     */
    public Project getProject(File directory) {
        requireNonNull(directory);
        BuildTool buildTool = detect(directory);
        return buildTool.getProject(directory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverTools();
        setupCallbacks();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("scmService", scmService)
                .add("tools", tools)
                .toString();
    }

    private void discoverTools() {
        String[] beanNames = applicationContext.getBeanNamesForType(BuildTool.class);
        for (String beanName : beanNames) {
            BuildTool tool = (BuildTool) applicationContext.getBean(beanName);
            toolsById.put(tool.getName(), tool);
            toolsById.put(tool.getId(), tool);
            tools.add(tool);
        }
    }

    private void setupCallbacks() {
        scmService.setProjectLoader(this::getProject);
    }
}
