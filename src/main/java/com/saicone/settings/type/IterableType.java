package com.saicone.settings.type;

import com.saicone.settings.type.iterator.ArrayIterator;
import com.saicone.settings.type.iterator.SingleIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Interface to allow any object to be targeted on the enhanced for statement.
 *
 * @author Rubenicos
 *
 * @param <T> the iterable object type.
 */
@FunctionalInterface
public interface IterableType<T> extends Iterable<T> {

    /**
     * Create a read-only iterable type object.
     *
     * @param value the value to iterate over.
     * @return      a new iterable type.
     * @param <T>   the iterable object type.
     */
    @NotNull
    static <T> IterableType<T> of(@NotNull Object value) {
        if (value instanceof IterableType) {
            return of(((IterableType<?>) value).getValue());
        }
        return () -> value;
    }

    /**
     * Get the object that can be iterated.
     *
     * @return an iterable object.
     */
    Object getValue();

    /**
     * Set the object that can be iterated.
     *
     * @param value an iterable object.
     * @return      an object.
     */
    default Object setValue(Object value) {
        throw new IllegalStateException("The current iterable type doesn't allow value override");
    }

    /**
     * Check if the current object can be iterated using for statement.<br>
     * This condition can be applied to any {@link Iterable} type or array.
     *
     * @return true if the object can be iterated.
     */
    default boolean isIterable() {
        return getValue() != null && (getValue() instanceof Iterable || getValue().getClass().isArray());
    }

    /**
     * Same has {@link #isIterable()} but with inverted result.
     *
     * @return true if the object can't be iterated.
     */
    default boolean isNotIterable() {
        return !isIterable();
    }

    @Override
    @SuppressWarnings("unchecked")
    default @NotNull java.util.Iterator<T> iterator() {
        Objects.requireNonNull(getValue(), "Cannot iterate over empty object");
        final Object value = getValue();
        if (value instanceof Iterable) {
            return ((Iterable<T>) value).iterator();
        } else if (value instanceof Map) {
            return (java.util.Iterator<T>) ((Map<?, ?>) value).entrySet().iterator();
        } else if (value instanceof Object[] || value.getClass().isArray()) {
            return new ArrayIterator<T>(value) {
                @Override
                public void setValue(Object value) {
                    IterableType.this.setValue(value);
                }
            };
        } else {
            return new SingleIterator<T>(value) {
                @Override
                public void setValue(Object value) {
                    IterableType.this.setValue(value);
                }
            };
        }
    }
}