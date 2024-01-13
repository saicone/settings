package com.saicone.settings.type.iterator;

import com.saicone.settings.type.TypeIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public abstract class ArrayIterator<T> extends TypeIterator<T> {

    private final boolean objectArray;

    int currentIndex;
    int lastIndex = -1;

    public ArrayIterator(@NotNull Object value) {
        super(value);
        this.objectArray = value instanceof Object[];
    }

    public int size() {
        if (objectArray) {
            return ((Object[]) getValue()).length;
        } else {
            return Array.getLength(getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (objectArray) {
            return (T) ((Object[]) getValue())[index];
        } else {
            return (T) Array.get(getValue(), index);
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex != size();
    }

    @Override
    public T next() {
        int i = currentIndex;
        if (i >= size()) {
            throw new NoSuchElementException();
        }
        currentIndex = i + 1;
        return get(lastIndex = i);
    }

    @Override
    public void remove() {
        if (lastIndex < 0) {
            throw new IllegalStateException();
        }

        remove(lastIndex);
        currentIndex = lastIndex;
        lastIndex = -1;
    }

    @SuppressWarnings("unchecked")
    public void remove(int index) {
        final int size = size();
        if (size == 0 || index >= size) {
            throw new ConcurrentModificationException();
        }
        Object newArray = Array.newInstance(getValue().getClass().getComponentType(), size - 1);
        for (int i = 0; i < size; i++) {
            if (i != index) {
                Array.set(newArray, i, get(i));
            }
        }
        setValue((T) newArray);
    }
}
