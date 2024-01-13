package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ListNode extends NodeKey<List<SettingsNode>> implements List<SettingsNode> {

    public ListNode() {
        this(new ArrayList<>());
    }

    public ListNode(@Nullable List<SettingsNode> value) {
        this(null, null, value);
    }

    public ListNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, new ArrayList<>());
    }

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
        if (value instanceof Map) {
            node = new MapNode(getParent(), getKey()).merge(this).setValue(value);
        } else {
            node = new ObjectNode(getParent(), getKey()).merge(this).setValue(value);
        }
        node.setKey(getKey());
        return node;
    }

    @NotNull
    public ListNode merge(@NotNull Iterable<?> iterable) {
        for (Object o : iterable) {
            add(NodeValue.of(o));
        }
        return this;
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
