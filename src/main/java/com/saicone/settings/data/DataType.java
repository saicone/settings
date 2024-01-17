package com.saicone.settings.data;

import org.jetbrains.annotations.NotNull;

public enum DataType {

    FILE(true),
    URL(false),
    INPUT_STREAM(false);

    private final boolean writeable;

    DataType(boolean writeable) {
        this.writeable = writeable;
    }

    public boolean isWriteable() {
        return writeable;
    }

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
