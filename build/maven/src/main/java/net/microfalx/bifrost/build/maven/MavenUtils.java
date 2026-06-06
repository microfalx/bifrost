package net.microfalx.bifrost.build.maven;

import net.microfalx.lang.ArgumentUtils;
import org.apache.maven.artifact.Artifact;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Various utilities around Maven.
 */
public class MavenUtils {

    /**
     * Returns the project identifier out of a Maven artifact.
     *
     * @param artifact the artifact
     * @return a non-null instance
     */
    public static String getId(Artifact artifact) {
        requireNonNull(artifact);
        return artifact.getGroupId() + ":" + artifact.getArtifactId();
    }
}
