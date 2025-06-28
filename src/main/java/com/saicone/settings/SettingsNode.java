package com.saicone.settings;

import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.node.NodeKey;
import com.saicone.settings.node.ObjectNode;
import com.saicone.settings.util.Strings;
import com.saicone.types.ValueType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface that represents the global settings memory object.
 *
 * @author Rubenicos
 */
public interface SettingsNode extends ValueType<Object> {

    /**
     * Check if the current node is not a map or list.
     *
     * @return true if the node is an object node.
     */
    default boolean isObject() {
        return false;
    }

    /**
     * Check if the current node is a map instance.
     *
     * @return true if the node is a map node.
     */
    default boolean isMap() {
        return false;
    }

    /**
     * Check if the current node is a list instance.
     *
     * @return true if the node is a list node.
     */
    default boolean isList() {
        return false;
    }

    /**
     * Check if the current node is the root node.
     *
     * @return true if any parent node is present.
     */
    default boolean isRoot() {
        return getParent() == null;
    }

    /**
     * Check if the current node is a real node.
     *
     * @return true if the node at least contains a real key or value.
     */
    default boolean isReal() {
        return getKey() != null || getValue() != null;
    }

    /**
     * Check if the current node is empty.
     *
     * @return true if the node contains no value or elements inside.
     */
    default boolean isEmpty() {
        return getValue() == null;
    }

    /**
     * Check if the current node has any top comment.
     *
     * @return true if the node contains a top comment.
     */
    boolean hasTopComment();

    /**
     * Check if the current node has any side comment.
     *
     * @return true if the node contains a side comment.
     */
    boolean hasSideComment();

    /**
     * Get the parent node that this node is child of.
     *
     * @return a map node if exists, null otherwise.
     */
    @Nullable
    MapNode getParent();

    /**
     * Get the node key associated with this node.
     *
     * @return a node key if is set, null otherwise.
     */
    @Nullable
    String getKey();

    /**
     * Get the value that will be used to save this node.
     *
     * @return a saved source value, null otherwise.
     */
    @Nullable
    Object getSourceValue();

    /**
     * Get top comment from the node.
     *
     * @return a comment if is set, null otherwise.
     */
    @Nullable
    List<String> getTopComment();

    /**
     * Get side comment from the node.
     *
     * @return a comment if is set, null otherwise.
     */
    @Nullable
    List<String> getSideComment();

    /**
     * Get root value from node tree.
     *
     * @return a recursively parent node get, this node otherwise.
     */
    @NotNull
    default SettingsNode getRoot() {
        SettingsNode node = this;
        MapNode map;
        while ((map = node.getParent()) != null) {
            node = map;
        }
        return node;
    }

    /**
     * Replace the parent node that this node come from.
     *
     * @param parent a map node.
     * @return       the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setParent(@Nullable MapNode parent);

    /**
     * Replace the key associated with this node.
     *
     * @param key a node key.
     * @return    the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setKey(@Nullable String key);

    /**
     * Set the value that will be return on {@link #getValue()} and change
     * this node instance depending on actual value.<br>
     * If source value is not set will be replaced.
     *
     * @param value the value to set.
     * @return      the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setValue(@NotNull Object value);

    /**
     * Replace the actual source value.
     *
     * @param value the source value to set.
     * @return      the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setSourceValue(@Nullable Object value);

    /**
     * Replace the node top comment.
     *
     * @param topComment the comment to set.
     * @return           the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setTopComment(@Nullable List<String> topComment);

    /**
     * Replace the node side comment.
     *
     * @param sideComment the comment to set.
     * @return            the effective node in this operation, normally this node.
     */
    @NotNull
    SettingsNode setSideComment(@Nullable List<String> sideComment);

    /**
     * Add comment lines to the actual top comment.
     *
     * @param lines the lines to be added.
     * @return      the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode addTopComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> topComment = getTopComment();
            if (topComment == null) {
                return setTopComment(new ArrayList<>(lines));
            } else {
                topComment.addAll(lines);
            }
        }
        return this;
    }

    /**
     * Add comment lines to the actual side comment.
     *
     * @param lines the lines to be added.
     * @return      the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode addSideComment(@Nullable List<String> lines) {
        if (lines != null) {
            final List<String> sideComment = getSideComment();
            if (sideComment == null) {
                return setSideComment(new ArrayList<>(lines));
            } else {
                sideComment.addAll(lines);
            }
        }
        return this;
    }

    /**
     * Merge to provided node information into current node.<br>
     * Only node source value and comments will be merged.
     *
     * @param node the node to get values.
     * @return     the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode merge(@NotNull SettingsNode node) {
        setSourceValue(node.getSourceValue());
        return mergeComment(node);
    }

    /**
     * Merge the provided node comments into current node.
     *
     * @param node the node to get comments.
     * @return     the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode mergeComment(@NotNull SettingsNode node) {
        if (getTopComment() == null && node.getTopComment() != null) {
            final List<String> comment = new ArrayList<>();
            comment.addAll(node.getTopComment());
            setTopComment(comment);
        }
        if (getSideComment() == null && node.getSideComment() != null) {
            final List<String> comment = new ArrayList<>();
            comment.addAll(node.getSideComment());
            setSideComment(comment);
        }
        return this;
    }

    /**
     * Edit every object inside this node.<br>
     * This means if this node is a list or map type will be iterated recursively
     * to edit every child or element inside map or list with the provided consumer.
     *
     * @param consumer the consumer that accepts every node object.
     * @return         the effective node in this operation, normally this node.
     */
    default SettingsNode edit(@NotNull Consumer<SettingsNode> consumer) {
        return edit(node -> {
            consumer.accept(node);
            return node;
        });
    }

