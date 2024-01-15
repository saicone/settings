package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ExpressionParser {

    @Nullable
    Object parse(@NotNull MapNode root, @NotNull SettingsNode provider, @NotNull Object... args);

}
