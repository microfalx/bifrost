package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildTool;
import net.microfalx.bifrost.build.scm.Scm;
import net.microfalx.bootstrap.cli.CliException;
import net.microfalx.bootstrap.cli.util.Console;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Version;

import java.util.function.Supplier;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;
import static net.microfalx.lang.TextUtils.insertSpaces;

/**
 * A class responsible to release a Maven projects.
 */
public class MavenRelease extends BuildExecution {

    private Scm cachedScm;
    private String errorMessage;

    public MavenRelease(BuildTool tool) {
        super(tool, "mvn release");
    }

    @Override
    public int waitFor() {
        int exitCode = validate();
        if (exitCode != 0) return exitCode;
        exitCode = switchToBranch();
        reloadProject();
        if (exitCode != 0) return exitCode;
        exitCode = updateGaVersionForProject(VersionType.GA);
        if (exitCode != 0) return exitCode;
        exitCode = updateGaVersionForThirdParties();
        if (exitCode != 0) return exitCode;
        exitCode = buildProject();
        if (exitCode != 0) return exitCode;
        return exitCode;
    }

    @Override
    public String getFirstAndLastStepLogs() {
        return defaultIfEmpty(errorMessage, "No additional information is available");
    }

    private int validate() {
        getConsole().printDots().printLn("prepare and validate:");
        String branch = getProjectOrFail().getBranch();
        if ("main".equals(branch)) {
            errorMessage = "A release cannot be performed from 'main' branch";
            return 1;
        }
        return 0;
    }

    private int switchToBranch() {
        getConsole().printTab().printBullet().print("Switch project to branch ").printQuote()
                .printBold(getProjectOrFail().getBranch()).printQuote().printDots();
        Scm scm = createScm();
        return handleRunnable(() -> execute(() -> scm.checkout(getProjectOrFail())));
    }

    private void reloadProject() {
        setProject(getTool().getProject());
    }

    private int updateGaVersionForProject(VersionType versionType) {
        Version parsedVersion = Version.parse(getProjectOrFail().getVersion().orElseThrow());
        Version version = updateVersion(parsedVersion, versionType);
        getConsole().printTab().printBullet()
                .print("Update project version to GA (" + version + ")").printDots();
        ProcessLauncher launcher = createLauncher().addArgument("-DgenerateBackupPoms=false")
                .addArgument("-Dtalos.quiet=false").addArgument("-DnewVersion=" + version.toString())
                .addArgument("versions:set");
        return handleExitCode(execute(launcher));
    }

    private int updateGaVersionForThirdParties() {
        getConsole().printTab().printBullet().print("Update third parties versions to GA").printDots();
        ProcessLauncher launcher = createLauncher().addArgument("-DgenerateBackupPoms=false")
                .addArgument("-Dtalos.quiet=false").addArgument("-DfailIfNotReplaced=true")
                .addArgument("-DallowRangeMatching=true")
                .addArgument("versions:use-releases");
        return handleExitCode(execute(launcher));
    }

    private int buildProject() {
        getConsole().printTab().printBullet().print("Build project").printDots();
        return handleExitCode(execute(() -> {
            BuildExecution execution = getTool().build();
            return execution.waitFor();
        }));
    }

    private Version updateVersion(Version version, VersionType versionType) {
        return switch (versionType) {
            case GA -> version.withSnapshot(false);
            case NEXT_MAJOR -> version.withMajor(version.getMajor() + 1).withMinor(0).withPatch(0).withSnapshot(true);
            case NEXT_MINOR -> version.withMinor(version.getMinor() + 1).withPatch(0).withSnapshot(true);
        };
    }

    private ProcessLauncher createLauncher() {
        ProcessLauncher launcher = getTool().createLauncher();
        setLauncher(launcher);
        return launcher;
    }

    private Scm createScm() {
        if (cachedScm == null) cachedScm = getTool().getScm(getProjectOrFail());
        return cachedScm;
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

    private int handleRunnable(Runnable runnable) {
        Console console = getConsole();
        Throwable throwable = null;
        int exitCode;
        try {
            runnable.run();
            exitCode = 0;
        } catch (Exception e) {
            throwable = e;
            exitCode = 1;
        }
        console.printExitCode(exitCode);
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
        GA,
        NEXT_MINOR,
        NEXT_MAJOR
    }


}
