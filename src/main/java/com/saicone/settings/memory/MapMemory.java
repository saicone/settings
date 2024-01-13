package com.saicone.settings.memory;

import com.saicone.settings.SettingsMemory;
import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MapMemory implements SettingsMemory {

    private final Map<String, SettingsNode> map;

    public MapMemory() {
        this(new HashMap<>());
    }

    public MapMemory(@NotNull Map<String, SettingsNode> map) {
        this.map = map;
    }

    @Override
    public @Nullable SettingsNode get(@NotNull String id) {
        return map.get(id);
    }

    @Override
    public void save(@NotNull String id, @NotNull SettingsNode node) {
        map.put(id, node);
    }

    @Override
    public void remove(@NotNull String id) {
        map.remove(id);
    }

    @Override
    @SuppressWarnings("all")
    public void remove(@NotNull SettingsNode node) {
        while (map.values().remove(node)) {
            /** intentionally empty */
        }
    }

    @Override
    public void clear() {
        map.clear();
    }
}
