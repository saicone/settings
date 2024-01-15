package com.saicone.settings.update;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class NodeUpdate {

    private final UpdateAction action;
    private Function<MapNode, SettingsNode> getter;
    private String[] path;
    private Object value;

    @NotNull
    public static NodeUpdate add(@NotNull Object value) {
        return new NodeUpdate(UpdateAction.ADD).value(value);
    }

    @NotNull
    public static NodeUpdate delete() {
        return new NodeUpdate(UpdateAction.DELETE);
    }

    @NotNull
    public static NodeUpdate replace(@NotNull Object value) {
        return new NodeUpdate(UpdateAction.REPLACE).value(value);
    }

    @NotNull
    public static NodeUpdate move() {
        return new NodeUpdate(UpdateAction.MOVE);
    }

    @NotNull
    public static NodeUpdate custom(@NotNull Function<MapNode, MapNode> function) {
        return new NodeUpdate(UpdateAction.CUSTOM) {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends MapNode> T apply(@NotNull T parent) {
                return (T) function.apply(parent);
            }
        };
    }

    public NodeUpdate(@NotNull UpdateAction action) {
        this.action = action;
    }

    @NotNull
    public UpdateAction getAction() {
        return action;
    }

    @Nullable
    public SettingsNode getNode(@NotNull MapNode parent) {
        return getter == null ? null : getter.apply(parent);
    }

    @Nullable
    public String[] getPath() {
        return path;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate from(@NotNull Function<MapNode, SettingsNode> getter) {
        this.getter = getter;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate from(@NotNull String... path) {
        return from(map -> map.get(path));
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate fromIgnoreCase(@NotNull String... path) {
        return from(map -> map.getIgnoreCase(path));
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate fromRegex(@NotNull @Language("RegExp") String... path) {
        return from(map -> map.getRegex(path));
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate to(@NotNull String... path) {
        this.path = path;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public NodeUpdate value(@Nullable Object value) {
        this.value = value;
        return this;
    }

    public <T extends MapNode> T apply(@NotNull T parent) {
        final SettingsNode node = getNode(parent);
        if (node == null) {
            return parent;
        }
        switch (action) {
            case ADD:
                if (!node.isReal()) {
                    node.setValue(value);
                }
                break;
            case DELETE:
                node.delete(true);
                break;
            case REPLACE:
                node.setValue(value);
                break;
            case MOVE:
                final String[] path = getPath();
                if (path != null) {
                    node.move(path);
                }
                break;
            default:
                break;
        }
        return parent;
    }
}
