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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.IOUtils.appendStream;
import static net.microfalx.lang.IOUtils.getBufferedWriter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@Prepare(mocks = ResourceService.class)
class GitTest extends ServiceUnitTestCase {

    private static final String TEST_BRANCH = "release_test";

    @Mock
    private ScmService service;

    @Subject
    private Git git;

    private Project project;
    private File workspace;

    @BeforeEach
    void setup() {
        project = Project.builder("bootstrap-demo")
                .repository(Project.Repository.git("git@github.com:adrian-tarau/bootstrap-demo.git"))
                .build();
        workspace = new File(JvmUtils.getTemporaryDirectory(), "bootstrap-demo-" + getId());
        doReturn(workspace).when(service).getWorkspace(any(Project.class));
        git.scmService = service;
    }

    @Test
    void getCurrentBranch() {
        String currentBranch = git.getCurrentBranch(project);
        assertEquals("main", currentBranch);
    }

    @Test
    void getBranches() {
        Set<String> branches = git.getBranches(project);
        assertThat(branches.size()).isEqualTo(1);
        assertThat(branches).containsAnyOf("1.0");
    }

    @Test
    void getTags() {
        Set<String> tags = git.getTags(project);
        assertThat(tags.size()).isEqualTo(1);
        assertThat(tags).containsAnyOf("1.0.0");
    }

    @Test
    void downloadFully() {
        git.download(project);
        assertWorkspace();
    }

    @Test
    void downloadOnlyBranch() {
        git.download(project, new Scm.Options().setBranch("1.0"));
        assertWorkspace();
    }

    @Test
    void downloadOnlyTag() {
        git.download(project, new Scm.Options().setTag("v1.0.0"));
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

    @Test
    void commitWithNoChanges() {
        Assertions.assertThatThrownBy(() -> {
            git.commit(project, "Test commit", true);
        }).isInstanceOf(ScmException.class);
        assertWorkspace();
    }

    @Test
    void commitWithChanges() throws IOException {
        switchToTestBranch();
        appendStream(getBufferedWriter(getFileInWorkspace("test/unit_test.txt")), new StringReader(Long.toString(currentTimeMillis())));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        git.commit(project, "Test commit " + formatter.format(LocalDateTime.now()), true);
        assertWorkspace();
    }

    @Test
    void tag() {
        switchToTestBranch();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        git.tag(project, "v" + formatter.format(LocalDateTime.now()),
                "Test tag " + formatter.format(LocalDateTime.now()));
    }

    @Test
    void add() throws IOException {
        switchToTestBranch();
        appendStream(getBufferedWriter(getFileInWorkspace("test/unit_test_added.txt")), new StringReader(Long.toString(currentTimeMillis())));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        git.add(project, "test/unit_test_added.txt");
        git.commit(project, "Test added file " + formatter.format(LocalDateTime.now()), true);
        assertWorkspace();
    }

    @Test
    @Disabled
    void manualTag() {
        switchToTestBranch();
        git.tag(project, "v1.0.0", "Manual tag");
    }

    private void assertWorkspace() {
        File workspace = git.getWorkspace(project);
        assertThat(workspace.exists()).isTrue();
        File[] files = ObjectUtils.defaultIfNull(workspace.listFiles(), new File[0]);
        assertThat(files.length).isGreaterThan(5);
    }

    private void switchToTestBranch() {
        git.checkout(project.withBranch(TEST_BRANCH));
        assertEquals(TEST_BRANCH, git.getCurrentBranch(project));
    }

    private File getFileInWorkspace(String path) {
        return new File(workspace, path.replace('/', File.separatorChar));
    }

    private static String getId() {
        return Long.toString(currentTimeMillis(), Character.MAX_RADIX)
                + "-" + IdGenerator.get().nextAsString();
    }

}