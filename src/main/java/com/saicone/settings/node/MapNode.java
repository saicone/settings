package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.util.Strings;
import com.saicone.types.IterableType;
import com.saicone.types.ValueType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Class to handle a map of settings nodes.<br>
 * This object can also be handled as regular Java map and inherited in enhanced for loop.
 *
 * @author Rubenicos
 */
public class MapNode extends NodeKey<Map<String, SettingsNode>> implements Map<String, SettingsNode>, Iterable<Map.Entry<String, SettingsNode>> {

    /**
     * Constructs an empty map of nodes.
     */
    public MapNode() {
        this(new LinkedHashMap<>());
    }

    /**
     * Constructs a map node with the given map value.
     *
     * @param value  the object to wrap as map node.
     */
    public MapNode(@Nullable Map<String, SettingsNode> value) {
        this(null, null, value);
    }

    /**
     * Constructs a map node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     */
    public MapNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, new LinkedHashMap<>());
    }

    /**
     * Constructs a map node with the given parameters.
     *
     * @param parent the parent node.
     * @param key    the node key.
     * @param value  the object to wrap as map node.
     */
    public MapNode(@Nullable MapNode parent, @Nullable String key, @Nullable Map<String, SettingsNode> value) {
        super(parent, key, value);
    }

    @Override
    public boolean isMap() {
        return true;
    }

    /**
     * Get the node associated with the given key.
     *
     * @param key the node key.
     * @return    a node from the map or a newly created instead.
     */
    @NotNull
    public SettingsNode get(@NotNull String key) {
        return getIf(s -> s.equals(key), key);
    }

    /**
     * Get the node associated with the given key path.
     *
     * @param path the node path.
     * @return     a node from any sub map or a newly created instead.
     */
    @NotNull
    public SettingsNode get(@NotNull String... path) {
        if (path.length == 1) {
            return get(path[0]);
        }
        return getIf(String::equals, path);
    }

    /**
     * Get the node associated with the given key ignoring case considerations.
     *
     * @param key the node key.
     * @return    a node from the map or a newly created instead.
     */
    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String key) {
        return getIf(s -> s.equalsIgnoreCase(key), key);
    }

    /**
     * Get the node associated with the given key path ignoring case considerations.
     *
     * @param path the node path.
     * @return     a node from any sub map or a newly created instead.
     */
    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String... path) {
        if (path.length == 1) {
            return getIgnoreCase(path[0]);
        }
        return getIf(String::equalsIgnoreCase, path);
    }

    /**
     * Get the node whose key matches with given regex expression.<br>
     * Instead of {@link #get(String)} or {@link #getIgnoreCase(String)} this method
     * may create a new node without any defined key.
     *
     * @param regex the expression to be compiled.
     * @return      a node from the map or a newly created without any defined key.
     */
    @NotNull
    public SettingsNode getRegex(@NotNull @Language("RegExp") String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return getIf(s -> pattern.matcher(s).matches(), null);
    }

    /**
     * Get the node whose key path matches with given regex expressions.<br>
     * Instead of {@link #get(String...)} or {@link #getIgnoreCase(String...)} this method
     * may create a new node without any defined key.
     *
     * @param regexPath the expressions to be compiled.
     * @return          a node from any sub map or a newly created without any defined key.
     */
    @NotNull
    public SettingsNode getRegex(@NotNull @Language("RegExp") String... regexPath) {
        if (regexPath.length == 1) {
            return getRegex(regexPath[0]);
        }
        return getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath);
    }

    /**
     * Get the node associated with the given path.<br>
     * This method will split by dots the provided path into keys
     * to be used with {@link #get(String...)}.
     *
     * @param path the node path.
     * @return     a node from current map, any sub map or a newly created instead.
     */
    @NotNull
    public SettingsNode getSplit(@NotNull Object path) {
        return get(Strings.split(String.valueOf(path), '.'));
    }

    /**
     * Get a node by applying a key comparison.
     *
     * @param condition the key predicate.
     * @param key       the expected key.
     * @return          a node whose key passes the condition.
     */
    @NotNull
    protected SettingsNode getIf(@NotNull Predicate<String> condition, @Nullable String key) {
        for (Entry<String, SettingsNode> entry : getValue().entrySet()) {
            if (condition.test(entry.getKey())) {
                return entry.getValue();
            }
        }
        if (key != null) {
            return child(key);
        }
        return child();
    }

    /**
     * Get a node by applying a key comparison.
     *
     * @param condition the key comparator.
     * @param path      the node path.
     * @return          a node whose key path passes the condition.
     */
    @NotNull
    protected SettingsNode getIf(@NotNull BiPredicate<String, String> condition, @NotNull String... path) {
        SettingsNode node = this;
        boolean create = false;
        for (int i = 0; i < path.length; i++) {
            final String key = path[i];
            if (create) {
                if (i + 1 >= path.length) {
                    node = node.asMapNode().child(key);
                } else {
                    node = new MapNode(node.asMapNode(), key);
                }
            } else if (node.isMap()) {
                final SettingsNode child = node.asMapNode().getIfType(condition, key);
                if (child == null) {
                    create = true;
                    i--;
                } else {
                    node = child;
                }
            } else {
                return child();
            }
        }
        return node;
    }

    /**
     * Get a node by applying a custom key conversion comparison.
     *
     * @param keyConversion the key conversion.
     * @param condition     a predicate that evaluates the current key and the converted key from path.
     * @param path          the pre-converted key path.
     * @return              a node whose key path passes the condition.
     * @param <T>           the type conversion.
     */
    @NotNull
    protected <T> SettingsNode getIf(@NotNull Function<String, T> keyConversion, @NotNull BiPredicate<String, T> condition, @NotNull String... path) {
        SettingsNode node = this;
        for (String key : path) {
            if (node.isMap()) {
                final SettingsNode child = node.asMapNode().getIfType(condition, keyConversion.apply(key));
                if (child != null) {
                    node = child;
                    continue;
                }
            }
            return child();
        }
        return node;
    }

    /**
     * Get a node from the current map by applying a type condition along with node key.
     *
     * @param condition a predicate that evaluates the current key and the given type.
     * @param type      the object type.
     * @return          a node whose key passes the condition.
     * @param <T>       the type object.
     */
    @Nullable
    protected <T> SettingsNode getIfType(@NotNull BiPredicate<String, T> condition, @NotNull T type) {
        for (Entry<String, SettingsNode> entry : getValue().entrySet()) {
            if (condition.test(entry.getKey(), type)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public @NotNull MapNode getRoot() {
        return (MapNode) super.getRoot();
    }

    /**
     * Puts any type of value into current map by wrap it into a settings node.
     *
     * @param key   the node key.
     * @param value the node value.
     * @return      the previously associated node with key.
     */
    @Nullable
    public SettingsNode put(String key, Object value) {
        return getValue().put(key, child(key, value));
    }

    /**
     * Sets node into given key path inside root map and subsequent maps.
     *
     * @param node the node to set.
     * @param path the node path.
     */
    public void set(@NotNull SettingsNode node, @NotNull String... path) {
        MapNode mapNode = getRoot();
        final int size = path.length - 1;
        for (int i = 0; i < size; i++) {
            final String key = path[i];
            final SettingsNode child = mapNode.getValue().get(key);
            if (child == null) {
                mapNode = new MapNode(this, key);
                put(key, mapNode);
            } else if (child.isMap()) {
                mapNode = child.asMapNode();
            } else {
                mapNode = new MapNode(this, key);
                put(key, mapNode.mergeComment(child));
            }
        }
        final String key = path[path.length - 1];
        node.setKey(key);
        mapNode.put(key, node);
    }

    @Override
    public @NotNull SettingsNode setValue(@NotNull Object value) {
        if (value instanceof Map) {
            if (getFaceValue() == null) {
                super.setValue(new LinkedHashMap<>());
            } else {
                clear();
            }
            return merge((Map<?, ?>) value);
        }
        final SettingsNode node;
        if (value instanceof SettingsNode) {
            node = setValue(((SettingsNode) value).getValue());
        } else if (value instanceof List) {
            node = new ListNode(getParent(), getKey()).merge((SettingsNode) this).setValue(value);
        } else {
            node = new ObjectNode(getParent(), getKey()).merge(this).setValue(value);
        }
        node.setKey(getKey());
        return node;
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.
     *
     * @param map     the map to merge.
     * @return        this object itself.
     */
    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map) {
        return merge(map, false);
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.
     *
     * @param map     the map to merge.
     * @param replace true to replace any value that already exist.
     * @return        this object itself.
     */
    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map, boolean replace) {
        return merge(map, replace, false);
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.
     *
     * @param map     the map to merge.
     * @param replace true to replace any value that already exist.
     * @param deep    true to go inside subsequent maps to merge the subsequent maps inside given map.
     * @return        this object itself.
     */
    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map, boolean replace, boolean deep) {
        return merge(map, replace, deep, false);
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.
     *
     * @param map     the map to merge.
     * @param replace true to replace any value that already exist.
     * @param deep    true to go inside subsequent maps to merge the subsequent maps inside given map.
     * @param append  true to append any node inside provided map into current map instead of creating a new one.
     * @return        this object itself.
     */
    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map, boolean replace, boolean deep, boolean append) {
        SettingsNode tempNode;
        for (Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            if (!containsKey(entry.getKey())) {
                put(key, child(key, entry.getValue(), append));
            } else if (deep && entry.getValue() instanceof Map && (tempNode = get(entry.getKey())) instanceof MapNode) {
                tempNode.asMapNode().merge((Map<?, ?>) entry.getValue(), replace, true);
            } else if (replace) {
                final SettingsNode child = child(key, entry.getValue(), append);
                final SettingsNode replaced = put(key, child);
                if (replaced != null) {
                    child.mergeComment(replaced);
                }
            }
        }
        return this;
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.<br>
     * This method go inside subsequent maps to merge the
     * subsequent maps inside given map.
     *
     * @param map     the map to merge.
     * @return        this object itself.
     */
    @NotNull
    public MapNode deepMerge(@NotNull Map<?, ?> map) {
        return deepMerge(map, false);
    }

    /**
     * Merge any type of map into map node by creating the
     * required settings nodes or overriding the parent node.<br>
     * This method go inside subsequent maps to merge the
     * subsequent maps inside given map.
     *
     * @param map     the map to merge.
     * @param replace true to replace any value that already exist.
     * @return        this object itself.
     */
    @NotNull
    public MapNode deepMerge(@NotNull Map<?, ?> map, boolean replace) {
        return merge(map, replace, true);
    }

    @Override
    public @NotNull SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        final Map<String, SettingsNode> map = getValue();
        for (String key : new HashSet<>(map.keySet())) {
            final SettingsNode node = map.get(key);
            if (node != null) {
                node.edit(function);
            }
        }
        return this;
    }

    /**
     * Executed method when any node is deleted by the value itself.
     *
     * @param node the deleted node.
     */
    protected void remove(@NotNull SettingsNode node) {
        // empty default method
    }

    /**
     * Remove node by given key.
     *
     * @param key  the node key.
     * @param deep true to delete any empty parent path.
     * @return     the previous node associated with key.
     */
    public SettingsNode remove(Object key, boolean deep) {
        final SettingsNode child = remove(key);
        final MapNode parent;
        if (deep && isEmpty() && (parent = getParent()) != null) {
            parent.remove(getKey(), true);
        }
        return child;
    }

    /**
     * Remove node by given condition.
     *
     * @param predicate the predicate to compare nodes.
     * @return          true if any node was removed.
     */
    public boolean removeIf(@NotNull Predicate<SettingsNode> predicate) {
        return removeIf(predicate, false);
    }

    /**
     * Remove node by given condition.
     *
     * @param predicate the predicate to compare nodes.
     * @param deep      true to delete any empty parent path.
     * @return          true if any node was removed.
     */
    public boolean removeIf(@NotNull Predicate<SettingsNode> predicate, boolean deep) {
        final boolean result = getValue().entrySet().removeIf((entry) -> {
            if (predicate.test(entry.getValue())) {
                remove(entry.getValue());
                return true;
            }
            return false;
        });
        final MapNode parent;
        if (deep && isEmpty() && (parent = getParent()) != null) {
            parent.remove(getKey(), true);
        }
        return result;
    }

    /**
     * Create an empty child node without any associated key.
     *
     * @return a newly created node.
     */
    @NotNull
    protected SettingsNode child() {
        return new ObjectNode(this, null);
    }

    /**
     * Create a child node with the given key.<br>
     * This method put the node into current map.
     *
     * @param key the node key.
     * @return    a newly created node.
     */
    @NotNull
    protected SettingsNode child(@NotNull String key) {
        final SettingsNode node = new ObjectNode(this, key, null);
        put(key, node);
        return node;
    }

    /**
     * Create a child node with the given key and value.<br>
     * This method put the node into current map.
     *
     * @param key   the node key.
     * @param value the node value.
     * @return      a newly created node or the same value if it's a settings node.
     */
    @NotNull
    protected SettingsNode child(@NotNull String key, @Nullable Object value) {
        return child(key, value, false);
    }

    /**
     * Create or append a child node with the given key and value.<br>
     * This method put the node into current map.
     *
     * @param key    the node key.
     * @param value  the node value.
     * @param append true to append any settings node value into map.
     * @return       a newly created node or the same value.
     */
    @NotNull
    protected SettingsNode child(@NotNull String key, @Nullable Object value, boolean append) {
        if (append && value instanceof SettingsNode) {
            return ((SettingsNode) value).setParent(this);
        }
        final SettingsNode node = NodeKey.of(this, key, value);
        put(key, node);
        return node;
    }

    /**
     * Get a linked set with all the node key paths from map values.
     *
     * @return a linked set with string arrays.
     */
    @NotNull
    public Set<String[]> paths() {
        final Set<String[]> set = new LinkedHashSet<>();
        for (Entry<String, SettingsNode> entry : getValue().entrySet()) {
            if (entry.getValue().isMap()) {
                for (String[] path : entry.getValue().asMapNode().paths()) {
                    final String[] deepPath = new String[path.length + 1];
                    deepPath[0] = entry.getKey();
                    System.arraycopy(path, 0, deepPath, 1, path.length);
                    set.add(deepPath);
                }
            } else {
                set.add(new String[] { entry.getKey() });
            }
        }
        return set;
    }

    @Override
    public @NotNull Map<String, Object> asLiteralObject() {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, SettingsNode> entry : getValue().entrySet()) {
            map.put(entry.getKey(), entry.getValue().asLiteralObject());
        }
        return map;
    }

    /**
     * Get the current map node as Json formatted text.
     *
     * @return a json string.
     */
    @NotNull
    public String asJson() {
        return asJson(this);
    }

    /**
     * Get the provided object as it's json representation.
     *
     * @param object the object to represent.
     * @return       a json string.
     */
    @NotNull
    protected String asJson(@Nullable Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof ValueType) {
            return asJson(((ValueType<?>) object).getValue());
        } else if (object instanceof Iterable || object.getClass().isArray()) {
            final StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (Object o : object instanceof Iterable ? (Iterable<?>) object : IterableType.of(object)) {
                joiner.add(asJson(o));
            }
            return joiner.toString();
        } else if (object instanceof Map) {
            if (((Map<?, ?>) object).isEmpty()) {
                return "{}";
            }
            final StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                joiner.add("\"" + entry.getKey() + "\": " + asJson(entry.getValue()));
            }
            return joiner.toString();
        } else if (object instanceof Boolean || object instanceof Number) {
            return String.valueOf(object);
        } else {
            return '"' + String.valueOf(object) + '"';
        }
    }

    // Default map implementation

    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getValue().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getValue().containsValue(value);
    }

    @Override
    public SettingsNode get(Object key) {
        return getValue().get(key);
    }

    @Nullable
    @Override
    public SettingsNode put(String key, SettingsNode value) {
        value.setParent(this);
        return getValue().put(key, value);
    }

    @Override
    public SettingsNode remove(Object key) {
        return getValue().remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends SettingsNode> m) {
        getValue().putAll(m);
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return getValue().keySet();
    }

    @NotNull
    @Override
    public Collection<SettingsNode> values() {
        return getValue().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, SettingsNode>> entrySet() {
        return getValue().entrySet();
    }

    // Default iterator implementation

    @NotNull
    @Override
    public Iterator<Entry<String, SettingsNode>> iterator() {
        return getValue().entrySet().iterator();
    }
}
