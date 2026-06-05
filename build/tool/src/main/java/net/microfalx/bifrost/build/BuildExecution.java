package net.microfalx.bifrost.build;

import net.microfalx.bifrost.util.ProcessLauncher;

import java.io.IOException;

import static net.microfalx.lang.ExceptionUtils.getRootCauseDescription;

/**
 * A class which encapsulate the execution of a build tool command.
 */
public class BuildExecution {

    private final BuildTool tool;
    private final ProcessLauncher launcher;

    BuildExecution(BuildTool tool, ProcessLauncher launcher) {
        this.tool = tool;
        this.launcher = launcher;
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
}
