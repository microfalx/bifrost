package net.microfalx.bifrost.build;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Holds a build line.
 */
@Getter
@Setter
@ToString
public class BuildLine {

    private final Type type;
    private final String line;
    private final int index;

    private String name;

    public BuildLine(Type type, String line, int index) {
        requireNonNull(type);
        requireNonNull(line);
        this.type = type;
        this.line = line;
        this.index = index;
    }

    public enum Type {
        MODULE,
        PLUGIN,
        TEXT
    }
}
