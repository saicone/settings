package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Class that represents a node with parent node and key parameter along with value.
 *
 * @author Rubenicos
 *
 * @param <V> the value type of the node.
 */
public class NodeKey<V> extends NodeValue<V> {

    private MapNode parent;
    private String key;

    /**
     * Create a node key with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     * @param object the object to wrap as node key.
     * @return       a settings object that represents the node key.
     */
    @NotNull
    public static SettingsNode of(@Nullable MapNode parent, @Nullable String key, @Nullable Object object) {
        if (object instanceof SettingsNode) {
            return of(parent, key, ((SettingsNode) object).getValue()).mergeComment((SettingsNode) object);
        }

        if (object instanceof Map) {
            return new MapNode(parent, key).merge((Map<?, ?>) object);
        } else if (object instanceof Iterable) {
            return new ListNode(parent, key).merge((Iterable<?>) object);
        } else {
            return new ObjectNode(parent, key, object);
        }
    }

    /**
     * Constructs a node key with the given parameters.
     *
     * @param parent the parent node.
     * @param value  the object to wrap as node key.
     */
    protected NodeKey(@Nullable MapNode parent, @Nullable V value) {
        super(value);
        this.parent = parent;
        this.key = null;
    }

    /**
     * Constructs a node key with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     * @param value  the object to wrap as node key.
     */
    public NodeKey(@Nullable MapNode parent, @Nullable String key, @Nullable V value) {
        super(value);
        this.parent = parent;
        this.key = key;
    }

    @Override
    public @Nullable MapNode getParent() {
        return parent;
    }

    @Override
    public @Nullable String getKey() {
        return key;
    }

    @NotNull
    @Override
    public SettingsNode setParent(MapNode parent) {
        this.parent = parent;
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setKey(@Nullable String key) {
        if (this.parent != null) {
            if (this.key != null && !Objects.equals(this.key, key)) {
                this.parent.remove(this.key);
            }
            if (key != null) {
                this.parent.put(key, this);
            }
        }
        this.key = key;
        return this;
    }
}
