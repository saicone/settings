package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.parser.impl.MathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Expressions {

    public static final ExpressionParser NODE = (root, provider, args) -> root.getSplit(args[0]);
    public static final ExpressionParser SIZE = (root, provider, args) -> {
        final SettingsNode node = (SettingsNode) args[0];
        return node.isMap() ? node.asMapNode().size() : node.isList() ? node.asListNode().size() : node.getValue() == null ? -1 : 1;
    };
    public static final ExpressionParser JOIN = (root, provider, args) -> {
        final SettingsNode node = root.getSplit(args[0]);
        final String delimiter = String.valueOf(args[1]);
        final int start = args.length > 2 ? Integer.parseInt(String.valueOf(args[2])) : 0;
        final int end = args.length > 3 ? Integer.parseInt(String.valueOf(args[3])) : -1;
        final List<String> list = node.asStringList();
        if (!list.isEmpty() && (start > 0 || end > 0)) {
            return String.join(delimiter, list.subList(start, end > 0 ? end : list.size()));
        } else {
            return String.join(delimiter, list);
        }
    };
    public static final ExpressionParser SPLIT = (root, provider, args) -> root.getSplit(args[0]).asString("").split(String.valueOf(args[1]));
    public static final ExpressionParser MATH;

    static {
        ExpressionParser math;
        try {
            Class.forName("com.ezylang.evalex.Expression");
            math = new MathExpression();
        } catch (ClassNotFoundException e) {
            math = (root, provider, args) -> {
                throw new RuntimeException("The current classpath doesn't contains EvalEx library");
            };
        }
        MATH = math;
    }

    Expressions() {
    }

    @NotNull
    public static Map<String, ExpressionParser> all() {
        final Map<String, ExpressionParser> map = new HashMap<>();
        map.put("node", NODE);
        map.put("size", SIZE);
        map.put("join", JOIN);
        map.put("split", SPLIT);
        map.put("math", MATH);
        return map;
    }
}
