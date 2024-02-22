package com.saicone.settings.source;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.saicone.settings.SettingsNode;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.node.NodeKey;
import com.saicone.types.ValueType;
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

/**
 * A settings source for toml-formatted data<br>
 * This class uses electronwill nightconfig library to read and write any data.
 *
 * @author Rubenicos
 */
public class TomlSettingsSource implements SettingsSource {

    private TomlFormat format;

    /**
     * Constructs a toml settings source with default options.<br>
     * This means any comment will be parsed.
     */
    public TomlSettingsSource() {
        this(TomlFormat.instance());
    }

    /**
     * Constructs a toml settings source with provided toml format loader.
     *
     * @param format the toml format to use.
     */
    public TomlSettingsSource(@NotNull TomlFormat format) {
        this.format = format;
    }

    /**
     * Get the current toml format loader instance.
     *
     * @return a toml format instance.
     */
    @NotNull
    public TomlFormat getFormat() {
        return format;
    }

    /**
     * Replace the current toml format loader instance.
     *
     * @param format the toml format to use.
     */
    public void setFormat(@NotNull TomlFormat format) {
        this.format = format;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final CommentedConfig config = format.createParser().parse(reader);
        return readConfig(parent, config);
    }

    /**
     * Read any value as settings node with provided parameters.
     *
     * @param parent the associated parent node that value belongs from.
     * @param key    the node key.
     * @param value  the value to read.
     * @return       a newly created settings node.
     */
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

    /**
     * Read yaml config object values and save into provided parent map node.
     *
     * @param parent the parent node to append values.
     * @param config the config object to read.
     * @return       the provided parent node.
     * @param <T>    the map node type.
     */
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

    /**
     * Write any object into literal compatible object with nightconfig library.
     *
     * @param object the object to be converted.
     * @return       a newly generated compatible object with nightconfig library, null otherwise.
     */
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

    /**
     * Write the provided object into config instance.
     *
     * @param object the object to write.
     * @return       a config that represent the provided object, null otherwise.
     */
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
