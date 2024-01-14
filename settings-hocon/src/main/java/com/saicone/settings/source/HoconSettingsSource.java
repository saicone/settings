package com.saicone.settings.source;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.node.NodeKey;
import com.typesafe.config.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class HoconSettingsSource extends SettingsSource {

    private final ConfigParseOptions parseOptions;
    private final ConfigRenderOptions renderOptions;
    private final ConfigResolveOptions resolveOptions;

    public HoconSettingsSource() {
        this(ConfigParseOptions.defaults(), ConfigRenderOptions.defaults(), ConfigResolveOptions.defaults());
    }

    public HoconSettingsSource(@NotNull ConfigParseOptions parseOptions, @NotNull ConfigRenderOptions renderOptions) {
        this(parseOptions, renderOptions, null);
    }

    public HoconSettingsSource(@NotNull ConfigParseOptions parseOptions, @NotNull ConfigRenderOptions renderOptions, @Nullable ConfigResolveOptions resolveOptions) {
        this.parseOptions = parseOptions;
        this.renderOptions = renderOptions;
        this.resolveOptions = resolveOptions;
    }

    @NotNull
    public ConfigParseOptions getParseOptions() {
        return parseOptions;
    }

    @NotNull
    public ConfigRenderOptions getRenderOptions() {
        return renderOptions;
    }

    @Nullable
    public ConfigResolveOptions getResolveOptions() {
        return resolveOptions;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final Config config = ConfigFactory.parseReader(reader, parseOptions);
        return readConfig(parent, config.root(), resolveOptions == null ? null : config.resolve(resolveOptions).root());
    }

    @NotNull
    public SettingsNode readValue(@Nullable MapNode parent, @Nullable String key, @NotNull ConfigValue value, @Nullable ConfigValue resolved) {
        if (value.valueType() == ConfigValueType.OBJECT) {
            if (resolved != null) {
                if (resolved.valueType() == ConfigValueType.OBJECT) {
                    return readConfig(new MapNode(parent, key), (ConfigObject) value, (ConfigObject) resolved);
                } else {
                    final MapNode map = new MapNode(parent, key);
                    map.setValue(new LinkedHashMap<>());
                    readConfig(map, (ConfigObject) value, null);
                    map.merge(readValue(null, null, resolved, null));
                    return map;
                }
            } else {
                return readConfig(new MapNode(parent, key), (ConfigObject) value, null);
            }
        } else if (value.valueType() == ConfigValueType.LIST) {
            if (resolved != null) {
                if (resolved.valueType() == ConfigValueType.LIST) {
                    return readList(new ListNode(parent, key), (ConfigList) value, (ConfigList) resolved);
                } else {
                    final ListNode list = new ListNode(parent, key);
                    list.setValue(new ArrayList<>());
                    readList(list, (ConfigList) value, null);
                    list.merge(readValue(null, null, resolved, null));
                    return list;
                }
            } else {
                return readList(new ListNode(parent, key), (ConfigList) value, null);
            }
        } else {
            final SettingsNode node = NodeKey.of(parent, key, value.unwrapped());
            if (resolved != null) {
                node.setValue(node.getValue());
                node.merge(readValue(null, null, resolved, null));
            }
            return node;
        }
    }

    @NotNull
    public <T extends MapNode> T readConfig(@NotNull T parent, @NotNull ConfigObject config, @Nullable ConfigObject resolved) {
        if (config.isEmpty()) {
            return parent;
        }

        final boolean ignore = resolved == null;
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            final String key = entry.getKey();
            final SettingsNode node = readValue(parent, key, entry.getValue(), ignore ? null : resolved.get(key));

            final List<String> comments = entry.getValue().origin().comments();
            if (comments != null && !comments.isEmpty()) {
                node.setTopComment(comments);
            }

            parent.put(key, node);
        }

        return parent;
    }

    @NotNull
    public ListNode readList(@NotNull ListNode node, @NotNull ConfigList list, @Nullable ConfigList resolved) {
        final boolean ignore = resolved == null;

        for (int i = 0; i < list.size(); i++) {
            final ConfigValue value = list.get(i);
            final SettingsNode child = readValue(null, null, value, ignore ? null : resolved.get(i));

            final List<String> comments = value.origin().comments();
            if (comments != null && !comments.isEmpty()) {
                child.setTopComment(comments);
            }

            node.add(child);
        }

        return node;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        final ConfigValue value = writeValue(parent);
        writer.write(value.render(renderOptions));
    }

    @Nullable
    @Contract("!null -> !null")
    public ConfigValue writeValue(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof SettingsNode) {
            return writeValue(((SettingsNode) object).getSourceValue());
        }

        if (object instanceof Map) {
            final Map<String, ConfigValue> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final ConfigValue value = writeValue(entry.getValue());
                if (value != null) {
                    if (entry.getValue() instanceof SettingsNode) {
                        value.origin().withComments(((SettingsNode) entry.getValue()).getTopComment());
                    }
                    map.put(String.valueOf(entry.getKey()), value);
                }
            }
            return ConfigValueFactory.fromMap(map);
        } else if (object instanceof Iterable) {
            final List<ConfigValue> list = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                final ConfigValue value = writeValue(o);
                if (value != null) {
                    if (o instanceof SettingsNode) {
                        value.origin().withComments(((SettingsNode) o).getTopComment());
                    }
                    list.add(value);
                }
            }
            return ConfigValueFactory.fromIterable(list);
        } else {
            return ConfigValueFactory.fromAnyRef(object);
        }
    }
}
