package net.microfalx.bifrost.build;

import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.bootstrap.cli.util.Tool;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TextUtils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.NA_STRING;

/**
 * An interface for a build tool.
 */
@Slf4j
public abstract class BuildTool extends Tool<BuildTool> {

    ScmService scmService;

    public BuildTool(String id, String name) {
        super(id, name);
    }

    /**
     * Builds the project and deploys artifacts in local repository (snapshots).
     *
     * @param forRelease {@code true} if the build is targeting a release, {@code false} otherwise
     */
    public abstract BuildExecution build(boolean forRelease);

    /**
     * Builds the project and deploys artifacts in remote repository (snapshots).
     *
     * @param forRelease {@code true} if the build is targeting a release, {@code false} otherwise
     */
    public abstract BuildExecution deploy(boolean forRelease);

    /**
     * Releases the project and deploys artifacts in remote repository (GA).
     */
    public abstract BuildExecution release();

    /**
     * Loads and the project from the current workspace.
     *
     * @return a non-null instance
     */
    public abstract Project getProject();

    /**
     * Loads and the project from the given directory.
     *
     * @return a non-null instance
     */
    public abstract Project getProject(File directory);

    /**
     * Returns the SCM to manage the project.
     *
     * @param project the project
     * @return the scm
     */
    public Scm getScm(Project project) {
        requireNonNull(project);
        Scm scm = scmService.detect(getWorkingDirectory());
        scm.setWorkingDirectory(getWorkingDirectory());
        return scm;
    }

    /**
     * Returns the lines from the log as a collection of build steps.
     *
     * @param execution the tool execution
     * @return a non-null instance
     */
    public final Collection<BuildStep> getSteps(BuildExecution execution) {
        Queue<BuildStep> steps = new LinkedList<>();
        extractSteps(execution, null, steps, Integer.MAX_VALUE);
        return steps;
    }

    /**
     * Returns the fist build step.
     *
     * @param execution the tool execution
     * @return a non-null instance
     */
    public final BuildStep getFirstStep(BuildExecution execution) {
        Queue<BuildStep> steps = new LinkedList<>();
        extractSteps(execution, steps, null, 1);
        return steps.isEmpty() ? new BuildStep("None") : steps.iterator().next();
    }

    /**
     * Returns the last build step.
     *
     * @param execution the tool execution
     * @return a non-null instance
     */
    public final BuildStep getLastStep(BuildExecution execution) {
        Queue<BuildStep> steps = new LinkedList<>();
        extractSteps(execution, null, steps, 1);
        return steps.isEmpty() ? new BuildStep("None") : steps.iterator().next();
    }

    /**
     * Returns the fist build step.
     *
     * @param execution the tool execution
     * @return a non-null instance
     */
    public final String getFirstAndLastStepLogs(BuildExecution execution) {
        Queue<BuildStep> headSteps = new LinkedList<>();
        Queue<BuildStep> tailSteps = new LinkedList<>();
        extractSteps(execution, headSteps, tailSteps, 1);
        StringBuilder builder = new StringBuilder();
        if (!headSteps.isEmpty()) {
            builder.append(headSteps.poll().getLogs()).append("\n");
        }
        String separator = StringUtils.getStringOfChar('.', 35);
        if (!builder.isEmpty()) {
            builder.append(separator).append(" truncated ").append(separator).append("\n");
        }
        if (!tailSteps.isEmpty()) {
            builder.append(tailSteps.poll().getLogs());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("executable", getExecutable())
                .toString();
    }

    /**
     * Parses the output line.
     *
     * @param line  the line
     * @param index the index of the line
     * @return a non-null instance
     */
    protected abstract BuildLine parse(String line, int index);

    /**
     * Creates an execution attached to a Maven command.
     *
     * @param launcher the launcher
     * @return a non-null instance
     */
    protected final BuildExecution createExecution(ProcessLauncher launcher) {
        requireNonNull(launcher);
        launcher.start(true);
        return new BuildExecution(this, launcher.getName()).setLauncher(launcher);
    }


    private void extractSteps(BuildExecution execution, Queue<BuildStep> headSteps, Queue<BuildStep> tailSteps, int maximumSteps) {
        int index = 1;
        String moduleName = null;
        BuildStep currentStep = new BuildStep(NA_STRING).setModule(execution.getProject().isPresent() ? execution.getProject().get().getName() : NA_STRING);
        Stream<String> stream = execution.getLogsStream();
        Iterator<String> iterator = stream.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            BuildLine buildLine = parse(line, index++);
            if (buildLine.getType() == BuildLine.Type.MODULE) {
                moduleName = buildLine.getName();
            } else if (buildLine.getType() == BuildLine.Type.PLUGIN) {
                appendStep(headSteps, tailSteps, currentStep, maximumSteps);
                currentStep = new BuildStep(buildLine.getName()).setModule(moduleName);
            } else {
                if (currentStep != null) {
                    currentStep.add(line);
                }
            }
        }
        appendStep(headSteps, tailSteps, currentStep, maximumSteps);
    }

    private void appendStep(Queue<BuildStep> headSteps, Queue<BuildStep> tailSteps, BuildStep step, int maximumSteps) {
        if (step == null) return;
        if (headSteps != null && headSteps.size() < maximumSteps) headSteps.add(step);
        if (tailSteps != null) {
            tailSteps.add(step);
            if (tailSteps.size() > maximumSteps) tailSteps.poll();
        }
    }
}
