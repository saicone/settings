package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a function that parse any type of node and return a node.
 *
 * @author Rubenicos
 */
@FunctionalInterface
public interface NodeParser {

    /**
     * Parse the given parameters into a settings node.
     *
     * @param root the root node where provider belongs from.
     * @param node the node to parse.
     * @return     a node value, normally the given node.
     */
    @NotNull
    SettingsNode parse(@NotNull MapNode root, @NotNull SettingsNode node);

}
