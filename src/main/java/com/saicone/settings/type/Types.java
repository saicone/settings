package com.saicone.settings.type;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to collect common and registrable type parsers.
 *
 * @author Rubenicos
 */
public class Types {

    private static final Map<Class<?>, TypeParser<?>> PARSER_MAP = new HashMap<>();
    /**
     * Type parser to return any object itself.
     */
    public static final TypeParser<Object> OBJECT = (object) -> object;
    /**
     * String type parser.
     *
     * @see String#valueOf(Object)
     */
    public static final TypeParser<String> STRING = TypeParser.single(String::valueOf);
    /**
     * Text type parser, instead of {@link Types#STRING} this parser calls {@link Arrays#toString(Object[])}
     * if the provided object is instance of object array.
     */
    public static final TypeParser<String> TEXT = (object) ->{
        if (object instanceof Object[]) {
            return Arrays.toString((Object[]) object);
        } else {
            return String.valueOf(object);
        }
    };
    /**
     * Character type parser.<br>
     * This parser extracts the first character from non-empty String value.
     */
    public static final TypeParser<Character> CHAR = TypeParser.single((object) -> {
        final String s = String.valueOf(object);
        return s.isEmpty() ? null : s.charAt(0);
    });
    /**
     * Boolean type parser.<br>
     * This parser accept any String representation of:<br>
     * true | false<br>
     * t | f<br>
     * 1 | 0<br>
     * yes | no<br>
     * on | off<br>
     * y | n
     */
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
    /**
     * Byte type parser.
     */
    public static final TypeParser<Byte> BYTE = TypeParser.number((object) -> object instanceof Number ? ((Number) object).byteValue() : Byte.parseByte(String.valueOf(object)));
    /**
     * Short type parser.
     */
    public static final TypeParser<Short> SHORT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).shortValue() : Short.parseShort(String.valueOf(object)));
    /**
     * Integer type parser.
     */
    public static final TypeParser<Integer> INT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).intValue() : Integer.parseInt(String.valueOf(object)));
    /**
     * Float type parser.
     */
    public static final TypeParser<Float> FLOAT = TypeParser.number((object) -> object instanceof Number ? ((Number) object).floatValue() : Float.parseFloat(String.valueOf(object)));
    /**
     * Long type parser.
     */
    public static final TypeParser<Long> LONG = TypeParser.number((object) -> object instanceof Number ? ((Number) object).longValue() : Long.parseLong(String.valueOf(object)));
    /**
     * Double type parser.
     */
    public static final TypeParser<Double> DOUBLE = TypeParser.number((object) -> object instanceof Number ? ((Number) object).doubleValue() : Double.parseDouble(String.valueOf(object)));
    /**
     * Unique ID type parser.<br>
     * This parser accepts any String representation of unique ID and also 4-length primitive int array.
     */
    public static final TypeParser<java.util.UUID> UUID = TypeParser.first((object) -> {
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

    /**
     * Register provided type parser associated by class type.
     *
     * @param type   the key class type.
     * @param parser the type parser.
     * @return       the previous type parsed associated with provided class.
     * @param <T>    the type result of the parser.
     */
    @Nullable
    public static <T> TypeParser<?> add(@NotNull Class<T> type, @NotNull TypeParser<T> parser) {
        return PARSER_MAP.put(type, parser);
    }

    /**
     * Remove any registered type parsed associated by class.
     *
     * @param type the class key.
     * @return     the previous type parsed associated with provided class.
     */
    @Nullable
    public static TypeParser<?> remove(@NotNull Class<?> type) {
        return PARSER_MAP.remove(type);
    }

    /**
     * Parse any object by providing a type class to find previously registered type parser.<br>
     * This method can also parse into primitive class objects and instead of null the failed
     * value to return will be MIN value for number types, FALSE for boolean and empty for char.
     *
     * @param type   the class type.
     * @param object the object to parse.
     * @return       a converted value type, null otherwise.
     * @param <T>    the type result.
     */
    @Nullable
    public static <T> T parse(@NotNull Class<T> type, @Nullable Object object) {
        return parse(type, object, null);
    }

    /**
     * Parse any object by providing a type class to find previously registered type parser.<br>
     * This method can also parse into primitive class objects and instead of default object the failed
     * value to return will be MIN value for number types, FALSE for boolean and empty for char.
     *
     * @param type   the class type.
     * @param object the object to parse.
     * @param def    the type object to return if parser fails or doesn't exist.
     * @return       a converted value type, default object otherwise.
     * @param <T>    the type result.
     */
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

    /**
     * Get the previously registered type parser from class type.
     *
     * @param type the class type.
     * @return     the registered type parser if found, parser by cast object otherwise.
     * @param <T>  the type result of the parser.
     */
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
