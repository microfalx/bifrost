package net.microfalx.bifrost.api;

import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedAndTaggedIdentifyAware;
import net.microfalx.lang.UriUtils;

import javax.swing.text.html.Option;
import java.net.URI;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A class representing a software project.
 */
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

    public static class Builder extends NamedAndTaggedIdentifyAware.Builder<String> {

        private URI repository;
        private String version;

        private Builder(String id) {
            super(id);
        }

        public Builder repository(String repository) {
            this.repository = UriUtils.parseUri(requireNonNull(repository));
            return this;
        }

        public Builder repository(URI repository) {
            this.repository = requireNonNull(repository);
            return this;
        }

        public Builder version(String version) {
            this.version = requireNotEmpty(version);
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
            return project;
        }
    }
}
