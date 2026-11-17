package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to handle a list of settings nodes.<br>
 * This object can also be handled as regular Java list.
 *
 * @author Rubenicos
 */
public class ListNode extends NodeKey<List<SettingsNode>> implements List<SettingsNode> {

    /**
     * Constructs an empty list of nodes.
     */
    public ListNode() {
        this(new ArrayList<>());
    }

    /**
     * Constructs an list node with the given list value.
     *
     * @param value the object to wrap as list node.
     */
    public ListNode(@Nullable List<SettingsNode> value) {
        this(null, null, value);
    }

    /**
     * Constructs an list node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     */
    public ListNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, new ArrayList<>());
    }

    /**
     * Constructs an list node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     * @param value  the object to wrap as list node.
     */
    public ListNode(@Nullable MapNode parent, @Nullable String key, @Nullable List<SettingsNode> value) {
        super(parent, key, value);
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public @NotNull SettingsNode setValue(@NotNull Object value) {
        if (value instanceof Iterable) {
            if (getFaceValue() == null) {
                super.setValue(new ArrayList<>());
            } else {
                clear();
            }
            return merge((Iterable<?>) value);
        }
        final SettingsNode node;
        if (value instanceof SettingsNode) {
            node = setValue(((SettingsNode) value).getValue());
        } else if (value instanceof Map) {
            node = new MapNode(getParent(), getKey()).merge(this).setValue(value);
        } else {
            node = new ObjectNode(getParent(), getKey()).merge(this).setValue(value);
        }
        node.setKey(getKey());
        return node;
    }

    /**
     * Merge provided iterable object into the current list of nodes.<br>
     * This method also creates a node value for each inherited value.
     *
     * @param iterable the object to inherit.
     * @return         a list node with the merged values, normally the original list itself.
     */
    @NotNull
    public ListNode merge(@NotNull Iterable<?> iterable) {
        for (Object o : iterable) {
            add(NodeValue.of(o));
        }
        return this;
    }

    @Override
    public @NotNull SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        final List<SettingsNode> list = getValue();
        final Iterator<SettingsNode> iterator = list.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            SettingsNode node = iterator.next();
            if (node != null) {
                node = node.edit(function);
                if (node != null) {
                    list.set(i, node);
                } else {
                    i--;
                    iterator.remove();
                }
            }
            i++;
        }
        return this;
    }

    @Override
    public @NotNull List<Object> asLiteralObject() {
        final List<Object> list = new ArrayList<>();
        for (SettingsNode node : getValue()) {
            list.add(node.asLiteralObject());
        }
        return list;
    }

    @Override
    public String toString() {
        return getValue().stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    // Default map implementation

    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getValue().contains(o);
    }

    @Override
    public @NotNull Iterator<SettingsNode> iterator() {
        return getValue().iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return getValue().toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return getValue().toArray(a);
    }

    @Override
    public boolean add(SettingsNode e) {
        return getValue().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return getValue().remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(getValue()).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends SettingsNode> c) {
        return getValue().addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends SettingsNode> c) {
        return getValue().addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return getValue().removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return getValue().retainAll(c);
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @Override
    public SettingsNode get(int index) {
        return getValue().get(index);
    }

    @Override
    public SettingsNode set(int index, SettingsNode element) {
        return getValue().set(index, element);
    }

    @Override
    public void add(int index, SettingsNode element) {
        getValue().add(index, element);
    }

    @Override
    public SettingsNode remove(int index) {
        return getValue().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return getValue().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getValue().lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<SettingsNode> listIterator() {
        return getValue().listIterator();
    }

    @NotNull
    @Override
    public ListIterator<SettingsNode> listIterator(int index) {
        return getValue().listIterator(index);
    }

    @NotNull
    @Override
    public List<SettingsNode> subList(int fromIndex, int toIndex) {
        return getValue().subList(fromIndex, toIndex);
    }
}
