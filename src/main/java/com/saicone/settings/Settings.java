package com.saicone.settings;

import com.saicone.settings.memory.MapMemory;
import com.saicone.settings.node.MapNode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Settings extends MapNode {

    private SettingsMemory memory;

    public Settings() {
        this(new LinkedHashMap<>());
    }

    public Settings(@NotNull Map<String, SettingsNode> nodes) {
        super(null, null, nodes);
    }

    public Settings(@Nullable SettingsMemory memory) {
        this(new LinkedHashMap<>(), memory);
    }

    public Settings(@NotNull Map<String, SettingsNode> nodes, @Nullable SettingsMemory memory) {
        super(null, null, nodes);
        this.memory = memory;
    }

    public boolean isMemorizing() {
        return this.memory != null;
    }

    @Nullable
    public SettingsMemory getMemory() {
        return memory;
    }

    @Override
    public @NotNull SettingsNode get(@NotNull String key) {
        if (isMemorizing()) {
            return save(key, () -> super.get(key));
        }
        return super.get(key);
    }

    @Override
    public @NotNull SettingsNode get(@NotNull String... path) {
        if (isMemorizing()) {
            return save(String.join(".", path), () -> super.get(path));
        }
        return super.get(path);
    }

    @Override
    public @NotNull SettingsNode getIgnoreCase(@NotNull String key) {
        if (isMemorizing()) {
            return save(key, () -> super.getIgnoreCase(key));
        }
        return super.getIgnoreCase(key);
    }

    @Override
    public @NotNull SettingsNode getIgnoreCase(@NotNull String... path) {
        if (isMemorizing()) {
            return save(String.join(".", path), () -> super.getIgnoreCase(path));
        }
        return super.getIgnoreCase(path);
    }

    @Override
    public @NotNull SettingsNode getRegex(@NotNull @Language(value = "RegExp") String regex) {
        if (isMemorizing()) {
            return save("$RegExp." + regex, () -> super.getRegex(regex));
        }
        return super.getRegex(regex);
    }

    @Override
    public @NotNull SettingsNode getRegex(@NotNull @Language(value = "RegExp") String... regexPath) {
        if (isMemorizing()) {
            save("$RegExp." + String.join(".", regexPath), () -> super.getRegex(regexPath));
        }
        return super.getRegex(regexPath);
    }

    @NotNull
    protected SettingsNode save(@NotNull String id, @NotNull Supplier<@NotNull SettingsNode> supplier) {
        SettingsNode node = memory.get(id);
        if (node != null) {
            return node;
        }
        node = supplier.get();
        memory.save(id, node);
        return node;
    }

    @NotNull
    @Contract("_ -> this")
    public Settings setMemory(@Nullable SettingsMemory memory) {
        this.memory = memory;
        return this;
    }

    @NotNull
    @Contract("-> this")
    public Settings setMapMemory() {
        return setMemory(new MapMemory());
    }

    @Override
    protected void remove(@NotNull SettingsNode node) {
        if (isMemorizing()) {
            memory.remove(node);
        }
    }

    @Override
    public SettingsNode remove(Object key) {
        if (key instanceof String && this.memory != null) {
            this.memory.remove((String) key);
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        super.clear();
        if (isMemorizing()) {
            memory.clear();
        }
    }
}