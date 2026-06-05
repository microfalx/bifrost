package net.microfalx.bifrost.build;

import net.microfalx.bifrost.util.ProcessException;

/**
 * A base exception for all build errors.
 */
public class BuildException extends ProcessException {

    public BuildException(String message) {
        super(message);
    }

    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
