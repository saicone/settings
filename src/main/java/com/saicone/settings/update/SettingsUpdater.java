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
        public <T extends MapNode> @NotNull T update(@NotNull T base, @Nullable MapNode provider) {
            if (provider != null) {
                base.deepMerge(provider.getValue());
            }
            return base;
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
     * Update the given base node along with optional provider map node.
     *
     * @param base     the base node to apply updates into.
     * @param provider the provider map node.
     * @return         the effective map node in this operation, normally the same base node.
     * @param <T>      the map node type.
     */
    @NotNull
    public <T extends MapNode> T update(@NotNull T base, @Nullable MapNode provider) {
        T baseResult = base;
        for (NodeUpdate nodeUpdate : getNodeUpdates()) {
            baseResult = nodeUpdate.apply(baseResult);
        }
        return baseResult;
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
}