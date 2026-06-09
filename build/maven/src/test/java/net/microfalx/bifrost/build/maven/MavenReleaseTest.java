package net.microfalx.bifrost.build.maven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static net.microfalx.lang.StringUtils.isNotEmpty;

class MavenReleaseTest {

    private MavenBuildTool buildTool;
    private MavenRelease release;

    @BeforeEach
    void setup() {
        buildTool = new MavenBuildTool();
        String workspace = System.getProperty("bifrost.build.workspace");
        if (isNotEmpty(workspace)) buildTool.setWorkingDirectory(new File(workspace));
        release = new MavenRelease(buildTool);
        release.setProject(buildTool.getProject());
    }

    @Test
    @Disabled
    void release() {
        release.waitFor();
    }

}