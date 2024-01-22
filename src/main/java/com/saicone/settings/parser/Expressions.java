package com.saicone.settings.parser;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.NodeValue;
import com.saicone.settings.parser.impl.MathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Utility class to collect node expressions that can be parsed.
 *
 * @author Rubenicos
 */
public class Expressions {

    /**
     * Expression to get a node with value replaced arguments.
     */
    public static final ExpressionParser NODE = (root, provider, args) -> {
        final SettingsNode node = root.getSplit(args[0]);
        if (args.length == 1) {
            return node;
        }
        return NodeValue.of(node.getValue()).replaceArgs(Arrays.copyOfRange(args, 1, args.length));
    };
    /**
     * Expression that return the current or given node size.
     */
    public static final ExpressionParser SIZE = (root, provider, args) -> {
        final SettingsNode node = root.getSplit(args[0]);
        return node.isMap() ? node.asMapNode().size() : node.isList() ? node.asListNode().size() : node.getValue() == null ? -1 : 1;
    };
    /**
     * Expression to join a node values from key path.
     */
    public static final ExpressionParser JOIN = (root, provider, args) -> {
        final SettingsNode node = root.getSplit(args[0]);
        if (node.isList()) {
            final String delimiter = String.valueOf(args[1]);
            final int start = args.length > 2 ? Integer.parseInt(String.valueOf(args[2])) : 0;
            final int end = args.length > 3 ? Integer.parseInt(String.valueOf(args[3])) : -1;

            final List<SettingsNode> list = node.asListNode().getValue();
            if (start >= list.size()) {
                return "";
            }

            final StringJoiner joiner = new StringJoiner(delimiter);
            int size = end > 0 && end < list.size() ? end : list.size();
            for (int i = start; i < size; i++) {
                joiner.add(list.get(i).toString());
            }
            return joiner.toString();
        } else {
            return node.toString();
        }
    };
    /**
     * Expression to split a node value from path.
     */
    public static final ExpressionParser SPLIT = (root, provider, args) -> root.getSplit(args[0]).asString("").split(String.valueOf(args[1]));
    /**
     * Expression to calculate a mathematical operation.<br>
     * EvalEx library must be in the current classpath.
     */
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

    /**
     * Constructs a map with all expression parsers in this class.
     *
     * @return a map with expression parsers.
     */
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
