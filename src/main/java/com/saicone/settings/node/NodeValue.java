package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.type.TypeParser;
import com.saicone.settings.type.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class NodeValue<V> implements SettingsNode {

    private Object faceValue;
    private Object sourceValue;

    private List<String> topComment;
    private List<String> sideComment;

    // Parsed value cache
    private transient TypeParser<?> typeParser;
    private transient Object parsedValue;

    @NotNull
    public static SettingsNode of(@Nullable Object object) {
        if (object instanceof SettingsNode) {
            return of(((SettingsNode) object).getValue()).mergeComment((SettingsNode) object);
        }
        if (object instanceof Map) {
            return new MapNode().merge((Map<?, ?>) object, true);
        } else if (object instanceof Iterable) {
            final List<SettingsNode> list = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                list.add(of(o));
            }
            return new ListNode(list);
        } else {
            return new ObjectNode(object);
        }
    }

    public NodeValue(@Nullable V value) {
        this.sourceValue = value;
    }

    @Override
    public boolean hasTopComment() {
        return this.topComment != null && !this.topComment.isEmpty();
    }

    @Override
    public boolean hasSideComment() {
        return this.sideComment != null && !this.sideComment.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getValue() {
        return (V) (faceValue != null ? faceValue : sourceValue);
    }

    @Nullable
    public Object getFaceValue() {
        return faceValue;
    }

    @Override
    public @Nullable Object getSourceValue() {
        return sourceValue;
    }

    @Override
    public @Nullable List<String> getTopComment() {
        return topComment;
    }

    @Override
    public @Nullable List<String> getSideComment() {
        return sideComment;
    }

    @NotNull
    @Override
    public SettingsNode setValue(@NotNull Object value) {
        if (this.sourceValue == null) {
            this.sourceValue = value;
        } else {
            this.faceValue = value;
        }
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setSourceValue(@Nullable Object value) {
        this.sourceValue = value;
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setTopComment(@Nullable List<String> topComment) {
        this.topComment = topComment;
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setSideComment(@Nullable List<String> sideComment) {
        this.sideComment = sideComment;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> @Nullable E as(@NotNull Class<E> type, @Nullable E def) {
        if (type.isInstance(this.parsedValue)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = Types.parse(type, getValue(), def);
        if (parsedValue != null) {
            this.parsedValue = parsedValue;
        }
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> @Nullable E as(@NotNull TypeParser<E> parser, @Nullable E def) {
        if (parser.equals(this.typeParser)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = parser.parse(getValue(), def);
        if (parsedValue != null) {
            this.typeParser = parser;
            this.parsedValue = parsedValue;
        }
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, C extends Collection<E>> @NotNull C asCollection(@NotNull C collection, @NotNull TypeParser<E> parser) {
        if (parser.equals(this.typeParser) && collection.getClass().isInstance(this.parsedValue)) {
            return (C) this.parsedValue;
        }
        final C parsedValue = parser.collection(collection, getValue());
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }
}