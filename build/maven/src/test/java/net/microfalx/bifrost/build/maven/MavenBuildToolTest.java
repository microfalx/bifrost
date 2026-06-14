package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildStep;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MavenBuildToolTest {

    private final MavenBuildTool tool = new MavenBuildTool();

    @Test
    @Disabled
    void buildForReal() {
        BuildExecution execution = tool.build(false);
        assertEquals(0, execution.waitFor(), execution::getLogs);
        assertThat(execution.getSteps().size()).isGreaterThan(5);

        BuildStep step = execution.getFirstStep();
        assertNotNull(step);

        step = execution.getLastStep();
        assertNotNull(step);

        String logs = execution.getFirstAndLastStepLogs();
        assertNotNull(logs);
    }

    @Test
    void build() {
        BuildExecution execution = tool.setDryRun(true).build(false);
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

    @Test
    void deploy() {
        BuildExecution execution = tool.setDryRun(true).build(false);
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

    @Test
    void release() {
        BuildExecution execution = tool.setDryRun(true).build(false);
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

}