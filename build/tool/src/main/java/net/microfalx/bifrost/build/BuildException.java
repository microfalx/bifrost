package net.microfalx.bifrost.build;

import net.microfalx.bootstrap.cli.CliException;

/**
 * A base exception for all build errors.
 */
public class BuildException extends CliException {

    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
