package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.JvmUtils;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.*;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.*;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.io.File;

import static net.microfalx.lang.StringUtils.isNotEmpty;

/**
 * A facade for a Maven.
 */
public class MavenLoader {

    private PlexusContainer container;
    private RepositorySystemSession session;

    /**
     * Loads the project from the file.
     *
     * @return the project
     */
    public Project getProject(File pom) {
        initialize();
        MavenProject mavenProject = loadMavenProject(pom, false);
        Project.Builder builder = Project.builder(MavenUtils.getId(mavenProject.getArtifact()));
        builder.version(mavenProject.getVersion()).name(mavenProject.getName())
                .description(mavenProject.getDescription());
        if (mavenProject.getScm() != null) {
            String uri = null;
            if (isNotEmpty(mavenProject.getScm().getDeveloperConnection())) {
                uri = mavenProject.getScm().getDeveloperConnection();
            } else if (isNotEmpty(mavenProject.getScm().getConnection())) {
                uri = mavenProject.getScm().getConnection();
            }
            if (uri != null) builder.repository(getRepository(uri));
        }
        return builder.build();
    }

    private MavenProject loadMavenProject(File pom, boolean resolveDependencies) {
        validate(pom);
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        buildingRequest.setRepositorySession(session);
        buildingRequest.setSystemProperties(System.getProperties());
        buildingRequest.setUserProperties(System.getProperties());
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        buildingRequest.setResolveDependencies(resolveDependencies);
        try {
            ProjectBuilder projectBuilder = container.lookup(ProjectBuilder.class);
            ProjectBuildingResult result = projectBuilder.build(pom, buildingRequest);
            return result.getProject();
        } catch (Exception e) {
            throw new MavenException("Failed to load POM", e);
        }
    }

    private void validate(File pom) {
        if (!pom.exists()) {
            throw new MavenException("A POM is not available at location '" + pom + "'");
        }
    }

    private void initialize() {
        try {
            ContainerConfiguration config = new DefaultContainerConfiguration().setName("maven-core")
                    .setAutoWiring(true).setClassPathScanning(PlexusConstants.SCANNING_INDEX);
            container = new DefaultPlexusContainer(config);
            RepositorySystem repoSystem = container.lookup(RepositorySystem.class);

            DefaultRepositorySystemSession repoSession = MavenRepositorySystemUtils.newSession();

            File localRepoPath = new File(new File(JvmUtils.getHomeDirectory(), ".m2"), "repository");
            LocalRepository localRepo = new LocalRepository(localRepoPath);

            LocalRepositoryManager localRepoManager = repoSystem.newLocalRepositoryManager(repoSession, localRepo);
            repoSession.setLocalRepositoryManager(localRepoManager);

            this.session = repoSession;
        } catch (Exception e) {
            throw new MavenException("Failed to initialize Maven", e);
        }
    }

    private Project.Repository getRepository(String uri) {
        String[] parts = splitUri(uri);
        if (!"scm".equals(parts[0])) return null;
        parts = splitUri(parts[1]);
        if (parts.length != 2) return null;
        Project.Repository.Type type = EnumUtils.fromName(Project.Repository.Type.class, parts[0], Project.Repository.Type.UNKNOWN);
        return new Project.Repository(type, parts[1]);
    }

    private String[] splitUri(String uri) {
        int index = uri.indexOf(':');
        if (index == -1) {
            return new String[]{uri};
        } else {
            return new String[]{uri.substring(0, index), uri.substring(index + 1)};
        }
    }
}
