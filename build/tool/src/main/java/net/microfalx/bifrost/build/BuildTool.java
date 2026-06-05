package net.microfalx.bifrost.build;

import net.microfalx.bifrost.util.ProcessLauncher;
import net.microfalx.lang.*;

import java.io.File;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * An interface for a build tool.
 */
public abstract class BuildTool implements Identifiable<String>, Nameable, Descriptable {

    private final String id;
    private final String name;
    private boolean dryRun;
    private boolean clean;
    private File workingDirectory;

    protected final Logger logger = Logger.create();

    public BuildTool(String id, String name) {
        requireNotEmpty(id);
        requireNotEmpty(name);
        this.id = id;
        this.name = name;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return "";
    }

    /**
     * Returns the log of the build process.
     *
     * @return a non-null instance
     */
    public String getLog() {
        return logger.getOutput();
    }

    /**
     * Returns whether the build tool can process the project at the specified directory.
     *
     * @param directory the directory
     * @return {@code true} if the tool can be used, {@code false} otherwise
     */
    public boolean accept(File directory) {
        requireNonNull(directory);
        for (String projectFile : getProjectFiles()) {
            File file = new File(directory, projectFile);
            if (!file.exists()) return false;
        }
        return true;
    }

    /**
     * Changes the working directory.
     *
     * @param workingDirectory the working directory
     * @return self
     */
    public BuildTool setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * Returns whether the build runs in "dry-run" mode.
     *
     * @return {@code true} if dry run, {@code false} otherwise
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Changes whether the tool simulates the execution.
     *
     * @param dryRun {@code true} for dry run, {@code false} otherwise
     * @return self
     */
    public BuildTool setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    /**
     * Returns whether the cleaning is performed.
     *
     * @return {@code true} to cleanup, {@code false} otherwise
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * Changes whether the cleaning is performed.
     *
     * @param clean {@code true} to clean, {@code false} otherwise
     * @return self
     */
    public BuildTool setClean(boolean clean) {
        this.clean = clean;
        return this;
    }

    /**
     * Builds the project and deploys artifacts in local repository (snapshots).
     */
    public abstract BuildExecution build();

    /**
     * Builds the project and deploys artifacts in remote repository (snapshots).
     */
    public abstract BuildExecution deploy();

    /**
     * Releases the project and deploys artifacts in remote repository (GA).
     */
    public abstract BuildExecution release();

    /**
     * Returns the executable name.
     *
     * @return a non-null instance
     */
    protected abstract String getExecutable();

    /**
     * Returns the files required to be in the working directory for the tool to be selected.
     *
     * @return a non-null instance
     */
    protected abstract String[] getProjectFiles();

    /**
     * Creates a process launcher for the executable associated with this tool.
     *
     * @return a non-null instance
     */
    protected final ProcessLauncher createLauncher() {
        if (workingDirectory != null) workingDirectory = JvmUtils.getWorkingDirectory();
        return ProcessLauncher.create(getExecutable())
                .setWorkingDirectory(workingDirectory)
                .setDryRun(dryRun);
    }

    /**
     * Creates a execution class.
     *
     * @param launcher the launcher
     * @return a non-null instance
     */
    protected final BuildExecution createExecution(ProcessLauncher launcher) {
        requireNonNull(launcher);
        launcher.start(true);
        return new BuildExecution(this, launcher);

    }
}
