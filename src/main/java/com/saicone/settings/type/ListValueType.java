package com.saicone.settings.type;

import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import com.saicone.types.AnyObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Represents a value that can be converted into different types of objects.
 *
 * @author Rubenicos
 *
 * @param <T> the value type itself.
 */
@FunctionalInterface
public interface ListValueType<T> extends AnyObject<T> {

    /**
     * Create a wrapped value type from given object type.
     *
     * @param value the object type to wrap.
     * @return      a value type instance.
     * @param <T>   the value type itself.
     */
    @NotNull
    static <T> ListValueType<T> of(T value) {
        return () -> value;
    }

    /**
     * Convert this object into a list of objects.
     *
     * @return a list containing objects.
     */
    @NotNull
    default List<Object> asList() {
        return asList(Types.OBJECT);
    }

    /**
     * Convert this object into a list of strings.
     *
     * @see Types#STRING
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into strings.
     */
    @NotNull
    default List<String> asStringList() {
        return asList(Types.STRING);
    }

    /**
     * Convert this object into a list of characters.
     *
     * @see Types#CHAR
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into characters.
     */
    @NotNull
    default List<Character> asCharList() {
        return asList(Types.CHAR);
    }

    /**
     * Convert this object into a list of booleans.
     *
     * @see Types#BOOLEAN
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into booleans.
     */
    @NotNull
    default List<Boolean> asBooleanList() {
        return asList(Types.BOOLEAN);
    }

    /**
     * Convert this object into a list of bytes.
     *
     * @see Types#BYTE
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into bytes.
     */
    @NotNull
    default List<Byte> asByteList() {
        return asList(Types.BYTE);
    }

    /**
     * Convert this object into a list of shorts.
     *
     * @see Types#SHORT
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into shorts.
     */
    @NotNull
    default List<Short> asShortList() {
        return asList(Types.SHORT);
    }

    /**
     * Convert this object into a list of integers.
     *
     * @see Types#INTEGER
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into integers.
     */
    @NotNull
    default List<Integer> asIntList() {
        return asList(Types.INTEGER);
    }

    /**
     * Convert this object into a list of floats.
     *
     * @see Types#FLOAT
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into floats.
     */
    @NotNull
    default List<Float> asFloatList() {
        return asList(Types.FLOAT);
    }

    /**
     * Convert this object into a list of longs.
     *
     * @see Types#LONG
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into longs.
     */
    @NotNull
    default List<Long> asLongList() {
        return asList(Types.LONG);
    }

    /**
     * Convert this object into a list of doubles.
     *
     * @see Types#DOUBLE
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into doubles.
     */
    @NotNull
    default List<Double> asDoubleList() {
        return asList(Types.DOUBLE);
    }

    /**
     * Convert this object into a list of unique IDs.
     *
     * @see Types#UUID
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into unique IDs.
     */
    @NotNull
    default List<UUID> asUniqueIdList() {
        return asList(Types.UUID);
    }
}
