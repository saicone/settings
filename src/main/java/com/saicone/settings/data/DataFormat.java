package com.saicone.settings.data;

import com.saicone.settings.SettingsSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to collect supported data formats and register any settings source parser.
 *
 * @author Rubenicos
 */
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

    /**
     * Get the file extension from provided data format.
     *
     * @param format the format to get file extension.
     * @return       a file extension if found, the provided format otherwise.
     */
    @NotNull
    public static String getExtension(@NotNull String format) {
        for (Map.Entry<String, String> entry : EXTENSIONS.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(format)) {
                return entry.getKey();
            }
        }
        return format.toLowerCase();
    }

    /**
     * Get all the registered file extensions as unmodifiable set.
     *
     * @return a set of file extensions.
     */
    @NotNull
    public static Set<String> getExtensions() {
        return Collections.unmodifiableSet(EXTENSIONS.keySet());
    }

    /**
     * Get data format from provided file extension.
     *
     * @param extension a file extension.
     * @return          a registered file format if found, the provided extension instead.
     */
    @NotNull
    public static String getFormat(@NotNull String extension) {
        return EXTENSIONS.getOrDefault(extension.toLowerCase(), extension.toLowerCase());
    }

    /**
     * Get all formats with associated settings source.
     *
     * @return a set of file data formats.
     */
    @NotNull
    public static Set<String> getFormats() {
        return Collections.unmodifiableSet(SOURCE_TYPES.keySet());
    }

    /**
     * Get a new settings source instance from any registered source.
     *
     * @param type the data format or file extension.
     * @return     a newly created settings source instance.
     */
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

    /**
     * Register a settings source instance associated with data format.
     *
     * @param format the data format type.
     * @param clazz  the settings source class.
     * @return       the previous source associated with data format, or null if there was no registered source.
     */
    @Nullable
    public static Class<? extends SettingsSource> putSource(@NotNull String format, @NotNull Class<? extends SettingsSource> clazz) {
        return SOURCE_TYPES.put(format, clazz);
    }

    /**
     * Append a settings source instance associated with data format.<br>
     * This method will ignore any repeated format.
     *
     * @param format the data format type.
     * @param clazz  the settings source class.
     * @return       true if the settings source was registered.
     */
    public static boolean addSource(@NotNull String format, @NotNull Class<? extends SettingsSource> clazz) {
        if (SOURCE_TYPES.containsKey(format)) {
            return false;
        }
        SOURCE_TYPES.put(format, clazz);
        return true;
    }
}
