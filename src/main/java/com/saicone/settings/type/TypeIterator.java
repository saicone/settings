package com.saicone.settings.type;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public abstract class TypeIterator<T> implements Iterator<T> {

    private final Object value;

    public TypeIterator(@NotNull Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public abstract void setValue(Object value);
}
