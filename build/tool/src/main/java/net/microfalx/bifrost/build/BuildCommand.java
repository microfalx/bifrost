package net.microfalx.bifrost.build;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        initProject();
        doExecute();
    }

    private String createBuildMessage() {
        String action;
        if (!push) {
            action = "building";
        } else {
            action = release ? "releasing" : "deploying";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(action).append(" project");
        if (clean) builder.append(" clean");
        String projectName = bold(project.getName());
        builder.append(' ').append(quote(projectName));
        return builder.toString();
    }

    private BuildExecution startExecuting() {
        print(createBuildMessage());
        BuildExecution execution;
        if (!push) {
            execution = buildTool.build();
        } else {
            if (release) {
                execution = buildTool.release();
            } else {
                execution = buildTool.deploy();
            }
        }
        printDots();
        execution.setProject(project);
        return execution;
    }

    private void completeExecution(BuildExecution execution) {
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
        LOGGER.info("Execute 'build' command with arguments: clean={}, push={}, release={}, verbose={}",
                clean, push, release, verbose);
        File workingDirectory = getFinalWorkingDirectory();
        buildTool = buildService.detect(workingDirectory);
        buildTool.setWorkingDirectory(workingDirectory);
        buildTool.setClean(clean);
    }

    private void initProject() {
        print("Loading project").printDots();
        execute(() -> loadProject(buildTool));
    }

    private void doExecute() {
        BuildExecution execution = startExecuting();
        completeExecution(execution);
    }

    private void loadProject(BuildTool buildTool) {
        project = buildTool.getProject(getFinalWorkingDirectory());
    }
}
