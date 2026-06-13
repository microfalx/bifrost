package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.JvmUtils;
import net.microfalx.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.microfalx.lang.NumberUtils.toNumber;

@Component
public class Git extends Scm {

    public Git() {
        super("git", "Git");
    }

    @Override
    public boolean add(Project project, String path) {
        downloadIfRequired(project);
        update(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("add").addArgument(path);
        execute(launcher);
        return true;
    }

    @Override
    public String getCurrentBranch(Project project) {
        downloadIfRequired(project);
        update(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("branch").addArgument("--show-current");
        String output = StringUtils.trim(execute(launcher).getLogsAsString());
        if (StringUtils.isEmpty(output)) {
            throw new ScmException("Empty branch name for " + project.getName());
        }
        return output;
    }

    @Override
    public Set<String> getBranches(Project project) {
        downloadIfRequired(project);
        update(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("branch").addArgument("-r");
        String output = execute(launcher).getLogsAsString();
        return output.lines().map(this::getVersionFromBranchOutput)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getTags(Project project) {
        downloadIfRequired(project);
        update(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("tag").addArgument("-l");
        String output = execute(launcher).getLogsAsString();
        return output.lines().map(this::getVersionFromTagOutput)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public void download(Project project) {
        ProcessLauncher launcher = createLauncher().addArgument("clone")
                .addArgument(project.getRepository())
                .addArgument(getWorkspace(project).getAbsolutePath());
        execute(launcher);
    }

    @Override
    public void checkout(Project project) {
        downloadIfRequired(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("checkout")
                .addArgument(project.getBranch());
        execute(launcher);
    }

    @Override
    public void branch(Project project, String name) {
        downloadIfRequired(project);
    }

    @Override
    public void tag(Project project, String name, String message) {
        downloadIfRequired(project);
        // first tag
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("tag").addArgument("-a")
                .addArgument("-m").addArgument(message)
                .addArgument(name);
        execute(launcher);
        // second, push to remote repo
        launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("push").addArgument("origin")
                .addArgument(name);
        execute(launcher);
    }

    @Override
    public void status(Project project) {
        downloadIfRequired(project);
    }

    @Override
    public Update update(Project project) {
        downloadIfRequired(project);
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("pull");
        execute(launcher);
        return launcher.getLogsStream()
                .map(this::extractUpdateCounts).filter(Update::isNotEmpty)
                .findFirst().orElse(new Update(0, 0, 0));
    }

    @Override
    public void commit(Project project, String message, boolean push) {
        downloadIfRequired(project);
        // commit in local repo
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("commit").addArgument("-a")
                .addArgument("-m").addArgument(message);
        execute(launcher);
        if (push) {
            // push to remote repo
            launcher = updateWorkingDirectory(createLauncher(), project)
                    .addArgument("push");
            execute(launcher);
        }
    }

    @Override
    public void restore(Project project) {
        downloadIfRequired(project);
        // undo local changes
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("restore")
                .addArgument(".");
        execute(launcher);
        // cleanup
        cleanup(project);
    }

    @Override
    public void reset(Project project) {
        downloadIfRequired(project);
        // first synchronize with remote
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("fetch").addArgument("origin");
        execute(launcher);
        // second
        launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("reset").addArgument("--hard")
                .addArgument("origin/" + project.getBranch());
        execute(launcher);
        // cleanup
        cleanup(project);
    }

    @Override
    protected String getExecutable() {
        return JvmUtils.isWindows() ? "git.exe" : "git";
    }

    @Override
    protected String[] getFiles() {
        return new String[]{".git"};
    }

    private void cleanup(Project project) {
        ProcessLauncher launcher = updateWorkingDirectory(createLauncher(), project)
                .addArgument("clean").addArgument("-df");
        execute(launcher);
    }

    private Scm.Update extractUpdateCounts(String text) {
        Matcher matcher = UPDATE_OUTPUT.matcher(text);
        if (matcher.find()) {
            String files = matcher.group(1);
            String insertions = matcher.group(2);
            String deletions = matcher.group(3);
            return new Scm.Update(toNumber(files, -1).intValue(),
                    toNumber(insertions, -1).intValue(), toNumber(deletions, -1).intValue());
        } else {
            return new Scm.Update();
        }
    }

    private void downloadIfRequired(Project project) {
        if (!hasWorkingCopy(project)) {
            download(project);
        }
    }

    private String getVersionFromBranchOutput(String line) {
        Matcher matcher = GET_BRANCH_VERSION.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private String getVersionFromTagOutput(String line) {
        Matcher matcher = GET_BRANCH_VERSION.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private static final Pattern UPDATE_OUTPUT = Pattern.compile("(?:(\\d+)\\s+file(?:s)? changed)?(?:.*(\\d+)\\s+insertion(?:s)?(?:\\(\\+\\))?)?(?:.*(\\d+)\\s+deletion(?:s)?(?:\\(\\-\\))?)?\n");
    private static final Pattern GET_BRANCH_VERSION = Pattern.compile("(?i:r|v)?(\\d+\\.\\d+.*?)(?=/|$)");
    private static final Pattern GET_TAG_VERSION = Pattern.compile("(?i:r|v)?(\\d+\\.\\d+.*?)(?=/|$)");

}