    /**
     * Edit every object inside this node.<br>
     * This means if this node is a list or map type will be iterated recursively
     * to edit every child or element inside map or list with the provided function.<br>
     * Take in count you can return a null value from function to delete map or list value,
     * and also return a new node to replace the provided map or list value.
     *
     * @param function the function that accepts every node and return itself, null o new node.
     * @return         the effective node in this operation, normally this node.
     */
    default SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        return function.apply(this);
    }

    /**
     * Parse every text value inside this node.<br>
     * This means if this node is a list or map type will be iterated recursively to parse
     * every string on child or element inside map or list with the provided function.<br>
     * Take in count only the value from {@link #getValue()} will be used to check if the
     * node is a string or not.
     *
     * @param function the function to parse every provided string value.
     * @return         the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode parse(@NotNull BiFunction<SettingsNode, String, Object> function) {
        return parse(null, function);
    }

    /**
     * Parse every text value inside this node with a string condition.<br>
     * This means if this node is a list or map type will be iterated recursively to parse
     * every string on child or element inside map or list with the provided function.<br>
     * Take in count only the value from {@link #getValue()} will be used to check if the
     * node is a string or not.
     *
     * @param predicate the condition to check if string can be parsed.
     * @param function  the function to parse every provided string value.
     * @return          the effective node in this operation, normally this node.
     */
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

    /**
     * Replace every argument denoted by its index value ({0}, {1}, {2}...) inside every text value in this node.<br>
     * This means if this node is a list or map type will be iterated recursively to replace indexed arguments from
     * every string on child or element inside map or list with the provided function.<br>
     * Take in count only the value from {@link #getValue()} will be used to check if the node is a string or not.
     *
     * @param args the arguments to be used as replacements.
     * @return     the effective node in this operation, normally this node.
     */
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

    /**
     * Replace every argument denoted by its key value ({key}, {asd}, {name}...) inside every text value in this node.<br>
     * This means if this node is a list or map type will be iterated recursively to replace indexed arguments from
     * every string on child or element inside map or list with the provided function.<br>
     * Take in count only the value from {@link #getValue()} will be used to check if the node is a string or not.
     *
     * @param args the arguments to be used as replacements.
     * @return     the effective node in this operation, normally this node.
     */
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

    /**
     * Delete this node from any parent node.
     *
     * @return the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode delete() {
        return delete(true);
    }

    /**
     * Delete this node from any parent node.
     *
     * @param deep true to delete any empty parent path.
     * @return     the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode delete(boolean deep) {
        final MapNode parent = getParent();
        if (parent != null && getKey() != null) {
            parent.remove(getKey(), deep);
        }
        return this;
    }

    /**
     * Move this node to other key path.<br>
     * This operation only will be effective if the current node contains a parent node,
     * otherwise only it's key will be updated with latest path key.
     *
     * @param path the new node path.
     * @return     the effective node in this operation, normally this node.
     */
    @NotNull
    default SettingsNode move(@NotNull String... path) {
        final MapNode parent = getParent();
        if (parent != null) {
            if (getKey() != null) {
                parent.remove(getKey(), true);
            }
            parent.set(this, path);
        } else {
            return setKey(path[path.length - 1]);
        }
        return this;
    }

    /**
     * Copy this node into new one and return the clone itself.
     *
     * @return       a clone of this node.
     */
    @NotNull
    default SettingsNode copy() {
        return copy(false);
    }

    /**
     * Copy this node into new one and return the clone itself.
     *
     * @param parent true to save parent information.
     * @return       a clone of this node.
     */
    @NotNull
    default SettingsNode copy(boolean parent) {
        return copy(parent, true);
    }

    /**
     * Copy this node into new one and return the clone itself.
     *
     * @param parent true to save parent information.
     * @param key    true to save key information.
     * @return       a clone of this node.
     */
    @NotNull
    default SettingsNode copy(boolean parent, boolean key) {
        return NodeKey.of(parent ? getParent() : null, key ? getKey() : null, this);
    }

    /**
     * Cast this node into an object node type.
     *
     * @return this node.
     */
    @NotNull
    default ObjectNode asObjectNode() {
        return (ObjectNode) this;
    }

    /**
     * Cast this node into a map node type.
     *
     * @return this node.
     */
    @NotNull
    default MapNode asMapNode() {
        return (MapNode) this;
    }

    /**
     * Cast this node into a list node type.
     *
     * @return this node.
     */
    @NotNull
    default ListNode asListNode() {
        return (ListNode) this;
    }

    /**
     * Get the literal object represented by this node, in other words, if the node is a map of nodes, a map of
     * objects wil be return, if the node is a list of nodes, a list of objects will be return, otherwise the
     * value from {@link #getValue()} will be return.<br>
     * This is an expensive operation due every child node or list element will be return as it's literal object
     * recursively, so it's not suggested to use this method frequently.
     *
     * @return the literal object represented by this node.
     */
    @NotNull
    default Object asLiteralObject() {
        return getValue();
    }
}
