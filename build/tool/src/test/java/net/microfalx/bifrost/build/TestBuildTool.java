package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.lang.JvmUtils;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TestBuildTool extends BuildTool {

    public TestBuildTool() {
        super("test", "Test");
    }

    @Override
    public BuildExecution build(boolean forRelease) {
        return createExecution(createLauncher());
    }

    @Override
    public BuildExecution deploy(boolean forRelease) {
        return createExecution(createLauncher());
    }

    @Override
    public BuildExecution release() {
        return createExecution(createLauncher());
    }

    @Override
    public Project getProject() {
        return Project.builder("test").build();
    }

    @Override
    public Project getProject(File directory) {
        return Project.builder("test").build();
    }

    @Override
    protected BuildLine parse(String line, int index) {
        return new BuildLine(BuildLine.Type.TEXT, line, index);
    }

    @Override
    protected String getExecutable() {
        return JvmUtils.isWindows() ? "rundll32.exe" : "true";
    }

    @Override
    protected String[] getFiles() {
        return new String[]{"pom.xml"};
    }
}
