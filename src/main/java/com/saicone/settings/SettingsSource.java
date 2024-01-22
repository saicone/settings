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

/**
 * Interface that represent a object that can parse and write a map node using a reader or writer
 * represented with a data source type, such as file formats.
 *
 * @author Rubenicos
 */
public interface SettingsSource {

    /**
     * Use the provided reader to add every node value into parent map node.
     *
     * @param reader the reader with any allowed data.
     * @param parent the map node to add values.
     * @return       the effective map node used in this operation, normally the provided one.
     * @param <T>    the map node type.
     * @throws IOException if any error occurs while reading the data.
     */
    default <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        throw new IllegalStateException("Cannot read settings with " + getClass().getName());
    }

    /**
     * Read the provided comment lines and convert into a user-friendly one.
     *
     * @param comment the comment to read.
     * @return        the provided non-null comment converted, null otherwise.
     */
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

    /**
     * Write the provided map node into writer.
     *
     * @param writer the writer to add any node value data.
     * @param parent the map node to get values.
     * @throws IOException if any error occurs while writing the data.
     */
    default void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        throw new IllegalStateException("Cannot write settings with " + getClass().getName());
    }

    /**
     * Read the provided user-friendly comment and convert into a writeable one.
     *
     * @param comment the comment to write.
     * @return        the provided non-null comment converted, null otherwise.
     */
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
