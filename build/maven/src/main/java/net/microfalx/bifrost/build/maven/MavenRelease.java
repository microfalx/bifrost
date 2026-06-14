package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Artifact;
import net.microfalx.bifrost.build.BuildException;
import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildTool;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bootstrap.cli.CliException;
import net.microfalx.bootstrap.cli.util.Console;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import static net.microfalx.lang.IOUtils.appendStream;
import static net.microfalx.lang.IOUtils.getBufferedWriter;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.TextUtils.insertSpaces;

/**
 * A class responsible to release a Maven projects.
 */
public class MavenRelease extends BuildExecution {

    private Scm cachedScm;
    private String errorMessage;

    private Version gaVersion;
    private String tag;
    private Version developmentVersion;

    private Set<String> tags = Collections.emptySet();

    public MavenRelease(BuildTool tool) {
        super(tool, "mvn release");
    }

    @Override
    public int waitFor() {
        int exitCode = validate();
        if (exitCode != 0) return exitCode;
        exitCode = updateGaVersionForProject();
        if (exitCode != 0) return exitCode;
        exitCode = updateGaVersionForThirdParties();
        if (exitCode != 0) return exitCode;
        exitCode = buildProject();
        if (exitCode != 0) return exitCode;
        if (!confirm()) return -1;
        exitCode = commitAndTag();
        if (exitCode != 0) return exitCode;
        exitCode = updateDevelopmentVersionForProject();
        if (exitCode != 0) return exitCode;
        exitCode = commit(VersionType.NEXT_PATCH);
        if (exitCode != 0) return exitCode;
        exitCode = releaseProject();
        return exitCode;
    }

    @Override
    public String getFirstAndLastStepLogs() {
        return defaultIfEmpty(errorMessage, "No additional information is available");
    }

    private int validate() {
        getConsole().printDots().printLn("prepare and validate:");
        tags = getScm().getTags(getProjectOrFail());
        return 0;
    }

    private boolean confirm() {
        if (getTool().isYes()) return true;
        getConsole().printTab().printBullet().print("Validation is successful, release? (y/n): ");
        String answer = getConsole().readLine().toLowerCase();
        boolean yes = "yes".equals(answer) || "y".equals(answer);
        if (!yes) {
            getConsole().printWarning("Release was cancelled").printLn();
        }
        return yes;
    }

    private int updateGaVersionForProject() {
        Version parsedVersion = Version.parse(getProjectOrFail().getVersion().orElseThrow());
        gaVersion = updateVersion(parsedVersion, VersionType.GA);
        getConsole().printTab().printBullet()
                .print("Update project version to GA (" + gaVersion + ")").printDots();
        return updateVersion(gaVersion);
    }

    private int updateDevelopmentVersionForProject() {
        developmentVersion = updateVersion(gaVersion, VersionType.NEXT_PATCH);
        getConsole().printTab().printBullet()
                .print("Update project version for next development cycle  (" + developmentVersion + ")").printDots();
        return updateVersion(developmentVersion);
    }

    private int updateGaVersionForThirdParties() {
        getConsole().printTab().printBullet().print("Update third parties versions to GA").printDots();
        ProcessLauncher launcher = createLauncher().addArgument("-DgenerateBackupPoms=false")
                .addArgument("-DfailIfNotReplaced=true").addArgument("-DallowRangeMatching=true")
                .addArgument("versions:use-releases");
        int exitCode = handleExitCode(execute(launcher));
        if (exitCode != 0) return exitCode;
        getConsole().printTab().printBullet().print("Update third parties version from properties to GA").printDots();
        launcher = createLauncher().addArgument("-DgenerateBackupPoms=false").addArgument("-DallowMajorUpdates=false")
                .addArgument("versions:update-properties");
        return handleExitCode(execute(launcher));
    }

    private int buildProject() {
        getConsole().printTab().printBullet().print("Build project").printDots();
        return handleExitCode(getTool().build(true));
    }

    private int releaseProject() {
        getConsole().printTab().printBullet().print("Release project").printDots();
        return handleExitCode(execute(() -> {
            File workspace = getScm().download(getProjectOrFail(), new Scm.Options().setTag(tag));
            BuildTool currentTool = getTool();
            File currentWorkingDirectory = currentTool.getWorkingDirectory();
            try {
                currentTool.setWorkingDirectory(workspace);
                return currentTool.deploy(true).waitFor();
            } finally {
                currentTool.setWorkingDirectory(currentWorkingDirectory);
            }
        }));
    }

    private int commitAndTag() {
        writeSignature();
        int exitCode = commit(VersionType.GA);
        if (exitCode != 0) return exitCode;
        return tag();
    }

