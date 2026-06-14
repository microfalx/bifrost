package net.microfalx.bifrost.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A class representing an artifact produced by a build activity.
 * <p>
 * In Java worlds, it usually represents a JAR.
 */
@ToString(callSuper = true)
public class Artifact extends NamedAndTaggedIdentifyAware<String> {

    public static final String TYPE_JAR = "jar";
    public static final String TYPE_POM = "pom";

    private String groupId;
    private String artifactId;
    private String type;

    /**
     * Creates an artifact builder.
     *
     * @param groupId the group identifier
     * @return a non-null instance
     */
    public static Builder builder(String groupId, String artifactId) {
        return new Builder(requireNotEmpty(groupId), requireNotEmpty(artifactId));
    }

    /**
     * Returns the group identifier.
     *
     * @return a non-null instance
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Returns the artifact identifier.
     *
     * @return a non-null instance
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the type of artifact.
     *
     * @return a non-null instance
     */
    public String getType() {
        return type;
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private final String groupId;
        private final String artifactId;

        private String type = "jar";

        public Builder(String groupId, String artifactId) {
            super(groupId + ":" + artifactId);
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public Builder type(String type) {
            this.type = requireNotEmpty(type);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Artifact();
        }

        @Override
        public Artifact build() {
            Artifact artifact = (Artifact) super.build();
            artifact.groupId = groupId;
            artifact.artifactId = artifactId;
            artifact.type = type;
            return artifact;
        }
    }
}
