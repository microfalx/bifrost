package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.cli.command.RunnableCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

@Component
@CommandLine.Command(name = "scm", mixinStandardHelpOptions = true,
        description = "Source Code Management")
public class ScmCommand extends RunnableCommand {

    @Autowired private ScmService scmService;

    @CommandLine.Option(names = {"-c", "--cleanup"}, description = "Purges state related to all repositories")
    private boolean cleanup;

    @CommandLine.Option(names = {"-d", "--directroy"}, description = "The directory where the project is located (instead of the working directory")
    private String workingDirectory;

    @Override
    protected void execute() throws IOException {
        Scm scm = scmService.detect(getFinalWorkingDirectory());
        Project project = scmService.getProject(getFinalWorkingDirectory());
        getConsole().print("SCM details for project ")
                .printQuote().printBold(project.getName()).printQuote().printLn(":");
        getConsole().increaseIndent();
        getConsole().printList().print("Remote: ").printLn(project.getRepository().getUri());
        printBranches(scm, project);
        printTags(scm, project);
        getConsole().decreaseIndent();
    }

    private void printBranches(Scm scm, Project project) {
        getConsole().printList().printLn("Branches:");
        getConsole().increaseIndent();
        Set<String> branches = new TreeSet<>(scm.getBranches(project));
        for (String branch : branches) {
            getConsole().printList().printLn(branch);
        }
        getConsole().decreaseIndent();
    }

    private void printTags(Scm scm, Project project) {
        getConsole().printList().printLn("Tags:");
        getConsole().increaseIndent();
        Set<String> branches = new TreeSet<>(scm.getTags(project));
        for (String branch : branches) {
            getConsole().printList().printLn(branch);
        }
        getConsole().decreaseIndent();
    }

    private File getFinalWorkingDirectory() {
        return getWorkingDirectory(this.workingDirectory);
    }
}
