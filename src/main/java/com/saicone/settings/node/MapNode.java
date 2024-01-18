package com.saicone.settings.node;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.type.IterableType;
import com.saicone.settings.type.ValueType;
import com.saicone.settings.util.Strings;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MapNode extends NodeKey<Map<String, SettingsNode>> implements Map<String, SettingsNode>, Iterable<Map.Entry<String, SettingsNode>> {

    public MapNode() {
        this(new LinkedHashMap<>());
    }

    public MapNode(@Nullable Map<String, SettingsNode> value) {
        this(null, null, value);
    }

    public MapNode(@Nullable MapNode parent, @Nullable String key) {
        this(parent, key, new LinkedHashMap<>());
    }

    public MapNode(@Nullable MapNode parent, @Nullable String key, @Nullable Map<String, SettingsNode> value) {
        super(parent, key, value);
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @NotNull
    public SettingsNode get(@NotNull String key) {
        return getIf(s -> s.equals(key), key);
    }

    @NotNull
    public SettingsNode get(@NotNull String... path) {
        if (path.length == 1) {
            return get(path[0]);
        }
        return getIf(String::equals, path);
    }

    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String key) {
        return getIf(s -> s.equalsIgnoreCase(key), key);
    }

    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String... path) {
        if (path.length == 1) {
            return getIgnoreCase(path[0]);
        }
        return getIf(String::equalsIgnoreCase, path);
    }

    @NotNull
    public SettingsNode getRegex(@NotNull @Language("RegExp") String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return getIf(s -> pattern.matcher(s).matches(), null);
    }

    @NotNull
    public SettingsNode getRegex(@NotNull @Language("RegExp") String... regexPath) {
        if (regexPath.length == 1) {
            return getRegex(regexPath[0]);
        }
        return getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath);
    }

    @NotNull
    public SettingsNode getSplit(@NotNull Object path) {
        return get(Strings.split(String.valueOf(path), '.'));
    }

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
        if (value instanceof List) {
            node = new ListNode(getParent(), getKey()).merge((SettingsNode) this).setValue(value);
        } else {
            node = new ObjectNode(getParent(), getKey()).merge(this).setValue(value);
        }
        node.setKey(getKey());
        return node;
    }

    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map) {
        return merge(map, false);
    }

    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map, boolean replace) {
        return merge(map, replace, false);
    }

    @NotNull
    public MapNode merge(@NotNull Map<?, ?> map, boolean replace, boolean deep) {
        SettingsNode tempNode;
        for (Entry<?, ?> entry : map.entrySet()) {
            final String key = String.valueOf(entry.getKey());
            if (!containsKey(entry.getKey())) {
                put(key, child(key, entry.getValue()));
            } else if (deep && entry.getValue() instanceof Map && (tempNode = get(entry.getKey())) instanceof MapNode) {
                tempNode.asMapNode().merge((Map<?, ?>) entry.getValue(), replace, true);
            } else if (replace) {
                final SettingsNode child = child(key, entry.getValue());
                final SettingsNode replaced = put(key, child);
                if (replaced != null) {
                    child.mergeComment(replaced);
                }
            }
        }
        return this;
    }

    @NotNull
    public MapNode deepMerge(@NotNull Map<?, ?> map) {
        return deepMerge(map, false);
    }

    @NotNull
    public MapNode deepMerge(@NotNull Map<?, ?> map, boolean replace) {
        return merge(map, replace, true);
    }

    @Override
    public @NotNull SettingsNode edit(@NotNull Function<SettingsNode, SettingsNode> function) {
        for (Map.Entry<String, SettingsNode> entry : getValue().entrySet()) {
            entry.getValue().edit(function);
        }
        return this;
    }

    protected void remove(@NotNull SettingsNode node) {
        // empty default method
    }

    public SettingsNode remove(Object key, boolean deep) {
        final SettingsNode child = remove(key);
        final MapNode parent;
        if (deep && isEmpty() && (parent = getParent()) != null) {
            parent.remove(getKey(), true);
        }
        return child;
    }

    public boolean removeIf(@NotNull Predicate<SettingsNode> predicate) {
        return removeIf(predicate, false);
    }

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

    @NotNull
    protected SettingsNode child() {
        return new ObjectNode(this, null);
    }

    @NotNull
    protected SettingsNode child(@NotNull String key) {
        final SettingsNode node = new ObjectNode(this, key, null);
        put(key, node);
        return node;
    }

    @NotNull
    protected SettingsNode child(@NotNull String key, @Nullable Object value) {
        if (value instanceof SettingsNode) {
            return ((SettingsNode) value).setParent(this);
        }
        final SettingsNode node = NodeKey.of(this, key, value);
        put(key, node);
        return node;
    }

    @NotNull
    public Set<String[]> paths() {
        final Set<String[]> set = new HashSet<>();
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

    @NotNull
    public Map<String, Object> asObjectMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, SettingsNode> entry : getValue().entrySet()) {
            if (entry.getValue().isMap()) {
                map.put(entry.getKey(), entry.getValue().asMapNode().asObjectMap());
            } else {
                map.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return map;
    }

    @NotNull
    public String asJson() {
        return asJson(this);
    }

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
