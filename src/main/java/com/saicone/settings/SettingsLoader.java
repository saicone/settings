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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsLoader {

    private static final Pattern EXPRESSION_VARIABLE = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern SUB_VARIABLE = Pattern.compile("\\$\\[([^}]+)]");

    private static final SettingsLoader EMPTY;
    private static final SettingsLoader SIMPLE;
    private static final SettingsLoader ALL;

    static {
        EMPTY = new SettingsLoader();
        EMPTY.immutable = true;

        final Map<String, ExpressionParser> expressions = new HashMap<>();
        expressions.put("node", Expressions.NODE);
        SIMPLE = new SettingsLoader(null, expressions);
        SIMPLE.immutable = true;

        ALL = new SettingsLoader(Parsers.all(), Expressions.all());
        ALL.immutable = true;
    }

    private List<NodeParser> parsers;
    private Map<String, ExpressionParser> expressions;

    private transient boolean immutable;

    @NotNull
    public static SettingsLoader empty() {
        return EMPTY;
    }

    @NotNull
    public static SettingsLoader simple() {
        return SIMPLE;
    }

    @NotNull
    public static SettingsLoader all() {
        return ALL;
    }

    public SettingsLoader() {
        this(null, null);
    }

    public SettingsLoader(@Nullable List<NodeParser> parsers, @Nullable Map<String, ExpressionParser> expressions) {
        this.parsers = parsers;
        this.expressions = expressions;
    }

    @Nullable
    public List<NodeParser> getParsers() {
        return parsers;
    }

    @Nullable
    public Map<String, ExpressionParser> getExpressions() {
        return expressions;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsLoader addParser(@NotNull NodeParser parser) {
        if (immutable) {
            throw new IllegalStateException("Cannot edit immutable settings loader");
        }
        if (this.parsers == null) {
            this.parsers = new ArrayList<>();
        }
        this.parsers.add(parser);
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public SettingsLoader addExpression(@NotNull String id, @NotNull ExpressionParser expression) {
        if (immutable) {
            throw new IllegalStateException("Cannot edit immutable settings loader");
        }
        if (this.expressions == null) {
            this.expressions = new HashMap<>();
        }
        this.expressions.put(id, expression);
        return this;
    }

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

    @NotNull
    public <T extends MapNode> T load(@NotNull SettingsSource source, @NotNull Reader reader, @NotNull T parent) throws IOException {
        final T node = source.read(reader, parent);
        parse(source.read(reader, parent));
        return node;
    }

    @Nullable
    @Contract("!null, _, _ -> !null")
    public <T extends MapNode> T set(@Nullable T node, @NotNull Object value, @NotNull String path) {
        if (node == null) {
            return null;
        }
        parse(node, node.getSplit(path).setValue(value));
        return node;
    }
}
