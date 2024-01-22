package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a value that can be converted into different types of objects.
 *
 * @author Rubenicos
 *
 * @param <T> the value type itself.
 */
@FunctionalInterface
public interface ValueType<T> {

    /**
     * Create a wrapped value type from given object type.
     *
     * @param value the object type to wrap.
     * @return      a value type instance.
     * @param <T>   the value type itself.
     */
    @NotNull
    static <T> ValueType<T> of(T value) {
        return () -> value;
    }

    /**
     * Get the value type that implements the interface.
     *
     * @return the value type itself.
     */
    T getValue();

    /**
     * Convert this object into given class type.
     * 
     * @see Types#parse(Class, Object) 
     *
     * @param type the class type.
     * @return     a converted value type, null otherwise.
     * @param <E>  the type result.
     */
    @Nullable
    default <E> E as(@NotNull Class<E> type) {
        return as(type, null);
    }

    /**
     * Convert this object into given class type.
     * 
     * @see Types#parse(Class, Object, Object) 
     *
     * @param type the class type.
     * @param def  the type object to return if the conversion fails.
     * @return     a converted value type, default object otherwise.
     * @param <E>  the type result.
     */
    @Nullable
    @Contract("_, !null -> !null")
    default <E> E as(@NotNull Class<E> type, @Nullable E def) {
        return Types.parse(type, getValue(), def);
    }

    /**
     * Convert this object with the given type parser.
     * 
     * @see TypeParser#parse(Object) 
     *
     * @param parser the parser to apply this object into.
     * @return       a converted value type, null otherwise.
     * @param <E>    the type result.
     */
    @Nullable
    default <E> E as(@NotNull TypeParser<E> parser) {
        return as(parser, null);
    }

    /**
     * Convert this object with the given type parser.
     * 
     * @see TypeParser#parse(Object, Object) 
     *
     * @param parser the parser to apply this object into.
     * @param def    the type object to return if parse fails.
     * @return       a converted value type, default object otherwise.
     * @param <E>    the type result.
     */
    @Nullable
    @Contract("_, !null -> !null")
    default <E> E as(@NotNull TypeParser<E> parser, @Nullable E def) {
        return parser.parse(getValue(), def);
    }

