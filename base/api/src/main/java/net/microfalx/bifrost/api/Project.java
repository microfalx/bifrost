package net.microfalx.bifrost.api;

import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.UriUtils;

import java.net.URI;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A class representing a software project.
 */
@ToString(callSuper = true)
public class Project extends NamedAndTaggedIdentifyAware<String> {

    /**
     * Creates a project builder.
     *
     * @param id the project identifier
     * @return a non-null instance
     */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    private URI repository;
    private String branch;
    private String version;

    /**
     * Returns the URI of the code repository.
     *
     * @return a non-null instance
     */
    public URI getRepository() {
        return repository;
    }

    /**
     * Returns the optional project version
     *
     * @return a non-null optional
     */
    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    /**
     * Changes the version of this project.
     *
     * @param version the new version
     * @return a new copy with a new version
     */
    public Project withVersion(String version) {
        Project copy = (Project) copy();
        copy.version = requireNotEmpty(version);
        return copy;
    }

    /**
     * Returns the branch of the project in SCM.
     *
     * @return a non-null instance
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Changes the branch of this project.
     *
     * @param branch the new branch
     * @return a new copy with a new branch
     */
    public Project withBranch(String branch) {
        Project copy = (Project) copy();
        copy.branch = requireNotEmpty(branch);
        return copy;
    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private URI repository;
        private String branch = "main";
        private String version;

        private Builder(String id) {
            super(id);
        }

        public Builder repository(String repository) {
            return repository(UriUtils.parseUri(requireNonNull(repository)));
        }

        public Builder repository(URI repository) {
            this.repository = requireNonNull(repository);
            return this;
        }

        public Builder version(String version) {
            this.version = requireNotEmpty(version);
            return this;
        }

        public Builder branch(String branch) {
            this.branch = requireNotEmpty(branch);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Project();
        }

        @Override
        public Project build() {
            Project project = (Project) super.build();
            project.repository = repository;
            project.version = version;
            project.branch = branch;
            return project;
        }
    }
}
