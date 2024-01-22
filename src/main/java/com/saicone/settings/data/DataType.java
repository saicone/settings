package com.saicone.settings.data;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Data types to load from.
 *
 * @author Rubenicos
 */
public enum DataType {

    /**
     * A regular file data type.
     */
    FILE(true, "file", "local"),
    /**
     * A file that can be obtained from class loader resources.
     */
    FILE_RESOURCE(true),
    /**
     * A regular url data type.
     */
    URL(false, "url", "http", "https"),
    /**
     * A input stream that can be obtained from class loader.
     */
    INPUT_STREAM(false, "input", "stream", "inputstream", "input_stream");

    private final boolean writeable;
    private final Set<String> aliases;

    DataType(boolean writeable, @NotNull String... aliases) {
        this.writeable = writeable;
        this.aliases = new HashSet<>();
        this.aliases.addAll(Arrays.asList(aliases));
    }

    /**
     * Check if data type allows to write data into.
     *
     * @return true if data type is writeable.
     */
    public boolean isWriteable() {
        return writeable;
    }

    /**
     * Check if data is a type of file.
     *
     * @return true if data type is a file.
     */
    public boolean isFile() {
        return this == FILE || this == FILE_RESOURCE;
    }

    /**
     * Get data type aliases.
     *
     * @return a set of string aliases.
     */
    @NotNull
    public Set<String> getAliases() {
        return aliases;
    }

    /**
     * Get data type from given string by finding any header.
     *
     * @param s the string to analyze.
     * @return  a data type.
     */
    @NotNull
    public static DataType of(@NotNull String s) {
        final int index = s.indexOf(':');
        final String type = index > 0 ? s.substring(0, index) : s;
        switch (type.replace(' ', '_').toLowerCase()) {
            case "url":
            case "http":
            case "https":
                return DataType.URL;
            case "input":
            case "stream":
            case "inputstream":
            case "input_stream":
                return DataType.INPUT_STREAM;
            case "file":
            case "local":
            default:
                return DataType.FILE;
        }
    }
}
