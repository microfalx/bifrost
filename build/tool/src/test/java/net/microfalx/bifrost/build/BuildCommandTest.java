package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import picocli.CommandLine;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;

class BuildCommandTest extends ServiceUnitTestCase {

    @Mock private Scm scm;
    @Mock private ScmService scmService;
    @Mock private BuildService buildService;
    @Mock private BuildExecution execution;
    @Mock private BuildTool buildTool;
    private Project project;

    @Subject
    private BuildCommand command;

    @BeforeEach
    void setup() {
        project = (Project) Project.builder("test").name("Test").build();
        doReturn(scm).when(scmService).detect(any(File.class));
        doReturn(scm).when(scmService).detect(any(Project.class));
        doReturn(buildTool).when(buildService).detect(any(File.class));
        doReturn(project).when(buildTool).getProject(any(File.class));
        doReturn(scm).when(buildTool).getScm(any(Project.class));
        doReturn(execution).when(buildTool).build(anyBoolean());
    }

    @Test
    void invalidCommand() {
        assertEquals(2, execute("aaa"));
    }

    @Test
    void invalidoption() {
        assertEquals(2, execute("-k"));
    }

    @Test
    void buildNoOptions() {
        assertEquals(0, execute());
    }

    @Test
    void buildWithCleanup() {
        assertEquals(0, execute("-c"));
    }

    private int execute(String... args) {
        return new CommandLine(command).execute(args);
    }

}