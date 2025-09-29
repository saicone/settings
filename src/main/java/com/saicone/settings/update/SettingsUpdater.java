package com.saicone.settings.update;

import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to handle node updates into provided map nodes.
 *
 * @author Rubenicos
 */
public class SettingsUpdater {

    private static final SettingsUpdater SIMPLE = new SettingsUpdater(Collections.unmodifiableList(new ArrayList<>())) {
        @Override
        public boolean update(@NotNull MapNode base, @Nullable MapNode provider) {
            if (provider != null && !base.equals(provider)) {
                base.deepMerge(provider.getValue());
                return true;
            }
            return false;
        }
    };

    private List<NodeUpdate> nodeUpdates;

    /**
     * Get a simple settings updater that only puts any non-existent node from provider map into base map node.
     *
     * @return a settings updater.
     */
    @NotNull
    public static SettingsUpdater simple() {
        return SIMPLE;
    }

    /**
     * Constructs an empty settings updater.
     */
    public SettingsUpdater() {
        this(null);
    }

    /**
     * Constructs a settings updater with provided node updates.
     *
     * @param nodeUpdates the node updates to apply into base map node.
     */
    public SettingsUpdater(@Nullable List<NodeUpdate> nodeUpdates) {
        this.nodeUpdates = nodeUpdates;
    }

    /**
     * Update the given base node.
     *
     * @param base     the base node to apply updates into.
     * @return         true if any update has been applied into base node.
     */
    public boolean update(@NotNull MapNode base) {
        return update(base, null);
    }

    /**
     * Update the given base node along with optional provider map node.
     *
     * @param base     the base node to apply updates into.
     * @param provider the provider map node.
     * @return         true if any update has been applied into base node.
     */
    public boolean update(@NotNull MapNode base, @Nullable MapNode provider) {
        boolean result = false;
        for (NodeUpdate nodeUpdate : getNodeUpdates()) {
            if (nodeUpdate.apply(base)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get every node update from this class.
     *
     * @return a list of node updates.
     */
    @NotNull
    public List<NodeUpdate> getNodeUpdates() {
        if (this.nodeUpdates == null) {
            final List<NodeUpdate> list = new ArrayList<>();
            for (Class<?> clazz = getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    final Object object;
                    try {
                        field.setAccessible(true);
                        object = field.get(null);
                    } catch (IllegalAccessException e) {
                        continue;
                    }

                    if (object instanceof NodeUpdate) {
                        list.add((NodeUpdate) object);
                    }
                }
            }
            this.nodeUpdates = list;
        }
        return nodeUpdates;
    }

    /**
     * Add a node update to this instance.
     *
     * @param update the node update to add.
     */
    public void add(@NotNull NodeUpdate update) {
        getNodeUpdates().add(update);
    }
}