package com.saicone.settings.type.iterator;

import com.saicone.settings.type.TypeIterator;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public abstract class SingleIterator<T> extends TypeIterator<T> {

    private boolean consumed = false;

    public SingleIterator(@NotNull Object value) {
        super(value);
    }

    @Override
    public boolean hasNext() {
        return !consumed && getValue() != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T next() {
        if (consumed || getValue() == null) {
            throw new NoSuchElementException();
        }
        consumed = true;
        return (T) getValue();
    }

    @Override
    public void remove() {
        if (consumed || getValue() == null) {
            setValue(null);
            consumed = false;
        } else {
            throw new IllegalStateException();
        }
    }
}
