package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.util.Execution;

import java.util.Collection;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which encapsulate the execution of a build tool command.
 */
public class BuildExecution extends Execution<BuildTool, BuildExecution> {

    private Project project;

    public BuildExecution(BuildTool tool, String name) {
        super(tool, name);
    }

    /**
     * Returns the current project for this execution.
     *
     * @return the project, empty project is not available or not loaded
     */
    public final Optional<Project> getProject() {
        return Optional.ofNullable(project);
    }

    /**
     * Changes the current project for this execution.
     *
     * @param project the project
     * @return self
     */
    public final BuildExecution setProject(Project project) {
        this.project = requireNonNull(project);
        return this;
    }

    /**
     * Returns the lines from the log as a collection of build steps.
     *
     * @return a non-null instance
     */
    public Collection<BuildStep> getSteps() {
        return getTool().getSteps(this);
    }

    /**
     * Returns the fist build step.
     *
     * @return a non-null instance
     */
    public final BuildStep getFirstStep() {
        return getTool().getFirstStep(this);
    }

    /**
     * Returns the last step printed by the build tool.
     *
     * @return a non-null instance
     */
    public BuildStep getLastStep() {
        return getTool().getLastStep(this);
    }

    /**
     * Returns the last step printed by the build tool.
     *
     * @return a non-null instance
     */
    public String getFirstAndLastStepLogs() {
        return getTool().getFirstAndLastStepLogs(this);
    }

    /**
     * Returns a project or fails.
     *
     * @return a non-null instance
     */
    protected final Project getProjectOrFail() {
        return getProject().orElseThrow();
    }
}
