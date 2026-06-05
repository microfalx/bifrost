package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.build.BuildExecution;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MavenBuildToolTest {

    private final MavenBuildTool tool = new MavenBuildTool();

    @Test
    @Disabled
    void buildForReal() {
        BuildExecution execution = tool.build();
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

    @Test
    void build() {
        BuildExecution execution = tool.setDryRun(true).build();
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

    @Test
    void deploy() {
        BuildExecution execution = tool.setDryRun(true).build();
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

    @Test
    void release() {
        BuildExecution execution = tool.setDryRun(true).build();
        assertEquals(0, execution.waitFor(), execution::getLogs);
    }

}