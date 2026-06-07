package net.microfalx.bifrost.build;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.command.RunnableCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static net.microfalx.lang.TextUtils.insertSpaces;

@Component
@CommandLine.Command(name = "build", mixinStandardHelpOptions = true,
        description = "Building & Release Management")
public class BuildCommand extends RunnableCommand {

    @Autowired private BuildService buildService;

    @CommandLine.Option(names = {"-c", "--clean"}, description = "Cleans the previous build")
    private boolean clean;

    @CommandLine.Option(names = {"-p", "--push"}, description = "Builds and deploys artifacts in remote repository")
    private boolean push;

    @CommandLine.Option(names = {"-r", "--release"}, description = "Builds, releases and deploys artifacts in remote repository")
    private boolean release;

    @CommandLine.Option(names = {"-d", "--directroy"}, description = "The directory where the project is located (instead of the working directory")
    private String workingDirectory;

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Displays verbose information")
    private boolean verbose;

    private BuildTool buildTool;
    private Project project;

    @Override
    protected void execute() throws IOException {
        initBuildTool();
        print("Loading project...");
        execute(() -> loadProject(buildTool));
        String projectName = bold(project.getName());
        BuildExecution execution;
        if (!push) {
            print("building project " + quote(projectName));
            execution = buildTool.build();
        } else {
            if (release) {
                print("releasing project " + quote(projectName));
                execution = buildTool.release();
            } else {
                print("deploy project " + quote(projectName));
                execution = buildTool.deploy();
            }
        }
        print("...");
        execution.setProject(project);
        int exitCode = execute(execution::waitFor);
        printLn(exitCode(exitCode));
        if (exitCode > 0) {
            String log = insertSpaces(execution.getLastStep().getLogs(), 2, true);
            printLn().printLn(log);
        }
    }

    private File getFinalWorkingDirectory() {
        return getWorkingDirectory(this.workingDirectory);
    }

    private void initBuildTool() {
        File workingDirectory = getFinalWorkingDirectory();
        buildTool = buildService.detect(workingDirectory);
        buildTool.setWorkingDirectory(workingDirectory).setClean(clean);
    }

    public void loadProject(BuildTool buildTool) {
        project = buildTool.getProject(getFinalWorkingDirectory());
    }
}
