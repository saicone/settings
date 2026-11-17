package com.saicone.settings;

import com.saicone.settings.node.MapNode;
import com.saicone.settings.parser.ExpressionParser;
import com.saicone.settings.parser.Expressions;
import com.saicone.settings.parser.NodeParser;
import com.saicone.settings.parser.Parsers;
import com.saicone.settings.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to handle node parser operations.
 *
 * @author Rubenicos
 */
public class SettingsParser {

    private static final Pattern EXPRESSION_VARIABLE = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern SUB_VARIABLE = Pattern.compile("\\$\\[([^}]+)]");

    private static final SettingsParser EMPTY;
    private static final SettingsParser SIMPLE;
    private static final SettingsParser ALL;

    static {
        EMPTY = new SettingsParser();
        EMPTY.immutable = true;

        final Map<String, ExpressionParser> expressions = new HashMap<>();
        expressions.put("node", Expressions.NODE);
        SIMPLE = new SettingsParser(null, expressions);
        SIMPLE.immutable = true;

        ALL = new SettingsParser(Parsers.all(), Expressions.all());
        ALL.immutable = true;
    }

    private List<NodeParser> parsers;
    private Map<String, ExpressionParser> expressions;

    private transient boolean immutable;

    /**
     * Get an empty settings parser that doesn't do anything with given nodes.
     *
     * @return a settings parser.
     */
    @NotNull
    public static SettingsParser empty() {
        return EMPTY;
    }

    /**
     * Get a simple settings parser that only parse {@link Expressions#NODE} replacements.
     *
     * @return a settings parser.
     */
    @NotNull
    public static SettingsParser simple() {
        return SIMPLE;
    }

    /**
     * Get a settings parser with any expression from {@link Expressions} and parser from {@link Parsers}.
     *
     * @return a settings parser.
     */
    @NotNull
    public static SettingsParser all() {
        return ALL;
    }

    /**
     * Constructs an empty settings parser.
     */
    public SettingsParser() {
        this(null, null);
    }

    /**
     * Constructs a settings parser with the given parameters.
     *
     * @param parsers     the node parsers to be applied.
     * @param expressions the expressions to be applied.
     */
    public SettingsParser(@Nullable List<NodeParser> parsers, @Nullable Map<String, ExpressionParser> expressions) {
        this.parsers = parsers;
        this.expressions = expressions;
    }

    /**
     * Get the current node parsers list.
     *
     * @return a list of node parsers.
     */
    @Nullable
    public List<NodeParser> getParsers() {
        return parsers;
    }

    /**
     * Get the current expression parsers.
     *
     * @return a map of expressions.
     */
    @Nullable
    public Map<String, ExpressionParser> getExpressions() {
        return expressions;
    }

    /**
     * Add a node parser into current instance.
     *
     * @param parser the parser to add.
     * @return       this settings parser.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsParser addParser(@NotNull NodeParser parser) {
        if (immutable) {
            throw new IllegalStateException("Cannot edit immutable settings loader");
        }
        if (this.parsers == null) {
            this.parsers = new ArrayList<>();
        }
        this.parsers.add(parser);
        return this;
    }

    /**
     * Add a expression parser into current instance.
     *
     * @param id         the expression id.
     * @param expression the expression to add.
     * @return           this settings parser.
     */
    @NotNull
    @Contract("_, _ -> this")
    public SettingsParser addExpression(@NotNull String id, @NotNull ExpressionParser expression) {
        if (immutable) {
            throw new IllegalStateException("Cannot edit immutable settings loader");
        }
        if (this.expressions == null) {
            this.expressions = new HashMap<>();
        }
        this.expressions.put(id, expression);
        return this;
    }

    /**
     * Parse provided node.
     *
     * @param node the node to parse.
     * @return     the effective node used in this operation, normally the provided one.
     */
    @Nullable
    @Contract("!null -> !null")
    public SettingsNode parse(@Nullable SettingsNode node) {
        if (node == null) {
            return null;
        }
        final SettingsNode root = node.getRoot();
        if (root.isMap()) {
            return parse(root.asMapNode(), node);
        }
        return node;
    }

    /**
     * Parse the provided node with used root node.
     *
     * @param root the root node where node belongs from.
     * @param node the node to parse.
     * @return     the effective node used in this operation, normally the provided one.
     */
    @Nullable
    @Contract("_, !null -> !null")
    public SettingsNode parse(@NotNull MapNode root, @Nullable SettingsNode node) {
        if (node == null) {
            return null;
        }
        SettingsNode finalNode = node;
        if (parsers != null) {
            for (NodeParser parser : parsers) {
                finalNode = parser.parse(root, finalNode);
            }
        }
        if (expressions != null) {
            finalNode = finalNode.parse(s -> s.contains("${"), (provider, s) -> {
                final Matcher matcher = EXPRESSION_VARIABLE.matcher(s);
                boolean first = true;
                while (matcher.find()) {
                    if (first) {
                        first = false;
                        if (s.equals(matcher.group())) {
                            return parse(root, provider, matcher.group(1));
                        }
                    }
                    s = matcher.replaceFirst(String.valueOf(parse(root, provider, matcher.group(1))));
                }
                return s;
            });
        }
        return finalNode;
    }

    /**
     * Build a parsed value with provided parameters.
     *
     * @param root     the root node where node belongs from.
     * @param provider the node that provide the given string.
     * @param s        the string that represent a expression object.
     * @return         a value from expression.
     */
    @Nullable
    public Object parse(@NotNull MapNode root, @NotNull SettingsNode provider, @NotNull String s) {
        final int index = s.indexOf(':');
        final String id;
        if (index < 1) {
            id = "node";
        } else {
            id = s.substring(0, index);
        }

        final ExpressionParser expression = expressions.get(id);
        if (expression == null) {
            return "${" + s + '}';
        }

        final String content;
        if (index < 1) {
            content = s;
        } else {
            content = index + 1 < s.length() ? s.substring(index + 1) : "";
        }
        final String[] split = Strings.split(content, '_');
        final Object[] args = new Object[split.length];
        for (int i = 0; i < split.length; i++) {
            String arg = split[i];
            if (arg.contains("$[")) {
                final Matcher matcher = SUB_VARIABLE.matcher(arg);
                while (matcher.find()) {
                    arg = matcher.replaceFirst(String.valueOf(parse(root, provider, matcher.group(1))));
                }
            }
            args[i] = arg;
        }
        return expression.parse(root, provider, args);
    }

    /**
     * Set a parsed value to path inside map node.
     *
     * @param node  the map node to insert tha value.
     * @param value the value to parse and insert.
     * @param path  the node key path.
     * @return      the effective node used in this operation, normally the provided map node.
     * @param <T>   the map node type.
     */
    @Nullable
    @Contract("!null, _, _ -> !null")
    public <T extends MapNode> T set(@Nullable T node, @NotNull Object value, @NotNull String... path) {
        if (node == null) {
            return null;
        }
        parse(node, node.get(path).setValue(value));
        return node;
    }
}
