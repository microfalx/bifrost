package net.microfalx.bifrost.build.scm;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.util.Tool;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.ObjectUtils;

import java.io.File;

/**
 * Base class for all SCM tools.
 */
public abstract class Scm extends Tool<Scm> {

    ScmService scmService;
    static ThreadLocal<Project> PROJECT = new ThreadLocal<>();
    private File workingDirectory;

    public Scm(String id, String name) {
        super(id, name);
    }

    /**
     * Downloads a working copy of the project
     *
     * @param project the project
     */
    public abstract void download(Project project);

    /**
     * Downloads a working copy of the project
     *
     * @param project the project
     */
    public abstract void checkout(Project project);

    /**
     * Update the  working copy.
     *
     * @param project the project
     */
    public abstract Update update(Project project);

    /**
     * Checks the status of a working copy.
     *
     * @param project the project
     */
    public abstract void status(Project project);

    /**
     * Commits the changes to the remote repository.
     * <p>
     * For some SCMs, this means commit (to local repository) and push (send changes to remote repositories).
     *
     * @param project the project
     * @param message the message to attach to the commit
     */
    public abstract void commit(Project project, String message);

    /**
     * Reverts the local changes to the workspace.
     * <p>
     * At the end of the call, the workspace has no changes outside what was already commited.
     *
     * @param project the project
     */
    public abstract void restore(Project project);

    /**
     * Reverts the local workspace to match the remote branch.
     * <p>
     * At the end of the call, the workspace is in sync with the remote repository
     *
     * @param project the project
     */
    public abstract void reset(Project project);

    /**
     * Creates a branch out of the current project branch.
     *
     * @param project the project
     * @param name    the name of the branch
     */
    public abstract void branch(Project project, String name);

    /**
     * Creates a tag out of the current project branch
     *
     * @param project the project
     * @param name    the name of the tag
     */
    public abstract void tag(Project project, String name);

    /**
     * Returns the workspace for a project.
     *
     * @param project the project
     * @return the directory
     */
    protected final File getWorkspace(Project project) {
        if (getWorkingDirectory() != null) {
            return getWorkingDirectory();
        } else {
            return scmService.getWorkspace(project);
        }
    }

    /**
     * Returns whether the project workspace has a working copy.
     *
     * @param project the project
     * @return {@code true} if has working copy, {@code false} otherwise
     */
    protected final boolean hasWorkingCopy(Project project) {
        File workspace = getWorkspace(project);
        return ObjectUtils.isNotEmpty(workspace.listFiles((dir, name) -> !name.endsWith(getFiles()[0])));
    }

    /**
     * Attaches a process to the current thread.
     *
     * @param project the project
     * @return self
     */
    protected final Scm attachProject(Project project) {
        PROJECT.set(project);
        return this;
    }

    /**
     * Returns the project attached to the current thread.
     *
     * @return a non-null instance
     */
    protected static Project currentProject() {
        return PROJECT.get();
    }

    /**
     * Executes the process and throws an exception if the process fails.
     *
     * @param launcher the launcher
     */
    protected final void execute(ProcessLauncher launcher) {
        launcher.start(false);
        int exitCode = launcher.waitFor();
        if (exitCode != 0) {
            throw new ScmException("Command failed with exit code " + exitCode)
                    .setLog(launcher.getLogsAsString());
        }
    }

    /**
     * Updates the working directory with the workspace of the project.
     *
     * @param launcher the launcher
     * @param project  the project
     * @return the launcher
     */
    protected ProcessLauncher updateWorkingDirectory(ProcessLauncher launcher, Project project) {
        File workspace = getWorkspace(project);
        if (!workspace.exists()) {
            throw new ScmException("Workspace for project '" + project.getName() + "' does not exist: " + workspace);
        }
        if (!hasWorkingCopy(project)) {
            throw new ScmException("Directory '" + workspace + "' does not contain a workspace for project: " + project.getName());
        }
        return launcher.setWorkingDirectory(workspace);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("executable", getExecutable())
                .toString();
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    public static final class Update {

        private final int fileCount;
        private final int insertionCount;
        private final int deletionCount;

        public Update() {
            fileCount = -1;
            insertionCount = -1;
            deletionCount = -1;
        }

        public boolean isNotEmpty() {
            return fileCount >= 0;
        }

        public boolean hasChanges() {
            return isNotEmpty() && fileCount > 0;
        }
    }
}
