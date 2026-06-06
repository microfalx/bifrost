package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.lang.JvmUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static net.microfalx.lang.JvmUtils.getWorkingDirectory;
import static org.junit.jupiter.api.Assertions.*;

class MavenLoaderTest {

    private File pom;
    private MavenLoader loader;

    @BeforeEach
    void setup() {
        pom = new File(getWorkingDirectory(), "pom.xml");
        loader = new MavenLoader();
    }

    @Test
    void loadProject() {
        Project project = loader.getProject(pom);
        assertNotNull(project);
        assertNotNull(project.getId());
        assertEquals("Bifrost :: Build :: Maven", project.getName());
        Assertions.assertThat(project.getRepository().toASCIIString()).contains("https://github.com/microfalx/bifrost");
    }

}