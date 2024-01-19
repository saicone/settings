package com.saicone.settings;

import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.type.ValueType;
import com.saicone.settings.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SettingsNode extends ValueType<Object> {

    default boolean isMap() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    default boolean isRoot() {
        return getParent() == null;
    }

    default boolean isReal() {
        return getKey() != null || getValue() != null;
    }

    boolean hasTopComment();

    boolean hasSideComment();

    @Nullable
    MapNode getParent();

    @Nullable
    String getKey();

    @Nullable
    Object getSourceValue();

    @Nullable
    List<String> getTopComment();

    @Nullable
    List<String> getSideComment();

    @NotNull
    default SettingsNode getRoot() {
        SettingsNode node = this;
        MapNode map;
        while ((map = node.getParent()) != null) {
            node = map;
        }
        return node;
    }

    @NotNull
    SettingsNode setParent(@Nullable MapNode parent);

    @NotNull
    SettingsNode setKey(@Nullable String key);

    @NotNull
    SettingsNode setValue(@NotNull Object value);

    @NotNull
    SettingsNode setSourceValue(@Nullable Object value);

    @NotNull
    SettingsNode setTopComment(@Nullable List<String> topComment);

    @NotNull
    SettingsNode setSideComment(@Nullable List<String> sideComment);

    @NotNull
    default SettingsNode addTopComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> topComment = getTopComment();
            if (topComment == null) {
                return setTopComment(lines);
            } else {
                topComment.addAll(lines);
            }
        }
        return this;
    }

    @NotNull
    default SettingsNode addSideComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> sideComment = getSideComment();
            if (sideComment == null) {
                return setTopComment(lines);
            } else {
                sideComment.addAll(lines);
            }
        }
        return this;
    }

    @NotNull
    default SettingsNode merge(@NotNull SettingsNode node) {
        setSourceValue(node.getSourceValue());
        return mergeComment(node);
    }

    @NotNull
    default SettingsNode mergeComment(@NotNull SettingsNode node) {
        if (getTopComment() == null) {
            setTopComment(node.getTopComment());
        }
        if (getSideComment() == null) {
            setSideComment(node.getSideComment());
        }
        return this;
    }

    default SettingsNode edit(@NotNull Consumer<SettingsNode> consumer) {
        return edit(node -> {
            consumer.accept(node);
            return node;
        });
    }

    default SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        return function.apply(this);
    }

    @NotNull
    default SettingsNode parse(@NotNull BiFunction<SettingsNode, String, Object> function) {
        return parse(null, function);
    }

    @NotNull
    default SettingsNode parse(@Nullable Predicate<String> predicate, @NotNull BiFunction<SettingsNode, String, Object> function) {
        return edit(node -> {
            if (!(node.getValue() instanceof String)) {
                return node;
            }
            final String s = (String) node.getValue();
            if (predicate != null && !predicate.test(s)) {
                return node;
            }
            return node.setValue(function.apply(node, s));
        });
    }

    @NotNull
    default SettingsNode replaceArgs(@Nullable Object... args) {
        if (args.length < 1) {
            return this;
        }
        return edit(node -> {
            if (!(node.getValue() instanceof String)) {
                return node;
            }

            final String s = (String) node.getValue();
            if (s.length() < 3 || s.indexOf('{') < 0) {
                return node;
            }

            final char[] chars = s.toCharArray();
            if (chars[0] == '{' && chars[chars.length - 1] == '}') {
                int i = 0;
                int num = 0;
                while (i + 1 < chars.length) {
                    if (!Character.isDigit(chars[i + 1])) {
                        break;
                    }
                    i++;
                    num *= 10;
                    num += chars[i] - '0';
                }
                if (i + 2 == chars.length) {
                    return args[num] == null ? node.delete() : node.setValue(args[num]);
                }
            }

            return node.setValue(Strings.replaceArgs(chars, args));
        });
    }

    @NotNull
    default SettingsNode replaceArgs(@NotNull Map<String, Object> args) {
        if (args.isEmpty()) {
            return this;
        }
        return edit(node -> {
            if (!(node.getValue() instanceof String)) {
                return node;
            }

            final String s = (String) node.getValue();
            if (s.length() < 3 || s.indexOf('{') < 0) {
                return this;
            }
            final char[] chars = s.toCharArray();
            if (chars[0] == '{' && chars[chars.length - 1] == '}') {
                final Object arg = args.get(s.substring(1, s.length() - 1));
                if (arg != null) {
                    return node.setValue(arg);
                }
            }

            return node.setValue(Strings.replaceArgs(s, chars, args));
        });
    }

    @NotNull
    default SettingsNode delete() {
        return delete(true);
    }

    @NotNull
    default SettingsNode delete(boolean deep) {
        final MapNode parent = getParent();
        if (parent != null) {
            parent.remove(getKey(), deep);
        }
        return this;
    }

    @NotNull
    default SettingsNode move(@NotNull String... path) {
        final MapNode parent = getParent();
        if (parent != null) {
            parent.remove(getKey());
            parent.set(this, path);
        } else {
            setKey(path[path.length - 1]);
        }
        return this;
    }

    @NotNull
    default MapNode asMapNode() {
        return (MapNode) this;
    }

    @NotNull
    default ListNode asListNode() {
        return (ListNode) this;
    }

    @NotNull
    default Object asLiteralObject() {
        return getValue();
    }
}
