package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.type.TypeParser;
import com.saicone.settings.type.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class NodeValue<V> implements SettingsNode {

    private Object faceValue;
    private Object sourceValue;

    private List<String> topComment;
    private List<String> sideComment;

    // Parsed value cache
    private transient TypeParser<?> typeParser;
    private transient Object parsedValue;

    @NotNull
    public static SettingsNode of(@Nullable Object object) {
        if (object instanceof SettingsNode) {
            return of(((SettingsNode) object).getValue()).mergeComment((SettingsNode) object);
        }
        if (object instanceof Map) {
            return new MapNode().merge((Map<?, ?>) object, true);
        } else if (object instanceof Iterable) {
            final List<SettingsNode> list = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                list.add(of(o));
            }
            return new ListNode(list);
        } else {
            return new ObjectNode(object);
        }
    }

    public NodeValue(@Nullable V value) {
        this.sourceValue = value;
    }

    @Override
    public boolean hasTopComment() {
        return this.topComment != null && !this.topComment.isEmpty();
    }

    @Override
    public boolean hasSideComment() {
        return this.sideComment != null && !this.sideComment.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getValue() {
        return (V) (faceValue != null ? faceValue : sourceValue);
    }

    @Nullable
    public Object getFaceValue() {
        return faceValue;
    }

    @Override
    public @Nullable Object getSourceValue() {
        return sourceValue;
    }

    @Override
    public @Nullable List<String> getTopComment() {
        return topComment;
    }

    @Override
    public @Nullable List<String> getSideComment() {
        return sideComment;
    }

    @NotNull
    @Override
    public SettingsNode setValue(@NotNull Object value) {
        if (this.sourceValue == null) {
            this.sourceValue = value;
        } else {
            this.faceValue = value;
        }
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setSourceValue(@Nullable Object value) {
        this.sourceValue = value;
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setTopComment(@Nullable List<String> topComment) {
        this.topComment = topComment;
        return this;
    }

    @NotNull
    @Override
    public SettingsNode setSideComment(@Nullable List<String> sideComment) {
        this.sideComment = sideComment;
        return this;
    }

    @Override
    public @NotNull SettingsNode replaceArgs(@Nullable Object... args) {
        if (!(getValue() instanceof String)) {
            return this;
        }
        final String s = (String) getValue();
        if (s.trim().isEmpty()) {
            return this;
        } else if (args.length < 1) {
            setValue(s.replace("{#}", "0").replace("{*}", "[]").replace("{-}", ""));
            return this;
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());

        String all = null;
        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (Character.isDigit(chars[i + 1])) {
                        i++;
                        num *= 10;
                        num += chars[i] - '0';
                        continue;
                    }
                    if (i == mark) {
                        final char c = chars[i + 1];
                        if (c == '#') {
                            i++;
                            num = -1;
                        } else if (c == '*') {
                            i++;
                            num = -2;
                        } else if (c == '-') {
                            i++;
                            num = -3;
                        }
                    }
                    break;
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num == -1) {
                        builder.append(args.length);
                    } else if (num == -2) {
                        builder.append(Arrays.toString(args));
                    } else if (num == -3) {
                        if (all == null) {
                            all = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(" "));
                        }
                        builder.append(all);
                    } else if (num < args.length) { // Avoid IndexOutOfBoundsException
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

        setValue(builder.toString());
        return this;
    }

    @Override
    public @NotNull SettingsNode replaceArgs(@NotNull Map<String, Object> args) {
        if (!(getValue() instanceof String)) {
            return this;
        }
        final String s = (String) getValue();
        if (s.trim().isEmpty() || args.isEmpty()) {
            return this;
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());

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
                    if (i > mark1) {
                        builder.replace(mark, i, s.substring(mark1, i));
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

        setValue(builder.toString());
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> @Nullable E as(@NotNull Class<E> type, @Nullable E def) {
        if (type.isInstance(this.parsedValue)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = Types.parse(type, getValue(), def);
        if (parsedValue != null) {
            this.parsedValue = parsedValue;
        }
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> @Nullable E as(@NotNull TypeParser<E> parser, @Nullable E def) {
        if (parser.equals(this.typeParser)) {
            return (E) this.parsedValue;
        }
        final E parsedValue = parser.parse(getValue(), def);
        if (parsedValue != null) {
            this.typeParser = parser;
            this.parsedValue = parsedValue;
        }
        return parsedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, C extends Collection<E>> @NotNull C asCollection(@NotNull C collection, @NotNull TypeParser<E> parser) {
        if (parser.equals(this.typeParser) && collection.getClass().isInstance(this.parsedValue)) {
            return (C) this.parsedValue;
        }
        final C parsedValue = parser.collection(collection, getValue());
        this.typeParser = parser;
        this.parsedValue = parsedValue;
        return parsedValue;
    }
}