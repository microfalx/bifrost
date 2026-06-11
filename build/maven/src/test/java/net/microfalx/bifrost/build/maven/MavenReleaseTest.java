package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.Git;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.lang.IdGenerator;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;

import static net.microfalx.lang.FileUtils.validateDirectoryExists;
import static net.microfalx.lang.JvmUtils.getTemporaryDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class MavenReleaseTest extends ServiceUnitTestCase {

    @Mock private ScmService scmService;

    private Project project;
    private MavenBuildTool buildTool;
    private MavenRelease release;

    @BeforeEach
    void setup() {
        project = Project.builder("bootstrap-demo")
                .repository("https://github.com/adrian-tarau/bootstrap-demo.git")
                .branch("release_test")
                .build();
        buildTool = new MavenBuildTool();
        File workspace = validateDirectoryExists(new File(getTemporaryDirectory(), "bootstrap-demo-" + getId()));
        Scm scm = new Git();
        scm.setWorkingDirectory(workspace);
        scm.checkout(project);

        buildTool.setWorkingDirectory(workspace);
        release = new MavenRelease(buildTool);

        doReturn(buildTool.getWorkingDirectory()).when(scmService).getWorkspace(any(Project.class));
        doReturn(scm).when(scmService).detect(any(Project.class));
        doReturn(scm).when(scmService).detect(any(File.class));
        Reflect.on(buildTool).set("scmService", scmService);

        release.setProject(buildTool.getProject());
    }

    @Test
    @Disabled
    void release() {
        assertEquals(0, release.waitFor());
    }

    private static String getId() {
        return Long.toString(System.currentTimeMillis(), Character.MAX_RADIX)
                + "-" + IdGenerator.get().nextAsString();
    }

}