package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.lang.JvmUtils;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.*;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.*;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;

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
        builder.name(mavenProject.getName()).description(mavenProject.getDescription());
        if (mavenProject.getScm() != null && isNotEmpty(mavenProject.getScm().getConnection())) {
            builder.repository(mavenProject.getScm().getConnection());
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
}
