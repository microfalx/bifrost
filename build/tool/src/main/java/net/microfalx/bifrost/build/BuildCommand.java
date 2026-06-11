package net.microfalx.bifrost.build;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bootstrap.cli.command.RunnableCommand;
import net.microfalx.bootstrap.cli.util.Console;
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
        update();
        doExecute();
    }

    private String createBuildMessage() {
        String action;
        if (release) {
            action = "releasing";
        } else if (push) {
            action = "deploying";
        } else {
            action = "building";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(action).append(" project");
        if (clean) builder.append(" clean");
        return builder.toString();
    }

    private BuildExecution startExecuting() {
        Console console = getConsole();
        console.print(createBuildMessage()).printSpace()
                .printQuote().printBold(project.getName()).printQuote();
        String version = " (" + project.getVersion().orElseThrow() + ") ";
        console.print(version);
        BuildExecution execution;
        if (release) {
            console.printLn();
            execution = buildTool.release();
        } else {
            console.printDots();
            if (push) {
                execution = buildTool.deploy();
            } else {
                execution = buildTool.build();
            }
        }
        execution.setProject(project);
        return execution;
    }

    private void completeExecution(BuildExecution execution) {
        Console console = getConsole();
        int exitCode = getConsole().execute(execution::waitFor);
        if (release) {
            console.print("Project was released...");
        }
        console.printExitCode(exitCode);
        if (exitCode > 0) {
            String log = insertSpaces(execution.getFirstAndLastStepLogs(), 2, true);
            console.printLn().printLn(log);
        }
    }

    private File getFinalWorkingDirectory() {
        return getWorkingDirectory(this.workingDirectory);
    }

    private void initBuildTool() {
        File workingDirectory = getFinalWorkingDirectory();
        LOGGER.info("Execute 'build' command with arguments: clean={}, push={}, release={}, verbose={}, working directory={}",
                clean, push, release, verbose, workingDirectory);
        buildTool = buildService.detect(workingDirectory);
        buildTool.setWorkingDirectory(workingDirectory);
        buildTool.setClean(clean);
        buildTool.setConsole(getConsole());
    }

    private void initProject() {
        getConsole().print("Loading project").printDots();
        getConsole().execute(() -> loadProject(buildTool));
    }

    private void doExecute() {
        BuildExecution execution = startExecuting();
        completeExecution(execution);
    }

    private void update() {
        getConsole().print("updating").printDots();
        Scm scm = buildTool.getScm(project);
        Scm.Update update = getConsole().execute(() -> scm.update(project));
        if (update.hasChanges()) {
            getConsole().print("pulled changes (");
            getConsole().print(update.getFileCount() + "/" + update.getInsertionCount() + "/" + update.getDeletionCount());
            getConsole().print(")");
        }
        project = buildTool.getProject();
    }

    private void loadProject(BuildTool buildTool) {
        project = buildTool.getProject(getFinalWorkingDirectory());
    }
}
