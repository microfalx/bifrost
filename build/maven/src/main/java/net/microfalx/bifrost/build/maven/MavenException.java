package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.build.BuildException;

/**
 * Base exception for all Maven exceptions.
 */
public class MavenException extends BuildException {

    public MavenException(String message) {
        super(message);
    }

    public MavenException(String message, Throwable cause) {
        super(message, cause);
    }
}
