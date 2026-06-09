package net.microfalx.bifrost.build.scm;

import net.microfalx.bootstrap.cli.CliException;

/**
 * Base exceptions for all SCM exceptions.
 */
public class ScmException extends CliException {

    public ScmException(String message) {
        super(message);
    }

    public ScmException(String message, Throwable cause) {
        super(message, cause);
    }
}
