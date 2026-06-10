package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Prepare;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.lang.IdGenerator;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.ObjectUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Prepare(mocks = ResourceService.class)
class GitTest extends ServiceUnitTestCase {

    @Mock
    private ScmService service;

    @Subject
    private Git git;

    private Project project;

    @BeforeEach
    void setup() {
        project = Project.builder("bootstrap-demo").repository("https://github.com/adrian-tarau/bootstrap-demo.git").build();
        File workspace = new File(JvmUtils.getTemporaryDirectory(), "bootstrap-demo-" + IdGenerator.get().nextAsString());
        doReturn(workspace).when(service).getWorkspace(any(Project.class));
        git.scmService = service;
    }

    @Test
    void download() {
        git.download(project);
        assertWorkspace();
    }

    @Test
    void checkout() {
        git.checkout(project);
        assertWorkspace();
    }

    @Test
    void restore() {
        git.restore(project);
        assertWorkspace();
    }

    private void assertWorkspace() {
        File workspace = git.getWorkspace(project);
        Assertions.assertThat(workspace.exists()).isTrue();
        File[] files = ObjectUtils.defaultIfNull(workspace.listFiles(), new File[0]);
        Assertions.assertThat(files.length).isGreaterThan(5);
    }

}