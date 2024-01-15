package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class SettingsSource {

    private static final Map<String, String> EXTENSIONS = new LinkedHashMap<>();

    static {
        EXTENSIONS.put("conf", "hocon");
        EXTENSIONS.put("yaml", "yaml");
        EXTENSIONS.put("yml", "yaml");
    }

    @NotNull
    public static String getExtension(@NotNull String type) {
        for (Map.Entry<String, String> entry : EXTENSIONS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(type)) {
                return entry.getKey();
            }
        }
        return type.toLowerCase();
    }

    @NotNull
    public static String getDataType(@NotNull String suffix) {
        return EXTENSIONS.getOrDefault(suffix.toLowerCase(), suffix.toLowerCase());
    }

    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        throw new IllegalStateException("Cannot read settings with " + getClass().getName());
    }

    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        throw new IllegalStateException("Cannot write settings with " + getClass().getName());
    }
}
