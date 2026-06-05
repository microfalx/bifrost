package net.microfalx.bifrost.build.scm;

import net.microfalx.bootstrap.cli.command.RunnableCommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.IOException;

@Component
@CommandLine.Command(name = "scm", mixinStandardHelpOptions = true,
        description = "Source Code Management")
public class ScmCommand extends RunnableCommand {

    @Override
    protected void execute() throws IOException {

    }
}
