package net.microfalx.bifrost.build;

import net.microfalx.bifrost.build.scm.ScmService;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.test.ServiceIntegrationTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {ResourceService.class, RegistryService.class,
        ScmService.class, BuildService.class, TestBuildTool.class})
public class BuildServiceIntegrationTest extends ServiceIntegrationTestCase {

    @Autowired private BuildService buildService;

    @Test
    void getTools() {
        assertEquals(1, buildService.getTools().size());
    }
}
