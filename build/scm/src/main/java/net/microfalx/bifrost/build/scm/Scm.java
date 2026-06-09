package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.util.Tool;

import java.io.File;

/**
 * Base class for all SCM tools.
 */
public abstract class Scm extends Tool<Scm> {

    ScmService scmService;

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
     */
    public abstract void commit(Project project);

    /**
     * Reverts the local changes.
     * <p>
     * At the end of the call, the workspace is in sync with the remote repository
     *
     * @param project the project
     */
    public abstract void revert(Project project);

    /**
     * Returns the workspace for a project.
     *
     * @param project the project
     * @return the directory
     */
    protected final File getWorkspace(Project project) {
        return scmService.getWorkspace(project);
    }

}
