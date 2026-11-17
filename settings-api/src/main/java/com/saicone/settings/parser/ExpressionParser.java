package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a function that parse any type of node and build a value.
 *
 * @author Rubenicos
 */
@FunctionalInterface
public interface ExpressionParser {

    /**
     * Parse the given parameters into any type of object.
     *
     * @param root     the root node where provider belongs from.
     * @param provider the provider that offers the arguments value.
     * @param args     the arguments from provider.
     * @return         a parsed object using the given parameters.
     */
    @Nullable
    Object parse(@NotNull MapNode root, @NotNull SettingsNode provider, @NotNull Object... args);

}
