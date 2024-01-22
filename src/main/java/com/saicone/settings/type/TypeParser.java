package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents a function that parse any type of object and converts into value type.
 *
 * @author Rubenicos
 *
 * @param <T> the type result of the function.
 */
@FunctionalInterface
public interface TypeParser<T> {

    /**
     * Create a type parser that accepts only single objects,
     * this means that any iterable or array object will be converted
     * into single object by taking the first list or array value.
     *
     * @param parser the delegate parser that process any single object.
     * @return       a type parser that accepts only single objects.
     * @param <T>    the type result of the function.
     */
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

    /**
     * Create a type parser that accepts the first object,
     * this means that any iterable will be converted into
     * the first present value to parse.
     *
     * @param parser the delegate parser that process the first object.
     * @return       a type parser that accepts the first object.
     * @param <T>    the type result of the function.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    static <T> TypeParser<T> first(@NotNull TypeParser<T> parser) {
        return (object) -> {
            if (object instanceof Iterable) {
                final Iterator<Object> iterator = ((Iterable<Object>) object).iterator();
                final Object obj;
                if (iterator.hasNext() && (obj = iterator.next()) != null) {
                    return parser.parse(obj);
                } else {
                    return null;
                }
            } else {
                return parser.parse(object);
            }
        };
    }

    /**
     * Create a type parser that return a number type.<br>
     * This method is a superset of {@link TypeParser#single(TypeParser)} that
     * convert any boolean value into integer after parse it.
     *
     * @param parser the delegate parser that process any single non-boolean object.
     * @return       a type parser that return a number type.
     * @param <T>    the number type result of the function.
     */
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

    /**
     * Parse the given object into required type.
     *
     * @param object the object to parse.
     * @return       a converted value type, null otherwise.
     */
    @Nullable
    T parse(@NotNull Object object);

    /**
     * Parse the given object into required type with a default return value.
     *
     * @param object the object to parse.
     * @param def    the type object to return if parse fails.
     * @return       a converted value type, default object otherwise.
     */
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

    /**
     * Parse the given object into required type with a default return value.<br>
     * This method also checks if the object is instance of given class type.
     *
     * @param type   the class type.
     * @param object the object to parse.
     * @param def    the type object to return if parse fails.
     * @return       a converted value type, default object otherwise.
     */
    @Nullable
    @Contract("_, _, !null -> !null")
    @SuppressWarnings("unchecked")
    default T parse(@NotNull Class<T> type, @Nullable Object object, @Nullable T def) {
        if (type.isInstance(object)) {
            return (T) object;
        }
        return parse(object, def);
    }

    /**
     * Parse the given object into collection parameter.<br>
     * This method inherits into any type of object to add parsed values into collection.
     *
     * @param collection the collection to add parsed values.
     * @param object     the object to parse.
     * @return           a type collection.
     * @param <C>        the collection type to return.
     */
    @NotNull
    default <C extends Collection<T>> C collection(@NotNull C collection, @Nullable Object object) {
        return collection(collection, object, null);
    }

    /**
     * Parse the given object into collection parameter.<br>
     * This method inherits into any type of object to add parsed values into collection.
     *
     * @param collection the collection to add parsed values.
     * @param object     the object to parse.
     * @param def        the type object to fill failed parsed values.
     * @return           a type collection.
     * @param <C>        the collection type to return.
     */
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

    /**
     * Parse the given object into list of type value.
     *
     * @param object the object to parse.
     * @return       a type list.
     */
    @NotNull
    default List<T> list(@Nullable Object object) {
        return list(object, null);
    }

    /**
     * Parse the given object into list of type value.
     *
     * @param object the object to parse.
     * @param def    the type object to fill failed parsed values.
     * @return       a type list.
     */
    @NotNull
    default List<T> list(@Nullable Object object, @Nullable T def) {
        return collection(new ArrayList<>(), object, def);
    }

    /**
     * Parse the given object into set of type value.
     *
     * @param object the object to parse.
     * @return       a type set.
     */
    @NotNull
    default Set<T> set(@Nullable Object object) {
        return set(object, null);
    }

    /**
     * Parse the given object into set of type value.
     *
     * @param object the object to parse.
     * @param def    the type object to fill failed parsed values.
     * @return       a type set.
     */
    @NotNull
    default Set<T> set(@Nullable Object object, @Nullable T def) {
        return collection(new HashSet<>(), object, def);
    }

    /**
     * Parse the given object into provided array type value.
     *
     * @param array  the array to add values.
     * @param object the object to parse.
     * @return       a type array.
     */
    @NotNull
    default T[] array(@NotNull T[] array, @Nullable Object object) {
        return array(array, object, null);
    }

    /**
     * Parse the given object into provided array type value.
     *
     * @param array  the array to add values.
     * @param object the object to parse.
     * @param def    the type object to fill failed parsed values.
     * @return       a type array.
     */
    @NotNull
    default T[] array(@NotNull T[] array, @Nullable Object object, @Nullable T def) {
        return list(object, def).toArray(array);
    }
}