    private int commit(VersionType version) {
        String message = switch (version) {
            case GA -> "Release " + gaVersion;
            case NEXT_PATCH -> "Next release " + developmentVersion.toMaven();
            case NEXT_MAJOR, NEXT_MINOR -> "Next development " + developmentVersion.toMaven();
        };
        getConsole().printTab().printBullet().print("Commit").printDots();
        return handleRunnable(() -> {
            Scm scm = getScm();
            scm.commit(getProjectOrFail(), message, true);
        }, version != VersionType.GA);
    }

    private int tag() {
        getConsole().printDots().print("Tag").printDots();
        tag = "v" + gaVersion.toString();
        return handleRunnable(() -> {
            Scm scm = getScm();
            scm.tag(getProjectOrFail(), tag, "Release " + gaVersion);
        });
    }

    private Version updateVersion(Version version, VersionType versionType) {
        Version nextVersion = switch (versionType) {
            case GA -> version.withSnapshot(false);
            case NEXT_PATCH -> version.withPatch(version.getPatch() + 1).withSnapshot(true);
            case NEXT_MINOR -> version.withMinor(version.getMinor() + 1).withPatch(0).withSnapshot(true);
            case NEXT_MAJOR -> version.withMajor(version.getMajor() + 1).withMinor(0).withPatch(0).withSnapshot(true);
        };
        if (isAlwaysGa()) nextVersion = nextVersion.withSnapshot(false);
        return nextVersion;
    }

    private boolean isAlwaysGa() {
        Artifact artifact = getProjectOrFail().getArtifact().orElseThrow();
        String artifactId = artifact.getArtifactId();
        return Artifact.TYPE_POM.equals(artifact.getType()) && (artifactId.equals("bom") || artifactId.equals("pom")
                || artifactId.endsWith("-pom") || artifactId.endsWith("-bom"));
    }

    private int updateVersion(Version version) {
        ProcessLauncher launcher = createLauncher().addArgument("-DgenerateBackupPoms=false")
                .addArgument("-DnewVersion=" + version.toMaven())
                .addArgument("versions:set");
        return handleExitCode(execute(launcher));
    }

    private ProcessLauncher createLauncher() {
        ProcessLauncher launcher = getTool().createLauncher();
        launcher.addArgument("-Dtalos.quiet=false");
        setLauncher(launcher);
        return launcher;
    }

    private Scm getScm() {
        if (cachedScm == null) cachedScm = getTool().getScm(getProjectOrFail());
        return cachedScm;
    }

    private void writeSignature() {
        File file = new File(getTool().getWorkingDirectory(), ".bifrost");
        String signature = "Release Time: " + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now());
        try {
            appendStream(getBufferedWriter(file), new StringReader(signature));
            getScm().add(getProjectOrFail(), ".bifrost");
        } catch (IOException e) {
            throw new BuildException("Failed to write release signature");
        }
    }

    private int handleExitCode(int exitCode) {
        Console console = getConsole();
        console.printExitCode(exitCode);
        if (exitCode > 0) {
            String log = insertSpaces(getFirstAndLastStepLogs(), 2, true);
            console.printLn(log);
        }
        return exitCode;
    }

    private int handleExitCode(BuildExecution execution) {
        Console console = getConsole();
        int exitCode = execute(execution::waitFor);
        console.printExitCode(exitCode);
        if (exitCode > 0) errorMessage = execution.getFirstAndLastStepLogs();
        return exitCode;
    }

    private int handleRunnable(Runnable runnable) {
        return handleRunnable(runnable, true);
    }

    private int handleRunnable(Runnable runnable, boolean newLine) {
        Console console = getConsole();
        Throwable throwable = null;
        int exitCode;
        try {
            execute(runnable::run);
            exitCode = 0;
        } catch (Exception e) {
            throwable = e;
            exitCode = 1;
        }
        console.printExitCode(exitCode, newLine);
        if (exitCode > 0) {
            String log;
            if (throwable instanceof CliException clie) {
                log = clie.getLog();
            } else {
                log = ExceptionUtils.getRootCauseDescription(throwable);
            }
            log = insertSpaces(log, 2, true);
            console.printLn(log);
        }
        return exitCode;
    }

    private int execute(ProcessLauncher launcher) {
        return execute(launcher::run);
    }

    private <T> T execute(Supplier<T> supplier) {
        return getConsole().execute(supplier);
    }

    private void execute(Runnable runnable) {
        getConsole().execute(runnable);
    }

    enum VersionType {
        NEXT_PATCH,
        NEXT_MINOR,
        NEXT_MAJOR,
        GA
    }


}
