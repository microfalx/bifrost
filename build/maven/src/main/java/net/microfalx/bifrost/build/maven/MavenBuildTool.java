package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildTool;
import net.microfalx.bifrost.util.ProcessLauncher;
import net.microfalx.lang.JvmUtils;
import org.springframework.stereotype.Component;

@Component
public class MavenBuildTool extends BuildTool {

    public MavenBuildTool() {
        super("maven", "Apache Maven");
    }

    @Override
    public BuildExecution build() {
        ProcessLauncher launcher = internalCreateLauncher().addArgument("install");
        return createExecution(launcher);
    }

    @Override
    public BuildExecution deploy() {
        ProcessLauncher launcher = internalCreateLauncher().addArgument("deploy");
        return createExecution(launcher);
    }

    @Override
    public BuildExecution release() {
        ProcessLauncher launcher = internalCreateLauncher().addArgument("release");
        return createExecution(launcher);
    }

    @Override
    public String getExecutable() {
        return JvmUtils.isWindows() ? "mvn.cmd" : "mvn";
    }

    @Override
    protected String[] getProjectFiles() {
        return new String[]{"pom.xml"};
    }

    private ProcessLauncher internalCreateLauncher() {
        ProcessLauncher launcher = createLauncher();
        if (isClean()) launcher = launcher.addArgument("clean");
        return launcher;
    }
}
