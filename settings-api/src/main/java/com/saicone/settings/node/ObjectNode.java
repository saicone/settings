package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.types.AnyIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Class to handle normal object as settings node.<br>
 * This object can be inherited in enhanced for loop.
 *
 * @author Rubenicos
 */
public class ObjectNode extends NodeKey<Object> implements AnyIterable<Object> {

    /**
     * Constructs an empty object value.
     */
    public ObjectNode() {
        this(null);
    }

    /**
     * Constructs an object node with the given value.
     *
     * @param value the object to wrap as object node.
     */
    protected ObjectNode(@Nullable Object value) {
        super(null, value);
    }

    /**
     * Constructs an empty object node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     */
    public ObjectNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, null);
    }

    /**
     * Constructs an empty object node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     * @param value  the object to wrap as object node.
     */
    public ObjectNode(@Nullable MapNode parent, @Nullable String key, @Nullable Object value) {
        super(parent, key, value);
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public @NotNull SettingsNode setValue(@NotNull Object value) {
        final SettingsNode node;
        if (value instanceof Map) {
            node = new MapNode(getParent(), getKey()).merge(this).setValue(value);
        } else if (value instanceof SettingsNode) {
            node = setValue(((SettingsNode) value).getValue());
        } else if (value instanceof Iterable) {
            node = new ListNode(getParent(), getKey()).merge((SettingsNode) this).setValue(value);
        } else {
            return super.setValue(value);
        }
        return node.setKey(getKey());
    }
}
