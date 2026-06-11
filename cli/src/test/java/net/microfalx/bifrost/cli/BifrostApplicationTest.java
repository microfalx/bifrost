package net.microfalx.bifrost.cli;

import net.microfalx.bifrost.build.BuildService;
import net.microfalx.bootstrap.application.ApplicationConfiguration;
import net.microfalx.bootstrap.application.ApplicationService;
import net.microfalx.bootstrap.cli.CliService;
import net.microfalx.bootstrap.core.async.AsynchronousConfiguration;
import net.microfalx.bootstrap.core.i18n.I18nProperties;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.resource.ResourceProperties;
import net.microfalx.bootstrap.resource.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@ContextConfiguration(classes = {I18nService.class, ResourceService.class, ResourceProperties.class,
        OptionalValidatorFactoryBean.class, ApplicationConfiguration.class, ApplicationService.class,
        BuildService.class, CliService.class})
@Import({I18nProperties.class, AsynchronousConfiguration.class})
@OverrideAutoConfiguration(enabled = false)
@ImportAutoConfiguration
@SpringBootTest
class BifrostApplicationTest {

    @Autowired CliService cliService;

    @Test
    void build() {
        cliService.execute("build");
    }

    @Test
    void deploy() {
        cliService.execute("build -p");
    }

}