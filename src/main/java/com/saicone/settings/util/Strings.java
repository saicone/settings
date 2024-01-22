package com.saicone.settings.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class to handle strings with a variety of methods.
 *
 * @author Rubenicos
 */
public class Strings {

    Strings() {
    }

    /**
     * Splits provided string around of the given character, ignoring any literal char declaration such as {@code \.} (dot).
     *
     * @param s the string to split.
     * @param c the char that split
     * @return  the array of strings computed by splitting this string around of the given character.
     */
    @NotNull
    public static String[] split(@NotNull String s, char c) {
        int end = s.indexOf(c);
        if (end < 1) {
            return new String[] { s };
        }
        int start = 0;
        final List<String> list = new ArrayList<>();
        while (end > 0) {
            if (s.charAt(end - 1) != '\\') {
                list.add(s.substring(start, end).replace("\\" + c, String.valueOf(c)));
                start = end + 1;
            }
            end = s.indexOf(c, end + 1);
        }
        list.add(s.substring(start));
        return list.toArray(new String[0]);
    }

    /**
     * Replace every argument denoted by its index value ({0}, {1}, {2}...) inside provided string.
     *
     * @param s    the string to replace arguments.
     * @param args the arguments to be used as replacements.
     * @return     the string with arguments replaced.
     */
    @NotNull
    public static String replaceArgs(@NotNull String s, @Nullable Object... args) {
        return replaceArgs(s.toCharArray(), args);
    }

    /**
     * Replace every argument denoted by its index value ({0}, {1}, {2}...) inside provided characters.
     *
     * @param chars the characters to replace arguments.
     * @param args  the arguments to be used as replacements.
     * @return      the built string with arguments replaced.
     */
    @NotNull
    public static String replaceArgs(char[] chars, @Nullable Object... args) {
        final StringBuilder builder = new StringBuilder(chars.length);

        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (!Character.isDigit(chars[i + 1])) {
                        break;
                    }
                    i++;
                    num *= 10;
                    num += chars[i] - '0';
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num < args.length) { // Avoid IndexOutOfBoundsException
                        builder.append(args[num]);
                    } else {
                        builder.append('{').append(num).append('}');
                    }
                } else {
                    i = mark;
                }
            }
            if (mark == i) {
                builder.append(chars[i]);
            }
        }

        return builder.toString();
    }

    /**
     * Replace every argument denoted by its key value ({key}, {asd}, {name}...) inside provided string.
     *
     * @param s    the string to replace arguments.
     * @param args the arguments to be used as replacements.
     * @return     the string with arguments replaced.
     */
    @NotNull
    public static String replaceArgs(@NotNull String s, @NotNull Map<String, Object> args) {
        return replaceArgs(s, s.toCharArray(), args);
    }

    /**
     * Replace every argument denoted by its key value ({key}, {asd}, {name}...) inside provided characters.
     *
     * @param s     the string to be used as char index provider.
     * @param chars the characters to replace arguments.
     * @param args  the arguments to be used as replacements.
     * @return      the built string with arguments replaced.
     */
    @NotNull
    public static String replaceArgs(@NotNull String s, char[] chars, @NotNull Map<String, Object> args) {
        final StringBuilder builder = new StringBuilder(chars.length);

        int mark = 0;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];

            builder.append(c);
            if (c != '{' || i + 1 >= chars.length) {
                mark++;
                continue;
            }

            final int mark1 = i + 1;
            while (++i < chars.length) {
                final char c1 = chars[i];
                if (c1 == '}') {
                    final Object arg;
                    if (i > mark1 && (arg = args.get(s.substring(mark1, i))) != null) {
                        builder.replace(mark, i, String.valueOf(arg));
                    } else {
                        builder.append(c1);
                    }
                    break;
                } else {
                    builder.append(c1);
                    if (i + 1 < chars.length && chars[i + 1] == '{') {
                        break;
                    }
                }
            }

            mark = builder.length();
        }

        return builder.toString();
    }
}
