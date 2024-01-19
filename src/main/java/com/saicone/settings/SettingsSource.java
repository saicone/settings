package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public interface SettingsSource {

    default <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        throw new IllegalStateException("Cannot read settings with " + getClass().getName());
    }

    @Nullable
    @Contract("!null -> !null")
    default List<String> readComment(@Nullable List<String> comment) {
        if (comment == null) {
            return null;
        }
        if (comment.isEmpty()) {
            return comment;
        }
        final List<String> list = new ArrayList<>();
        for (String line : comment) {
            line = line.replace("\r", "");
            if (!line.isEmpty() && line.charAt(0) == ' ') {
                list.add(line.substring(1));
            } else {
                list.add(line);
            }
        }
        return list;
    }

    default void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        throw new IllegalStateException("Cannot write settings with " + getClass().getName());
    }

    @Nullable
    @Contract("!null -> !null")
    default List<String> writeComment(@Nullable List<String> comment) {
        if (comment == null) {
            return null;
        }
        if (comment.isEmpty()) {
            return comment;
        }
        final List<String> list = new ArrayList<>();
        for (String line : comment) {
            if (line.trim().isEmpty()) {
                list.add(line);
            } else {
                list.add(' ' + line);
            }
        }
        return list;
    }
}
