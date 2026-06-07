package net.microfalx.bifrost.build;

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
                    return ClassUtils.create(tool.getClass());
                } catch (Exception e) {
                    throw new BuildException("Could not create build tool: " + tool.getName(), e);
                }
            }
        }
        String supportedBuildTools = getTools().stream()
                .map(BuildTool::getName).collect(joining(", "));
        throw new BuildException("A suitable build tool for project '" + directory.getAbsolutePath()
                + "' is not registered or the directory does not contain a project. Supported build tools: " + supportedBuildTools);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverTools();
    }

    private void discoverTools() {
        String[] beanNames = applicationContext.getBeanNamesForType(BuildTool.class);
        for (String beanName : beanNames) {
            BuildTool command = (BuildTool) applicationContext.getBean(beanName);
            toolsById.put(command.getName(), command);
            toolsById.put(command.getId(), command);
            tools.add(command);
        }
    }
}
