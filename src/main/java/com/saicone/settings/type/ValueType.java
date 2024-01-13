package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface ValueType<T> {

    T getValue();

    @Nullable
    default <E> E as(@NotNull Class<E> type) {
        return as(type, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    default <E> E as(@NotNull Class<E> type, @Nullable E def) {
        return Types.parse(type, getValue(), def);
    }

    @Nullable
    default <E> E as(@NotNull TypeParser<E> parser) {
        return as(parser, null);
    }

    @Nullable
    @Contract("_, !null -> !null")
    default <E> E as(@NotNull TypeParser<E> parser, @Nullable E def) {
        return parser.parse(getValue(), def);
    }

    @NotNull
    default <E, C extends Collection<E>> C asCollection(@NotNull C collection, @NotNull TypeParser<E> parser) {
        return parser.collection(collection, getValue());
    }

    @NotNull
    default List<Object> asList() {
        return asList(Types.OBJECT);
    }

    @NotNull
    default <E> List<E> asList(@NotNull TypeParser<E> parser) {
        return asCollection(new ArrayList<>(), parser);
    }

    @NotNull
    default <E> Set<E> asSet(@NotNull TypeParser<E> parser) {
        return asCollection(new HashSet<>(), parser);
    }

    @Nullable
    default String asString() {
        return as(Types.STRING);
    }

    @Nullable
    @Contract("!null -> !null")
    default String asString(@Nullable String def) {
        return as(Types.STRING, def);
    }

    @NotNull
    default List<String> asStringList() {
        return asList(Types.STRING);
    }

    @Nullable
    default Character asChar() {
        return as(Types.CHAR);
    }

    @Nullable
    @Contract("!null -> !null")
    default Character asChar(@Nullable Character def) {
        return as(Types.CHAR, def);
    }

    @NotNull
    default List<Character> asCharList() {
        return asList(Types.CHAR);
    }

    @Nullable
    default Boolean asBoolean() {
        return as(Types.BOOLEAN);
    }

    @Nullable
    @Contract("!null -> !null")
    default Boolean asBoolean(@Nullable Boolean def) {
        return as(Types.BOOLEAN, def);
    }

    @NotNull
    default List<Boolean> asBooleanList() {
        return asList(Types.BOOLEAN);
    }

    @Nullable
    default Byte asByte() {
        return as(Types.BYTE);
    }

    @Nullable
    @Contract("!null -> !null")
    default Byte asByte(@Nullable Byte def) {
        return as(Types.BYTE, def);
    }

    @NotNull
    default List<Byte> asByteList() {
        return asList(Types.BYTE);
    }

    @Nullable
    default Short asShort() {
        return as(Types.SHORT);
    }

    @Nullable
    @Contract("!null -> !null")
    default Short asShort(@Nullable Short def) {
        return as(Types.SHORT, def);
    }

    @NotNull
    default List<Short> asShortList() {
        return asList(Types.SHORT);
    }

    @Nullable
    default Integer asInt() {
        return as(Types.INT);
    }

    @Nullable
    @Contract("!null -> !null")
    default Integer asInt(@Nullable Integer def) {
        return as(Types.INT, def);
    }

    @NotNull
    default List<Integer> asIntList() {
        return asList(Types.INT);
    }

    @Nullable
    default Float asFloat() {
        return as(Types.FLOAT);
    }

    @Nullable
    @Contract("!null -> !null")
    default Float asFloat(@Nullable Float def) {
        return as(Types.FLOAT, def);
    }

    @NotNull
    default List<Float> asFloatList() {
        return asList(Types.FLOAT);
    }

    @Nullable
    default Long asLong() {
        return as(Types.LONG);
    }

    @Nullable
    @Contract("!null -> !null")
    default Long asLong(@Nullable Long def) {
        return as(Types.LONG, def);
    }

    @NotNull
    default List<Long> asLongList() {
        return asList(Types.LONG);
    }

    @Nullable
    default Double asDouble() {
        return as(Types.DOUBLE);
    }

    @Nullable
    @Contract("!null -> !null")
    default Double asDouble(@Nullable Double def) {
        return as(Types.DOUBLE, def);
    }

    @NotNull
    default List<Double> asDoubleList() {
        return asList(Types.DOUBLE);
    }

    @Nullable
    default UUID asUniqueId() {
        return as(Types.UUID);
    }

    @Nullable
    @Contract("!null -> !null")
    default UUID asUniqueId(@Nullable UUID def) {
        return as(Types.UUID, def);
    }

    @NotNull
    default List<UUID> asUniqueIdList() {
        return asList(Types.UUID);
    }
}
