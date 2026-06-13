package net.microfalx.bifrost.api;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;

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

    private Repository repository;
    private String branch;
    private String version;

    /**
     * Returns the code repository.
     *
     * @return a non-null instance
     */
    public Repository getRepository() {
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

    @Getter
    @ToString
    public static class Repository {

        private final Type type;
        private final String uri;

        public static Repository git(String uri) {
            return new Repository(Type.GIT, uri);
        }

        public Repository(Type type, String uri) {
            this.type = requireNonNull(type);
            this.uri = requireNonNull(uri);
        }

        public enum Type {
            UNKNOWN,
            GIT,
            SVN
        }

    }

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private Repository repository = new Repository(Repository.Type.UNKNOWN, "");
        private String branch = "main";
        private String version;

        private Builder(String id) {
            super(id);
        }

        public Builder repository(Repository repository) {
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
