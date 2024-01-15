package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Parsers {

    public static final NodeParser EXTENDS = (root, node) -> {
        if (!node.isMap()) {
            return node;
        }
        final MapNode map = node.asMapNode();
        if (!map.containsKey("extends")) {
            return node;
        }
        final Map<String, SettingsNode> args = map.getValue();
        return map.setValue(root.getSplit(map.get("extends").asString(""))).replaceArgs(args);
    };

    Parsers() {
    }

    @NotNull
    public static List<NodeParser> all() {
        final List<NodeParser> list = new ArrayList<>();
        list.add(EXTENDS);
        return list;
    }
}
