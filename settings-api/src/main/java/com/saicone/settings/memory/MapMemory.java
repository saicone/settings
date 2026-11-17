package com.saicone.settings.memory;

import com.saicone.settings.SettingsMemory;
import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to save settings nodes into Java map.
 *
 * @author Rubenicos
 */
public class MapMemory implements SettingsMemory {

    private final Map<String, SettingsNode> map;

    /**
     * Constructs a map memory to save nodes.
     */
    public MapMemory() {
        this(new HashMap<>());
    }

    /**
     * Constructs a map memory to save node into provided map.
     *
     * @param map the map to save nodes.
     */
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
