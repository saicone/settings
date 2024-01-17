package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import com.saicone.settings.update.SettingsUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsLoader {

    private static final SettingsLoader EMPTY = new SettingsLoader();
    private static final SettingsLoader SIMPLE = new SettingsLoader(SettingsUpdater.simple(), SettingsParser.simple());
    private static final SettingsLoader ALL = new SettingsLoader(SettingsUpdater.simple(), SettingsParser.all());

    private final SettingsUpdater updater;
    private final SettingsParser parser;

    @NotNull
    public static SettingsLoader empty() {
        return EMPTY;
    }

    @NotNull
    public static SettingsLoader simple() {
        return SIMPLE;
    }

    @NotNull
    public static SettingsLoader all() {
        return ALL;
    }

    public SettingsLoader() {
        this(null, null);
    }

    public SettingsLoader(@Nullable SettingsUpdater updater) {
        this(updater, null);
    }

    public SettingsLoader(@Nullable SettingsParser parser) {
        this(null, parser);
    }

    public SettingsLoader(@Nullable SettingsUpdater updater, @Nullable SettingsParser parser) {
        this.updater = updater;
        this.parser = parser;
    }

    @Nullable
    public SettingsUpdater getUpdater() {
        return updater;
    }

    @Nullable
    public SettingsParser getParser() {
        return parser;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends MapNode> T load(@NotNull SettingsData<T> provider) {
        T node = provider.load();
        if (updater != null) {
            node = updater.update(node, provider.getOptionalLoaded());
        }
        if (parser != null) {
            node = (T) parser.parse(node);
        }
        return node;
    }
}
