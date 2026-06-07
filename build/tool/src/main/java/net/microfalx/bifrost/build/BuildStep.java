package net.microfalx.bifrost.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.lang.Nameable;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Represents a build step.
 */
@Getter
@Setter
@ToString
public class BuildStep implements Nameable {

    private final String name;
    private String module;

    private StringBuilder logs = new StringBuilder();

    public BuildStep(String name) {
        requireNotEmpty(name);
        this.name = name;
    }

    public void add(String line) {
        requireNonNull(line);
        if (!logs.isEmpty()) logs.append("\n");
        logs.append(line);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public String getLogs() {
        return logs.toString();
    }


}
