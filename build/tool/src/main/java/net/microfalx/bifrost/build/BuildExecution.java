package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.util.ProcessLauncher;
import net.microfalx.lang.ArgumentUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

/**
 * A class which encapsulate the execution of a build tool command.
 */
public class BuildExecution {

    private final BuildTool tool;
    private final ProcessLauncher launcher;

    private Project project;

    BuildExecution(BuildTool tool, ProcessLauncher launcher) {
        this.tool = tool;
        this.launcher = launcher;
    }

    /**
     * Returns the current project.
     *
     * @return the project, empty project is not available or not loaded
     */
    public Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    /**
     * Changes the current project.
     *
     * @param project the project
     * @return self
     */
    public BuildExecution setProject(Project project) {
        this.project = requireNonNull(project);
        return this;
    }

    /**
     * Waits for the execution to complete and returns the exit code.
     *
     * @return a positive integer, 0 = OK
     */
    public int waitFor() {
        return launcher.waitFor();
    }

    /**
     * Returns the execution date.
     *
     * @return a non-null string
     */
    public String getLogs() {
        try {
            return launcher.getLogs().loadAsString();
        } catch (IOException e) {
            return "#ERROR: " + getRootCauseDescription(e);
        }
    }

    /**
     * Returns a stream of string (lines) from the process log.
     *
     * @return a non-null instance
     */
    public Stream<String> getLogsStream() {
        return launcher.getLogsStream();
    }

    /**
     * Returns the lines from the log as a collection of build steps.
     *
     * @return a non-null instance
     */
    public Collection<BuildStep> getSteps() {
        return tool.getSteps(this);
    }

    /**
     * Returns the last step printed by the build tool.
     *
     * @return a non-null instance
     */
    public BuildStep getLastStep() {
        return tool.getLastStep(this);
    }
}
