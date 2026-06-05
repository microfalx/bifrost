package net.microfalx.bifrost.build;

import net.microfalx.bootstrap.cli.command.RunnableCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

import static net.microfalx.lang.StringUtils.isNotEmpty;

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

    @Override
    protected void execute() throws IOException {
        File workingDirectory = getWorkingDirectory();
        if (isNotEmpty(this.workingDirectory)) {
            workingDirectory = new File(this.workingDirectory);
            if (!workingDirectory.exists()) {
                printLn("The project directory '" + workingDirectory + "' does not exist");
                return;
            }
        }
        BuildTool buildTool = buildService.detect(workingDirectory);
        buildTool.setWorkingDirectory(workingDirectory);
        if (!push) {
            printLn("Building project");
            buildTool.build();
        } else {
            if (release) {
                printLn("Releasing project");
                buildTool.release();
            } else {
                printLn("Building and deploy project");
                buildTool.deploy();
            }
        }
    }
}
