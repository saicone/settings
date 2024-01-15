package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NodeParser {

    @NotNull
    SettingsNode parse(@NotNull MapNode root, @NotNull SettingsNode node);

}
