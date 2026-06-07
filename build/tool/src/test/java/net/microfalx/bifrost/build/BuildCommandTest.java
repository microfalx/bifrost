package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import picocli.CommandLine;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BuildCommandTest {

    @Mock private BuildService buildService;
    @Mock private BuildExecution execution;
    @Mock private BuildTool buildTool;
    private Project project;

    @InjectMocks
    private BuildCommand command;

    @BeforeEach
    void setup() {
        project = (Project) Project.builder("test").name("Test").build();
        doReturn(buildTool).when(buildService).detect(any(File.class));
        doReturn(project).when(buildTool).getProject(any(File.class));
        doReturn(execution).when(buildTool).build();
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