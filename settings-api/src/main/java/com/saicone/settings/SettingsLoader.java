package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import com.saicone.settings.update.SettingsUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class to load settings data objects with any global transformations.<br>
 * This is a useful way to apply a settings parser and updater
 * together without generating unexpected results.
 *
 * @author Rubenicos
 */
public class SettingsLoader {

    private static final SettingsLoader EMPTY = new SettingsLoader();
    private static final SettingsLoader SIMPLE = new SettingsLoader(SettingsUpdater.simple(), SettingsParser.simple());
    private static final SettingsLoader ALL = new SettingsLoader(SettingsUpdater.simple(), SettingsParser.all());

    private final SettingsUpdater updater;
    private final SettingsParser parser;

    /**
     * Get an empty settings loader without any transformation.
     *
     * @return a settings loader instance.
     */
    @NotNull
    public static SettingsLoader empty() {
        return EMPTY;
    }

    /**
     * Get a simple settings loader with {@link SettingsUpdater#simple()} and {@link SettingsParser#simple()}.
     *
     * @return a settings loader instance.
     */
    @NotNull
    public static SettingsLoader simple() {
        return SIMPLE;
    }

    /**
     * Get a settings loader with all the supported transformations using {@link SettingsUpdater#simple()} and {@link SettingsParser#all()}.
     *
     * @return a settings loader instance.
     */
    @NotNull
    public static SettingsLoader all() {
        return ALL;
    }

    /**
     * Constructs an empty settings loader without any transformation.
     */
    public SettingsLoader() {
        this(null, null);
    }

    /**
     * Constructs a settings loader with provided updater.
     *
     * @param updater the updater to update data.
     */
    public SettingsLoader(@Nullable SettingsUpdater updater) {
        this(updater, null);
    }

    /**
     * Constructs a settings loader with provider parser.
     *
     * @param parser the parser to transform data.
     */
    public SettingsLoader(@Nullable SettingsParser parser) {
        this(null, parser);
    }

    /**
     * Constructs a settings loader with provided parameters.
     *
     * @param updater the updater to update data.
     * @param parser  the parser to transform data.
     */
    public SettingsLoader(@Nullable SettingsUpdater updater, @Nullable SettingsParser parser) {
        this.updater = updater;
        this.parser = parser;
    }

    /**
     * Get used settings updater in this instance.
     *
     * @return a settings updater if exists, null otherwise.
     */
    @Nullable
    public SettingsUpdater getUpdater() {
        return updater;
    }

    /**
     * Get used settings parser in this instance.
     *
     * @return a settings parser if exists, null otherwise.
     */
    @Nullable
    public SettingsParser getParser() {
        return parser;
    }

    /**
     * Load the provided settings data and return the loaded map node.
     *
     * @param provider the settings data provider.
     * @return         the loaded map node.
     * @param <T>      the map node type.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends MapNode> T load(@NotNull SettingsData<T> provider) {
        T node = provider.load();
        if (updater != null) {
            if (provider.getOptional() != null) {
                try {
                    provider.getOptional().load();
                } catch (Throwable ignored) { }
            }
            if (updater.update(node, provider.getOptionalLoaded()) && provider.getDataType().isWriteable()) {
                provider.save();
            }
        }
        if (parser != null) {
            node = (T) parser.parse(node);
        }
        provider.loaded(node);
        return node;
    }
}
