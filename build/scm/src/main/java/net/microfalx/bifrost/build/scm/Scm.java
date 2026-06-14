package net.microfalx.bifrost.build.scm;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.util.Tool;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.ObjectUtils;

import java.io.File;
import java.util.Set;

/**
 * Base class for all SCM tools.
 */
public abstract class Scm extends Tool<Scm> {

    protected static final Options DEFAULT_OPTIONS = new Options();

    ScmService scmService;
    static ThreadLocal<Project> PROJECT = new ThreadLocal<>();
    private File workingDirectory;

    public Scm(String id, String name) {
        super(id, name);
    }

    /**
     * Adds a new file to repository.
     *
     * @return {@code true} if the file was added, {@code false} otherwise
     */
    public abstract boolean add(Project project, String path);

    /**
     * Returns the current branch name.
     *
     * @return a non-null instance
     */
    public abstract String getCurrentBranch(Project project);

    /**
     * Returns the branches available in the project.
     *
     * @return a non-null instance
     */
    public abstract Set<String> getBranches(Project project);

    /**
     * Returns the tags available in the project.
     *
     * @return a non-null instance
     */
    public abstract Set<String> getTags(Project project);

    /**
     * Downloads a working copy of the project
     *
     * @param project the project
     */
    public File download(Project project) {
        return download(project, new Options());
    }

    /**
     * Downloads a working copy of the project
     *
     * @param project the project
     * @param options the options to be used during download
     */
    public abstract File download(Project project, Options options);

    /**
     * Switches to a different branch of the project.
     *
     * @param project the project
     */
    public File checkout(Project project) {
        return checkout(project, new Options());
    }

    /**
     * Switches to a different branch of the project.
     *
     * @param project the project
     * @param options the options to be used during download
     */
    public abstract File checkout(Project project, Options options);

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
     * @param push    {@code true} to push the code too, {@code false}
     */
    public abstract void commit(Project project, String message, boolean push);

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
     * @param message the message to attach to the tag
     */
    public abstract void tag(Project project, String name, String message);

    /**
     * Returns the workspace for a project.
     *
     * @param project the project
     * @return the directory
     */
    protected final File getWorkspace(Project project) {
        return getWorkspace(project, false);
    }

    /**
     * Returns the workspace for a project.
     *
     * @param project  the project
     * @param override {@code true} to ignore the workspace and use an independent workspace,
     *                 {@code flase} to use current workspace, if exists
     * @return the directory
     */
    protected final File getWorkspace(Project project, boolean override) {
        if (hasWorkingDirectory() && !override) {
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
        File workspace = getWorkspace(project, false);
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
    protected final ProcessLauncher execute(ProcessLauncher launcher) {
        launcher.start(false);
        int exitCode = launcher.waitFor();
        if (exitCode != 0) {
            throw new ScmException("Command failed with exit code " + exitCode)
                    .setLog(launcher.getLogsAsString());
        }
        return launcher;
    }

    /**
     * Updates the working directory with the workspace of the project.
     *
     * @param launcher the launcher
     * @param project  the project
     * @return the launcher
     */
    protected ProcessLauncher updateWorkingDirectory(ProcessLauncher launcher, Project project) {
        return updateWorkingDirectory(launcher, project, false);
    }

    /**
     * Updates the working directory with the workspace of the project.
     *
     * @param launcher the launcher
     * @param project  the project
     * @param override {@code true} to ignore the workspace and use an independent workspace,
     *                 {@code flase} to use current workspace, if exists
     * @return the launcher
     */
    protected ProcessLauncher updateWorkingDirectory(ProcessLauncher launcher, Project project, boolean override) {
        File workspace = getWorkspace(project, override);
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
    @Setter
    @ToString
    public static class Options {
        private String branch;
        private String tag;
        private Integer depth;

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