    /**
     * Convert this object into given collection type with type parser.
     *
     * @see TypeParser#collection(Collection, Object)
     *
     * @param collection the collection to add parsed values.
     * @param parser     the parser to apply this object into.
     * @return           a type collection.
     * @param <E>        the type by parser.
     * @param <C>        the collection type.
     */
    @NotNull
    default <E, C extends Collection<E>> C asCollection(@NotNull C collection, @NotNull TypeParser<E> parser) {
        return parser.collection(collection, getValue());
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
     * Convert this object into a list with the given type parser.
     * 
     * @see TypeParser#list(Object) 
     *
     * @param parser the parser to apply this object into.
     * @return       a type list containing the parsed values.
     * @param <E>    the type by parser.
     */
    @NotNull
    default <E> List<E> asList(@NotNull TypeParser<E> parser) {
        return asCollection(new ArrayList<>(), parser);
    }

    /**
     * Convert this object into a set with the given type parser.
     * 
     * @see TypeParser#set(Object) 
     *
     * @param parser the parser to apply this object into.
     * @return       a type set containing the parsed values.
     * @param <E>    the type by parser.
     */
    @NotNull
    default <E> Set<E> asSet(@NotNull TypeParser<E> parser) {
        return asCollection(new HashSet<>(), parser);
    }

    /**
     * Convert this object into a string.
     *
     * @see Types#STRING
     *
     * @return a string, null if conversion fails.
     */
    @Nullable
    default String asString() {
        return as(Types.STRING);
    }

    /**
     * Convert this object into a string.
     *
     * @see Types#STRING
     *
     * @param def the default string.
     * @return    a string, default string if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default String asString(@Nullable String def) {
        return as(Types.STRING, def);
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
     * Convert this object into a character.
     *
     * @see Types#CHAR
     *
     * @return a character, null if conversion fails.
     */
    @Nullable
    default Character asChar() {
        return as(Types.CHAR);
    }

    /**
     * Convert this object into a character.
     *
     * @see Types#CHAR
     *
     * @param def the default character.
     * @return    a character, default character if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Character asChar(@Nullable Character def) {
        return as(Types.CHAR, def);
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
     * Convert this object into a boolean.
     *
     * @see Types#BOOLEAN
     *
     * @return a boolean, null if conversion fails.
     */
    @Nullable
    default Boolean asBoolean() {
        return as(Types.BOOLEAN);
    }

    /**
     * Convert this object into a boolean.
     *
     * @see Types#BOOLEAN
     *
     * @param def the default boolean.
     * @return    a boolean, default boolean if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Boolean asBoolean(@Nullable Boolean def) {
        return as(Types.BOOLEAN, def);
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
     * Convert this object into a byte.
     *
     * @see Types#BYTE
     *
     * @return a byte, null if conversion fails.
     */
    @Nullable
    default Byte asByte() {
        return as(Types.BYTE);
    }

    /**
     * Convert this object into a byte.
     *
     * @see Types#BYTE
     *
     * @param def the default string.
     * @return    a byte, default byte if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Byte asByte(@Nullable Byte def) {
        return as(Types.BYTE, def);
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
     * Convert this object into a short.
     *
     * @see Types#SHORT
     *
     * @return a short, null if conversion fails.
     */
    @Nullable
    default Short asShort() {
        return as(Types.SHORT);
    }

    /**
     * Convert this object into a short.
     *
     * @see Types#SHORT
     *
     * @param def the default string.
     * @return    a short, default short if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Short asShort(@Nullable Short def) {
        return as(Types.SHORT, def);
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
     * Convert this object into an integer.
     *
     * @see Types#INT
     *
     * @return an integer, null if conversion fails.
     */
    @Nullable
    default Integer asInt() {
        return as(Types.INT);
    }

    /**
     * Convert this object into an integer.
     *
     * @see Types#INT
     *
     * @param def the default string.
     * @return    an integer, default integer if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Integer asInt(@Nullable Integer def) {
        return as(Types.INT, def);
    }

    /**
     * Convert this object into a list of integers.
     *
     * @see Types#INT
     * @see TypeParser#collection(Collection, Object)
     *
     * @return a list containing only values that was converted into integers.
     */
    @NotNull
    default List<Integer> asIntList() {
        return asList(Types.INT);
    }

    /**
     * Convert this object into a float.
     *
     * @see Types#FLOAT
     *
     * @return a float, null if conversion fails.
     */
    @Nullable
    default Float asFloat() {
        return as(Types.FLOAT);
    }

    /**
     * Convert this object into a float.
     *
     * @see Types#FLOAT
     *
     * @param def the default string.
     * @return    a float, default float if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Float asFloat(@Nullable Float def) {
        return as(Types.FLOAT, def);
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
     * Convert this object into a long.
     *
     * @see Types#LONG
     *
     * @return a long, null if conversion fails.
     */
    @Nullable
    default Long asLong() {
        return as(Types.LONG);
    }

    /**
     * Convert this object into a long.
     *
     * @see Types#LONG
     *
     * @param def the default string.
     * @return    a long, default long if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Long asLong(@Nullable Long def) {
        return as(Types.LONG, def);
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
     * Convert this object into a double.
     *
     * @see Types#DOUBLE
     *
     * @return a double, null if conversion fails.
     */
    @Nullable
    default Double asDouble() {
        return as(Types.DOUBLE);
    }

    /**
     * Convert this object into a double.
     *
     * @see Types#DOUBLE
     *
     * @param def the default string.
     * @return    a double, default double if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default Double asDouble(@Nullable Double def) {
        return as(Types.DOUBLE, def);
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
     * Convert this object into a unique ID.
     *
     * @see Types#UUID
     *
     * @return a unique ID, null if conversion fails.
     */
    @Nullable
    default UUID asUniqueId() {
        return as(Types.UUID);
    }

    /**
     * Convert this object into a unique ID.
     *
     * @see Types#UUID
     *
     * @param def the default string.
     * @return    a unique ID, default unique ID if conversion fails.
     */
    @Nullable
    @Contract("!null -> !null")
    default UUID asUniqueId(@Nullable UUID def) {
        return as(Types.UUID, def);
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
