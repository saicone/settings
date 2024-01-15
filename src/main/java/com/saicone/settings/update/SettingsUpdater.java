package com.saicone.settings.update;

import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsUpdater {

    private static final SettingsUpdater SIMPLE = new SettingsUpdater(Collections.unmodifiableList(new ArrayList<>())) {
        @Override
        public <T extends MapNode> T update(@NotNull T base, @Nullable MapNode provider) {
            if (provider != null) {
                base.deepMerge(provider.getValue());
            }
            return base;
        }
    };

    private List<NodeUpdate> nodeUpdates;

    @NotNull
    public static SettingsUpdater simple() {
        return SIMPLE;
    }

    public SettingsUpdater() {
        this(null);
    }

    public SettingsUpdater(@Nullable List<NodeUpdate> nodeUpdates) {
        this.nodeUpdates = nodeUpdates;
    }

    public <T extends MapNode> T update(@NotNull T base, @Nullable MapNode provider) {
        T baseResult = base;
        for (NodeUpdate nodeUpdate : getNodeUpdates()) {
            baseResult = nodeUpdate.apply(baseResult);
        }
        return baseResult;
    }

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