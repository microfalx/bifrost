package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildLine;
import net.microfalx.bifrost.build.BuildTool;
import net.microfalx.bifrost.util.ProcessLauncher;
import net.microfalx.lang.JvmUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.microfalx.lang.StringUtils.isEmpty;

@Component
public class MavenBuildTool extends BuildTool {

    private final MavenLoader mavenLoader = new MavenLoader();

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
    public Project getProject(File directory) {
        return mavenLoader.getProject(new File(directory, "pom.xml"));
    }

    @Override
    public String getExecutable() {
        return JvmUtils.isWindows() ? "mvn.cmd" : "mvn";
    }

    @Override
    protected String[] getProjectFiles() {
        return new String[]{"pom.xml"};
    }

    @Override
    protected BuildLine parse(String line, int index) {
        if (isEmpty(line.trim())) {
            return new BuildLine(BuildLine.Type.TEXT, line, index);
        } else {
            Matcher matcher = MODULE_NAME.matcher(line);
            if (matcher.matches()) {
                return new BuildLine(BuildLine.Type.MODULE, line, index)
                        .setName(matcher.group(1));
            } else {
                matcher = PLUGIN_NAME.matcher(line);
                if (matcher.matches()) {
                    return new BuildLine(BuildLine.Type.PLUGIN, line, index)
                            .setName(matcher.group(1));
                } else {
                    return new BuildLine(BuildLine.Type.TEXT, line, index);
                }
            }
        }
    }

    private ProcessLauncher internalCreateLauncher() {
        ProcessLauncher launcher = createLauncher();
        if (isClean()) launcher = launcher.addArgument("clean");
        return launcher;
    }

    private static final Pattern MODULE_NAME = Pattern.compile("^\\[INFO\\]\\s+Building\\s+(.+)\\s+.*");
    private static final Pattern PLUGIN_NAME = Pattern.compile("^\\[INFO\\]\\s---\\s+([A-Za-z0-9\\-\\.\\:]+).*");
}
