package com.saicone.settings.type.iterator;

import com.saicone.settings.type.TypeIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * Abstract class to inherit into any array.<br>
 * Accepts any type of primitive and object array.
 *
 * @author Rubenicos
 *
 * @param <T> the array type.
 */
public abstract class ArrayIterator<T> extends TypeIterator<T> {

    private final boolean objectArray;

    private int currentIndex;
    private int lastIndex = -1;

    /**
     * Constructs an array iterator with provided array object.
     *
     * @param value the array to inherit.
     */
    public ArrayIterator(@NotNull Object value) {
        super(value);
        this.objectArray = value instanceof Object[];
    }

    /**
     * Returns the length of the specified array object, as an int.
     *
     * @return the length of the array.
     */
    public int size() {
        if (objectArray) {
            return ((Object[]) getValue()).length;
        } else {
            return Array.getLength(getValue());
        }
    }

    /**
     * Returns the value of the indexed component in the specified array object.
     * The value is automatically wrapped in an object if it has a primitive type.
     *
     * @param index the index.
     * @return      the (possibly wrapped) value of the indexed component in the specified array.
     */
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

    /**
     * Remove value form array at specified index.
     *
     * @param index the index.
     */
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
