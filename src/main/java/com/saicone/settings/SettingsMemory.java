package com.saicone.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that represents a memory to save settings node values.
 *
 * @author Rubenicos
 */
public interface SettingsMemory {

    /**
     * Get the previously saved node by given path id.
     *
     * @param id the node path id.
     * @return   a previously saved node, null otherwise.
     */
    @Nullable
    SettingsNode get(@NotNull String id);

    /**
     * Save a node with the given id.
     *
     * @param id   the node path id.
     * @param node the node to save.
     */
    void save(@NotNull String id, @NotNull SettingsNode node);

    /**
     * Remove a node by path id.
     *
     * @param id the node path id.
     */
    void remove(@NotNull String id);

    /**
     * Remove a node by value itself.
     *
     * @param node the node to remove.
     */
    void remove(@NotNull SettingsNode node);

    /**
     * Clear the current settings memory.
     */
    void clear();

}