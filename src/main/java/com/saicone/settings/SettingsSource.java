package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public abstract class SettingsSource {

    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        throw new IllegalStateException("Cannot read settings with " + getClass().getName());
    }

    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        throw new IllegalStateException("Cannot write settings with " + getClass().getName());
    }
}
