package com.saicone.settings.data;

import com.saicone.settings.SettingsSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class DataFormat {

    private static final Map<String, String> EXTENSIONS = new LinkedHashMap<>();
    private static final Map<String, Class<? extends SettingsSource>> SOURCE_TYPES = new HashMap<>();

    static {
        EXTENSIONS.put("conf", "hocon");
        EXTENSIONS.put("json", "json");
        EXTENSIONS.put("toml", "toml");
        EXTENSIONS.put("yaml", "yaml");
        EXTENSIONS.put("yml", "yaml");
        try {
            SOURCE_TYPES.put("json", Class.forName("com.saicone.settings.source.GsonSettingsSource").asSubclass(SettingsSource.class));
        } catch (ClassNotFoundException ignored) { }
        try {
            SOURCE_TYPES.put("hocon", Class.forName("com.saicone.settings.source.HoconSettingsSource").asSubclass(SettingsSource.class));
        } catch (ClassNotFoundException ignored) { }
        try {
            SOURCE_TYPES.put("toml", Class.forName("com.saicone.settings.source.TomlSettingsSource").asSubclass(SettingsSource.class));
        } catch (ClassNotFoundException ignored) { }
        try {
            SOURCE_TYPES.put("yaml", Class.forName("com.saicone.settings.source.YamlSettingsSource").asSubclass(SettingsSource.class));
        } catch (ClassNotFoundException ignored) { }
    }

    DataFormat() {
    }

    @NotNull
    public static String getExtension(@NotNull String format) {
        for (Map.Entry<String, String> entry : EXTENSIONS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(format)) {
                return entry.getKey();
            }
        }
        return format.toLowerCase();
    }

    @NotNull
    public static Set<String> getExtensions() {
        return Collections.unmodifiableSet(EXTENSIONS.keySet());
    }

    @NotNull
    public static String getFormat(@NotNull String extension) {
        return EXTENSIONS.getOrDefault(extension.toLowerCase(), extension.toLowerCase());
    }

    @NotNull
    public static SettingsSource getSource(@NotNull String type) {
        final String format = getFormat(type);
        if (!SOURCE_TYPES.containsKey(format)) {
            throw new IllegalArgumentException("Cannot find SettingsSource for '" + type + "' with format '" + format + "', consider using your own implementation");
        }
        try {
            return SOURCE_TYPES.get(type).getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot initialize the SettingsSource " + SOURCE_TYPES.get(type).getName() + " with reflection, consider using your own implementation", e);
        }
    }

    @Nullable
    public static Class<? extends SettingsSource> addSource(@NotNull String format, @NotNull Class<? extends SettingsSource> clazz) {
        return SOURCE_TYPES.put(format, clazz);
    }
}
