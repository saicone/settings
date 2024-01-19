package com.saicone.settings.source;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

public class GsonSettingsSource implements SettingsSource {

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    private final Gson gson;

    public GsonSettingsSource() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

    public GsonSettingsSource(@NotNull Gson gson) {
        this.gson = gson;
    }

    @NotNull
    public Gson getGson() {
        return gson;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        parent.merge((Map<?, ?>) gson.fromJson(reader, MAP_TYPE));
        return parent;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        gson.toJson(parent.asLiteralObject(), writer);
    }
}
