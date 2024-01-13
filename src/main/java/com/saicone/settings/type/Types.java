package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Types {

    private static final Map<Class<?>, TypeParser<?>> PARSER_MAP = new HashMap<>();
    public static final TypeParser<Object> OBJECT = (object) -> object;
    public static final TypeParser<String> STRING = TypeParser.single(String::valueOf);
    public static final TypeParser<String> TEXT = (object) ->{
        if (object instanceof Object[]) {
            return Arrays.toString((Object[]) object);
        } else {
            return String.valueOf(object);
        }
    };
    public static final TypeParser<Character> CHAR = TypeParser.single((object) -> {
        final String s = String.valueOf(object);
        return s.trim().isEmpty() ? null : s.charAt(0);
    });
    public static final TypeParser<Boolean> BOOLEAN = TypeParser.single((object) -> {
        switch (String.valueOf(object instanceof Number ? ((Number) object).intValue() : object).toLowerCase()) {
            case "true":
            case "t":
            case "1":
            case "yes":
            case "on":
            case "y":
                return true;
            case "false":
            case "f":
            case "0":
            case "no":
            case "off":
            case "n":
                return false;
            default:
                return null;
        }
    });
    public static final TypeParser<Byte> BYTE = TypeParser.number((object) -> object instanceof Number ? ((Number) object).byteValue() : Byte.parseByte(String.valueOf(object)));
    public static final TypeParser<Short> SHORT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).shortValue() : Short.parseShort(String.valueOf(object)));
    public static final TypeParser<Integer> INT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(String.valueOf(object)));
    public static final TypeParser<Float> FLOAT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(String.valueOf(object)));
    public static final TypeParser<Long> LONG = TypeParser.number((object) -> object instanceof Number ? ((Number) object).longValue() : Long.parseLong(String.valueOf(object)));
    public static final TypeParser<Double> DOUBLE = TypeParser.number((object) -> object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble(String.valueOf(object)));
    public static final TypeParser<UUID> UUID = TypeParser.single((object) -> {
        if (object instanceof int[]) {
            final int[] array = (int[]) object;
            if (array.length == 4) {
                StringBuilder builder = new StringBuilder();
                for (int i : array) {
                    String hex = Integer.toHexString(i);
                    builder.append(new String(new char[8 - hex.length()]).replace('\0', '0')).append(hex);
                }
                if (builder.length() == 32) {
                    builder.insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-');
                    return java.util.UUID.fromString(builder.toString());
                } else {
                    throw new IllegalArgumentException("The final converted UUID '" + builder + "' isn't a 32-length string");
                }
            }
        } else if (object instanceof String) {
            return java.util.UUID.fromString((String) object);
        }
        return null;
    });

    static {
        add(Object.class, OBJECT);
        add(String.class, STRING);
        add(Character.class, CHAR);
        add(char.class, (object) -> {
            final Character c = CHAR.parse(object);
            return c != null ? c : '\0';
        });
        add(Boolean.class, BOOLEAN);
        add(boolean.class, (object) -> {
            final Boolean bool = BOOLEAN.parse(object);
            return bool != null ? bool : Boolean.FALSE;
        });
        add(Byte.class, BYTE);
        add(byte.class, object -> {
            final Byte num = BYTE.parse(object);
            return num != null ? num : Byte.MIN_VALUE;
        });
        add(Short.class, SHORT);
        add(short.class, object -> {
            final Short num = SHORT.parse(object);
            return num != null ? num : Short.MIN_VALUE;
        });
        add(Integer.class, INT);
        add(int.class, object -> {
            final Integer num = INT.parse(object);
            return num != null ? num : Integer.MIN_VALUE;
        });
        add(Float.class, FLOAT);
        add(float.class, object -> {
            final Float num = FLOAT.parse(object);
            return num != null ? num : Float.MIN_VALUE;
        });
        add(Long.class, LONG);
        add(long.class, object -> {
            final Long num = LONG.parse(object);
            return num != null ? num : Long.MIN_VALUE;
        });
        add(Double.class, DOUBLE);
        add(double.class, object -> {
            final Double num = DOUBLE.parse(object);
            return num != null ? num : Double.MIN_VALUE;
        });
        add(java.util.UUID.class, UUID);
    }

    Types() {
    }

    @Nullable
    public static <T> TypeParser<?> add(@NotNull Class<T> type, @NotNull TypeParser<T> parser) {
        return PARSER_MAP.put(type, parser);
    }

    @Nullable
    public static TypeParser<?> remove(@NotNull Class<?> type) {
        return PARSER_MAP.remove(type);
    }

    @Nullable
    public static <T> T parse(@NotNull Class<T> type, @Nullable Object object) {
        return parse(type, object, null);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    @SuppressWarnings("unchecked")
    public static <T> T parse(@NotNull Class<T> type, @Nullable Object object, @Nullable T def) {
        final TypeParser<?> parser = PARSER_MAP.get(type);
        if (parser == null) {
            return def;
        }
        return ((TypeParser<T>) parser).parse(object, def);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> TypeParser<T> parser(@NotNull Class<T> type) {
        final TypeParser<?> parser = PARSER_MAP.get(type);
        if (parser == null) {
            return (object) -> (T) object;
        }
        return (TypeParser<T>) parser;
    }
}
