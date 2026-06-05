package net.microfalx.bifrost.cli;

import net.microfalx.bootstrap.cli.CliService;
import net.microfalx.bootstrap.configuration.annotation.EnableConfigurationMapping;
import net.microfalx.bootstrap.core.utils.BootUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.bifrost"})
@EnableConfigurationMapping({"net.microfalx.bootstrap", "net.microfalx.bifrost"})
public class BifrostApplication implements CommandLineRunner {

    @Autowired private CliService cliService;

    @Override
    public void run(String... args) throws Exception {
        int exitCode = cliService.execute(args);
        // do not say there is a problem because there is no arguments; it prints the help, that's not an error
        if (exitCode != 0 && args.length == 0) exitCode = 0;
        if (exitCode != 0) System.exit(exitCode);
    }

    public static void main(String[] args) {
        BootUtils.asCli();
        SpringApplication.run(BifrostApplication.class, args);
    }
}
