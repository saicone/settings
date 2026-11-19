package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract class that represents a node multi-layer value along with comments.<br>
 * Any type transformation will be cached.
 *
 * @author Rubenicos
 *
 * @param <V> the value type of the node.
 */
public abstract class NodeValue<V> implements SettingsNode {

    /**
     * The facing value of this node.
     */
    protected Object faceValue;
    /**
     * The source value of this node, usually the original value.
     */
    protected Object sourceValue;

    /**
     * The top comment of the node.
     */
    protected List<String> topComment;
    /**
     * The side comment of the node.
     */
    protected List<String> sideComment;

    // Parsed value cache
    private transient TypeParser<?> typeParser;
    private transient TypeParser<?> valueParser;
    private transient Object parsedValue;

    /**
     * Create a node value with the given object.
     *
     * @param object the object to wrap as node value.
     * @return       a settings node that represents the node value.
     */
    @NotNull
    public static SettingsNode of(@Nullable Object object) {
        if (object instanceof SettingsNode) {
            return of(((SettingsNode) object).getValue()).mergeComment((SettingsNode) object);
        }
        if (object instanceof Map) {
            return new MapNode().merge((Map<?, ?>) object, true);
        } else if (object instanceof Iterable) {
            return new ListNode().merge((Iterable<?>) object);
        } else {
            return new ObjectNode(object);
        }
    }

    /**
     * Constructs a node value with the given object.
     *
     * @param value the value of the node.
     */
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

    /**
     * Get the facing value inside the node.
     *
     * @return the value that was set after node creation, null otherwise.
     */
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
    public @NotNull <E> E as(@NotNull Class<E> type) {
        if (type.isInstance(this.parsedValue)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = SettingsNode.super.as(type);
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <E> E as(@NotNull TypeParser<E> parser) {
        if (parser.equals(this.typeParser) && this.parsedValue != null) {
            return (E) this.parsedValue;
        }
        final E parsedValue = SettingsNode.super.as(parser);
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> @NotNull E asEnum(@NotNull Class<E> type) {
        if (type.isInstance(this.parsedValue)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = SettingsNode.super.asEnum(type);
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <C, A> A asArray(@NotNull Class<C> component) {
        if (this.parsedValue != null && this.parsedValue.getClass().isArray() && component.isInstance(this.parsedValue.getClass().getComponentType())) {
            return (A) this.parsedValue;
        }
        final A parsedValue = SettingsNode.super.asArray(component);
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <C, A> A asArray(@NotNull TypeParser<C> parser) {
        if (parser.equals(this.typeParser) && this.parsedValue != null && this.parsedValue.getClass().isArray()) {
            return (A) this.parsedValue;
        }
        final A parsedValue = SettingsNode.super.asArray(parser);
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <E> List<E> asList(@NotNull TypeParser<E> parser) {
        if (parser.equals(this.typeParser) && this.parsedValue instanceof List) {
            return (List<E>) this.parsedValue;
        }
        final List<E> parsedValue = SettingsNode.super.asList(parser);
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <E> Set<E> asSet(@NotNull TypeParser<E> parser) {
        if (parser.equals(this.typeParser) && this.parsedValue instanceof Set) {
            return (Set<E>) this.parsedValue;
        }
        final Set<E> parsedValue = SettingsNode.super.asSet(parser);
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <K, v, M extends Map<K, v>> M asMap(@NotNull TypeParser<K> keyParser, @NotNull TypeParser<v> valueParser) {
        if (keyParser.equals(this.typeParser) && valueParser.equals(this.valueParser) && this.parsedValue instanceof Map) {
            return (M) this.parsedValue;
        }
        final M parsedValue = SettingsNode.super.asMap(keyParser, valueParser);
        this.typeParser = keyParser;
        this.valueParser = valueParser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeValue<V> clone() {
        final NodeValue<V> node;
        try {
            node = (NodeValue<V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }

        node.faceValue = null;
        if (this.faceValue != null) {
            node.sourceValue = this.faceValue;
        } else {
            node.sourceValue = this.sourceValue;
        }

        if (this.topComment != null) {
            node.topComment = new ArrayList<>(this.topComment);
        }
        if (this.sideComment != null) {
            node.sideComment = new ArrayList<>(this.sideComment);
        }

        node.typeParser = null;
        node.valueParser = null;
        node.parsedValue = null;

        return node;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeValue<V> shallowClone() {
        return (NodeValue<V>) SettingsNode.super.shallowClone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeValue<V> shallowClone(boolean parent) {
        return (NodeValue<V>) SettingsNode.super.shallowClone(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull NodeValue<V> shallowClone(boolean parent, boolean key) {
        final NodeValue<V> node;
        try {
            node = (NodeValue<V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }

        node.typeParser = null;
        node.valueParser = null;
        node.parsedValue = null;

        return node;
    }

    @Override
    public String toString() {
        return Types.STRING.parseOrElse(getValue(), "null");
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NodeValue)) return false;

        NodeValue<?> nodeValue = (NodeValue<?>) object;

        return Objects.equals(getValue(), nodeValue.getValue());
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}