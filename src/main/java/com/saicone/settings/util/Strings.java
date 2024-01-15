package com.saicone.settings.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Strings {

    Strings() {
    }

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

    @NotNull
    public static String replaceArgs(@NotNull String s, @Nullable Object... args) {
        return replaceArgs(s.toCharArray(), args);
    }

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

    @NotNull
    public static String replaceArgs(@NotNull String s, @NotNull Map<String, Object> args) {
        return replaceArgs(s, s.toCharArray(), args);
    }

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
