package com.saicone.settings.source;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.saicone.settings.SettingsNode;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.node.NodeKey;
import com.saicone.settings.type.ValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TomlSettingsSource implements SettingsSource {

    private TomlFormat format;

    public TomlSettingsSource() {
        this(TomlFormat.instance());
    }

    public TomlSettingsSource(@NotNull TomlFormat format) {
        this.format = format;
    }

    @NotNull
    public TomlFormat getFormat() {
        return format;
    }

    public void setFormat(@NotNull TomlFormat format) {
        this.format = format;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final CommentedConfig config = format.createParser().parse(reader);
        return readConfig(parent, config);
    }

    @NotNull
    public SettingsNode readValue(@Nullable MapNode parent, @Nullable String key, @NotNull Object value) {
        if (value instanceof Config) {
            return readConfig(new MapNode(parent, key), (Config) value);
        } else if (value instanceof Iterable) {
            final ListNode list = new ListNode(parent, key);
            for (Object o : (Iterable<?>) value) {
                list.add(readValue(null, null, o));
            }
            return list;
        } else {
            return NodeKey.of(parent, key, value);
        }
    }

    @Nullable
    @Contract("!null, _ -> !null")
    public <T extends MapNode> T readConfig(@Nullable T parent, @NotNull Config config) {
        if (config.valueMap().isEmpty()) {
            return parent;
        }
        for (Map.Entry<String, Object> entry : config.valueMap().entrySet()) {
            final SettingsNode node = readValue(parent, entry.getKey(), entry.getValue());
            if (config instanceof CommentedConfig) {
                final String comment = ((CommentedConfig) config).getComment(entry.getKey());
                if (comment != null) {
                    node.setTopComment(readComment(Arrays.asList(comment.split("\n"))));
                }
            }
            if (parent != null) {
                parent.put(entry.getKey(), node);
            }
        }
        return parent;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        format.createWriter().write(writeConfig(parent), writer);
    }

    @Nullable
    @Contract("!null -> !null")
    public Object writeValue(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof SettingsNode) {
            return writeValue(((SettingsNode) object).getSourceValue());
        } else if (object instanceof Map) {
            return writeConfig(object);
        } else if (object instanceof Iterable) {
            final List<Object> list = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                list.add(writeValue(o));
            }
            return list;
        } else {
            return object instanceof ValueType ? ((ValueType<?>) object).getValue() : object;
        }
    }

    @Nullable
    @Contract("!null -> !null")
    public Config writeConfig(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof SettingsNode) {
            return writeConfig(((SettingsNode) object).getSourceValue());
        }

        final CommentedConfig config = CommentedConfig.inMemory();
        if (object instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                final Object value = entry.getValue();
                config.set(key, writeValue(value));
                if (entry.getValue() instanceof SettingsNode) {
                    final List<String> topComment = writeComment(((SettingsNode) value).getTopComment());
                    if (topComment != null) {
                        config.setComment(key, String.join("\n", topComment));
                    }
                }
            }
        }
        return config;
    }
}
