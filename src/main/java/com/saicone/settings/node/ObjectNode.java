package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.type.IterableType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ObjectNode extends NodeKey<Object> implements IterableType<Object> {

    public ObjectNode() {
        this(null);
    }

    protected ObjectNode(@Nullable Object value) {
        super(null, value);
    }

    public ObjectNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, null);
    }

    public ObjectNode(@Nullable MapNode parent, @Nullable String key, @Nullable Object value) {
        super(parent, key, value);
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
