package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

@FunctionalInterface
public interface TypeParser<T> {

    @NotNull
    @SuppressWarnings("unchecked")
    static <T> TypeParser<T> single(@NotNull TypeParser<T> parser) {
        return (object) -> {
            if (object instanceof Iterable) {
                final Iterator<Object> iterator = ((Iterable<Object>) object).iterator();
                final Object obj;
                if (iterator.hasNext() && (obj = iterator.next()) != null) {
                    return parser.parse(obj);
                } else {
                    return null;
                }
            } else if (object.getClass().isArray()) {
                final Object obj;
                if (Array.getLength(object) > 0 && (obj = Array.get(object, 0)) != null) {
                    return parser.parse(obj);
                } else {
                    return null;
                }
            } else {
                return parser.parse(object);
            }
        };
    }

    @NotNull
    static <T extends Number> TypeParser<T> number(@NotNull TypeParser<T> parser) {
        return single((object) -> {
            if (object instanceof Boolean) {
                return parser.parse(Boolean.TRUE.equals(object) ? 1 : 0);
            } else {
                return parser.parse(object);
            }
        });
    }

    @Nullable
    T parse(@NotNull Object object);

    @Nullable
    @Contract("_, !null -> !null")
    default T parse(@Nullable Object object, @Nullable T def) {
        if (object == null) {
            return def;
        }
        if (object instanceof ValueType) {
            return parse(((ValueType<?>) object).getValue(), def);
        }
        final T obj = parse(object);
        return obj != null ? obj : def;
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    @SuppressWarnings("unchecked")
    default T parse(@NotNull Class<T> type, @Nullable Object object, @Nullable T def) {
        if (type.isInstance(object)) {
            return (T) object;
        }
        return parse(object, def);
    }

    @NotNull
    default <C extends Collection<T>> C collection(@NotNull C collection, @Nullable Object object) {
        return collection(collection, object, null);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    default <C extends Collection<T>> C collection(@NotNull C collection, @Nullable Object object, @Nullable T def) {
        if (object == null) {
            return collection;
        }

        // Check if the current value is the same type of collection
        if (collection.getClass().isInstance(object)) {
            if (((Collection<?>) object).isEmpty()) {
                return collection;
            }
            // Test first non-null value type
            for (Object obj : ((Collection<?>) object)) {
                if (obj == null) {
                    continue;
                }
                if (obj.equals(parse(obj, def))) {
                    try {
                        return (C) object;
                    } catch (ClassCastException ignored) { }
                }
                break;
            }
        }
        for (Object obj : IterableType.of(object)) {
            final T result = parse(obj, def);
            if (result != null) {
                collection.add(result);
            }
        }
        return collection;
    }

    @NotNull
    default List<T> list(@Nullable Object object) {
        return list(object, null);
    }

    @NotNull
    default List<T> list(@Nullable Object object, @Nullable T def) {
        return collection(new ArrayList<>(), object, def);
    }

    @NotNull
    default Set<T> set(@Nullable Object object) {
        return set(object, null);
    }

    @NotNull
    default Set<T> set(@Nullable Object object, @Nullable T def) {
        return collection(new HashSet<>(), object, def);
    }

    @NotNull
    default T[] array(@NotNull T[] array, @Nullable Object object) {
        return array(array, object, null);
    }

    @NotNull
    default T[] array(@NotNull T[] array, @Nullable Object object, @Nullable T def) {
        return list(object, def).toArray(array);
    }
}
