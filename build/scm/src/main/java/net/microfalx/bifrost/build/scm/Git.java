package net.microfalx.bifrost.build.scm;

import net.microfalx.bifrost.api.Project;
import net.microfalx.lang.JvmUtils;
import org.springframework.stereotype.Component;

@Component
public class Git extends Scm {

    public Git() {
        super("git", "Git");
    }

    @Override
    public void download(Project project) {

    }

    @Override
    public void status(Project project) {

    }

    @Override
    public void commit(Project project) {

    }

    @Override
    public void revert(Project project) {

    }

    @Override
    protected String getExecutable() {
        return JvmUtils.isWindows() ? "git.exe" : "git";
    }

    @Override
    protected String[] getFiles() {
        return new String[]{".git"};
    }
}
