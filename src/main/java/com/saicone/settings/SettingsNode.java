package com.saicone.settings;

import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.type.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SettingsNode extends ValueType<Object> {

    default boolean isMap() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    boolean isRoot();

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

    @NotNull
    default SettingsNode edit(@NotNull Consumer<SettingsNode> consumer) {
        return edit(node -> {
            consumer.accept(node);
            return node;
        });
    }

    @NotNull
    default SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        return function.apply(this);
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
            if (s.trim().isEmpty()) {
                return this;
            }
            final char[] chars = s.toCharArray();
            final StringBuilder builder = new StringBuilder(s.length());

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

            return node.setValue(builder.toString());
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
            if (s.trim().isEmpty()) {
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

            return node.setValue(builder.toString());
        });
    }

    default void delete() {
        delete(true);
    }

    default void delete(boolean deep) {
        final MapNode parent = getParent();
        if (parent != null) {
            parent.remove(getKey(), deep);
        }
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
}
