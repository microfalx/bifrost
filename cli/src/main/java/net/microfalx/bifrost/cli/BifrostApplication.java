package net.microfalx.bifrost.cli;

import net.microfalx.bootstrap.configuration.annotation.EnableConfigurationMapping;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.bifrost"})
@EnableConfigurationMapping({"net.microfalx.bootstrap", "net.microfalx.bifrost"})
public class BifrostApplication implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running Bifrost CLI with arguments: " + String.join(" ", args));
    }

    public static void main(String[] args) {
        SpringApplication.run(BifrostApplication.class, args);
    }
}
