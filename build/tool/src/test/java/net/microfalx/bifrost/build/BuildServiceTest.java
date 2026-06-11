package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.Git;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Prepare;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.lang.JvmUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Prepare(subjects = {ScmService.class, TestBuildTool.class, Git.class})
class BuildServiceTest extends ServiceUnitTestCase {

    @Subject private BuildService buildService;

    private Project project;

    @BeforeEach
    void setup() {
        project = Project.builder("bifrost-build-tool").build();
    }

    @Test
    void getTools() {
        assertEquals(1, buildService.getTools().size());
    }

    @Test
    void detect() {
        BuildTool tool = buildService.detect(getWorkingDirectory());
        assertEquals(0, tool.build().waitFor());
    }

    @Test
    void scm() {
        BuildTool tool = buildService.detect(getWorkingDirectory());
        Scm scm = tool.getScm(project);
        scm.update(project);
    }

    private File getWorkingDirectory() {
        File workingDirectory = JvmUtils.getWorkingDirectory();
        while (workingDirectory != null) {
            File top = new File(workingDirectory, ".git");
            if (top.exists()) return workingDirectory;
            workingDirectory = workingDirectory.getParentFile();
        }
        throw new IllegalStateException("Cannot detect top project working directory");
    }

}