package com.saicone.settings;

import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.type.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SettingsNode extends ValueType<Object> {

    default boolean isMap() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    boolean isRoot();

    boolean hasTopComment();

    boolean hasSideComment();

    @Nullable
    MapNode getParent();

    @Nullable
    String getKey();

    @Nullable
    Object getSourceValue();

    @Nullable
    List<String> getTopComment();

    @Nullable
    List<String> getSideComment();

    @NotNull
    default SettingsNode getRoot() {
        SettingsNode node = this;
        MapNode map;
        while ((map = node.getParent()) != null) {
            node = map;
        }
        return node;
    }

    @NotNull
    SettingsNode setParent(@Nullable MapNode parent);

    @NotNull
    SettingsNode setKey(@Nullable String key);

    @NotNull
    SettingsNode setValue(@NotNull Object value);

    @NotNull
    SettingsNode setSourceValue(@Nullable Object value);

    @NotNull
    SettingsNode setTopComment(@Nullable List<String> topComment);

    @NotNull
    SettingsNode setSideComment(@Nullable List<String> sideComment);

    @NotNull
    default SettingsNode addTopComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> topComment = getTopComment();
            if (topComment == null) {
                return setTopComment(lines);
            } else {
                topComment.addAll(lines);
            }
        }
        return this;
    }

    @NotNull
    default SettingsNode addSideComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> sideComment = getSideComment();
            if (sideComment == null) {
                return setTopComment(lines);
            } else {
                sideComment.addAll(lines);
            }
        }
        return this;
    }

    @NotNull
    default SettingsNode merge(@NotNull SettingsNode node) {
        setSourceValue(node.getSourceValue());
        return mergeComment(node);
    }

    @NotNull
    default SettingsNode mergeComment(@NotNull SettingsNode node) {
        if (getTopComment() == null) {
            setTopComment(node.getTopComment());
        }
        if (getSideComment() == null) {
            setSideComment(node.getSideComment());
        }
        return this;
    }

    default void delete() {
        delete(true);
    }

    default void delete(boolean deep) {
        final MapNode parent = getParent();
        if (parent != null) {
            parent.remove(getKey(), deep);
        }
    }

    @NotNull
    default SettingsNode move(@NotNull String... path) {
        final MapNode parent = getParent();
        if (parent != null) {
            parent.remove(getKey());
            parent.set(this, path);
        } else {
            setKey(path[path.length - 1]);
        }
        return this;
    }

    @NotNull
    default MapNode asMapNode() {
        return (MapNode) this;
    }

    @NotNull
    default ListNode asListNode() {
        return (ListNode) this;
    }
}
