package net.microfalx.bifrost.build.maven;

import net.microfalx.bifrost.api.Project;
import net.microfalx.bifrost.build.BuildExecution;
import net.microfalx.bifrost.build.BuildTool;
import net.microfalx.bootstrap.cli.util.Console;
import net.microfalx.bootstrap.core.process.ProcessLauncher;
import net.microfalx.lang.Version;

import static net.microfalx.lang.TextUtils.insertSpaces;

/**
 * A class responsible to release a Maven projects.
 */
public class MavenRelease extends BuildExecution {

    public MavenRelease(BuildTool tool) {
        super(tool, "mvn release");
    }

    @Override
    public int waitFor() {
        int exitCode = updateVersion(VersionType.GA);
        if (exitCode != 0) return exitCode;
        return 0;
    }

    private int updateVersion(VersionType versionType) {
        getConsole().printTab().printBullet().print("Update to GA version").printDots();
        Project project = getProject().orElseThrow();
        Version parsedVersion = Version.parse(project.getVersion().orElseThrow());
        Version version = updateVersion(parsedVersion, versionType);
        ProcessLauncher launcher = createLauncher().addArgument("-DgenerateBackupPoms=false")
                .addArgument("-Dtalos.quiet=false")
                .addArgument("-DnewVersion=" + version.toString())
                .addArgument("versions:set");
        return handleExitCode(launcher.run());
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

    private int handleExitCode(int exitCode) {
        Console console = getConsole();
        console.printExitCode(exitCode).printLn();
        if (exitCode > 0) {
            String log = insertSpaces(getLastStep().getLogs(), 2, true);
            console.printLn(log);
        }
        return exitCode;
    }

    enum VersionType {
        GA,
        NEXT_MINOR,
        NEXT_MAJOR
    }


}
