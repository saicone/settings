package com.saicone.settings.source;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.node.NodeKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.AnchorNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A settings source for yaml-formatted data<br>
 * This class uses snakeyaml library to read and write any data.
 *
 * @author Rubenicos
 */
public class YamlSettingsSource implements SettingsSource {

    private PublicYaml yaml;

    /**
     * Constructs a yaml settings source with default options.<br>
     * This means any comment will be parsed and the data will be written using pretty flow.
     */
    public YamlSettingsSource() {
        final LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        final DumperOptions dumpOptions = new DumperOptions();
        dumpOptions.setPrettyFlow(true);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOptions.setProcessComments(true);

        this.yaml = new PublicYaml(new PublicConstructor(loaderOptions), new Representer(dumpOptions), dumpOptions, loaderOptions);
    }

    /**
     * Constructs a yaml settings source with provided {@link PublicYaml} wrapped instance.
     *
     * @param yaml the yaml instance to use.
     */
    public YamlSettingsSource(@NotNull PublicYaml yaml) {
        this.yaml = yaml;
    }

    private static DumperOptions defaultDumperOptions() {
        final DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setProcessComments(true);
        return options;
    }

    /**
     * Get the current yaml instance.
     *
     * @return a yaml instance.
     */
    @NotNull
    public Yaml getYaml() {
        return yaml;
    }

    /**
     * Replace the current yaml instance with provided {@link PublicYaml} wrapped instance.
     *
     * @param yaml the yaml instance to use.
     */
    public void setYaml(@NotNull PublicYaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        return readMapNode(parent, (MappingNode) realNode(yaml.compose(reader)));
    }

    /**
     * Read yaml node as settings node with provided parameters.
     *
     * @param parent the associated parent node that this node belongs from.
     * @param key    the node key.
     * @param node   the yaml node to read.
     * @return       a newly created settings node.
     */
    @NotNull
    public SettingsNode readNode(@Nullable MapNode parent, @Nullable String key, @NotNull Node node) {
        if (node instanceof MappingNode) {
            return readMapNode(new MapNode(parent, key), (MappingNode) node);
        } else if (node instanceof SequenceNode) {
            final ListNode list = new ListNode(parent, key);
            if (((SequenceNode) node).getValue().isEmpty()) {
                return list;
            }
            for (Node value : ((SequenceNode) node).getValue()) {
                final SettingsNode child = readNode(null, null, value);
                child.setTopComment(readCommentLines(value.getBlockComments()));
                child.setSideComment(readCommentLines(value.getInLineComments()));
                list.add(child);
            }
            return list;
        } else {
            return NodeKey.of(parent, key, yaml.getConstructor().constructObject(node));
        }
    }

    /**
     * Read yaml mapping node values and save into provided parent map node.
     *
     * @param parent the parent node to append values.
     * @param node   the yaml node to read.
     * @return       the provided parent node.
     * @param <T>    the map node type.
     */
    @Nullable
    @Contract("!null, _ -> !null")
    public <T extends MapNode> T readMapNode(@Nullable T parent, @Nullable MappingNode node) {
        if (node == null || node.getValue().isEmpty()) {
            return parent;
        }
        for (NodeTuple tuple : node.getValue()) {
            // Get node key and value
            final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
            final Node valueNode = realNode(tuple.getValueNode());

            // Read child node
            final String key = keyNode.getValue();
            final SettingsNode child = readNode(parent, key, valueNode);

            // Read comments
            child.setTopComment(readCommentLines(keyNode.getBlockComments()));
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode) {
                child.setSideComment(readCommentLines(keyNode.getInLineComments()));
            } else {
                child.setSideComment(readCommentLines(valueNode.getInLineComments()));
            }

            // Save child node
            if (parent != null) {
                parent.put(key, child);
            }
        }
        return parent;
    }

    /**
     * Parse yaml comment lines as list of strings.
     *
     * @param list the yaml comment to read.
     * @return     a list of strings containing all the comment lines, null otherwise.
     */
    public List<String> readCommentLines(@Nullable List<CommentLine> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        final List<String> comment = new ArrayList<>();
        for (CommentLine line : list) {
            String s;
            if (line.getCommentType() == CommentType.BLANK_LINE) {
                s = "";
            } else {
                s = line.getValue().replace("\r", "");
                if (!s.isEmpty() && s.charAt(0) == ' ') {
                    s = s.substring(1);
                }
            }
            comment.add(s);
        }
        return comment;
    }

    private static Node realNode(final Node yamlNode) {
        if (yamlNode instanceof AnchorNode) {
            return ((AnchorNode) yamlNode).getRealNode();
        } else {
            return yamlNode;
        }
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        yaml.serialize(writeNode(parent), writer);
    }

    /**
     * Write any object into yaml node object.
     *
     * @param object the object to be converted.
     * @return       a newly generated yaml node from provided object, null otherwise.
     */
    @Nullable
    @Contract("!null -> !null")
    public Node writeNode(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof SettingsNode) {
            return writeNode(((SettingsNode) object).getSourceValue());
        }

        if (object instanceof Map) {
            final List<NodeTuple> values = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                // Create tuple value
                final Node valueNode = writeNode(entry.getValue());
                if (valueNode == null) {
                    continue;
                }
                final Node keyNode = yaml.getRepresenter().represent(entry.getKey());

                // Set comments if necessary
                if (entry.getValue() instanceof SettingsNode) {
                    final SettingsNode node = (SettingsNode) entry.getValue();
                    writeCommentLines(node.getTopComment(), keyNode, CommentType.BLOCK);
                    if (node.isMap() || node.isList()) {
                        writeCommentLines(node.getSideComment(), keyNode, CommentType.IN_LINE);
                    } else {
                        writeCommentLines(node.getSideComment(), valueNode, CommentType.IN_LINE);
                    }
                }

                // Save into values
                values.add(new NodeTuple(keyNode, valueNode));
            }
            return new MappingNode(Tag.MAP, values, yaml.getRepresenter().getDefaultFlowStyle());
        } else if (object instanceof Iterable) {
            final List<Node> values = new ArrayList<>();
            for (Object value : (Iterable<?>) object) {
                final Node nodeValue = writeNode(value);
                if (nodeValue == null) {
                    continue;
                }
                if (value instanceof SettingsNode) {
                    final SettingsNode node = (SettingsNode) value;
                    writeCommentLines(node.getTopComment(), nodeValue, CommentType.BLOCK);
                    writeCommentLines(node.getSideComment(), nodeValue, CommentType.IN_LINE);
                }
                values.add(nodeValue);
            }
            return new SequenceNode(Tag.SEQ, values, yaml.getRepresenter().getDefaultFlowStyle());
        } else {
            return yaml.getRepresenter().represent(object);
        }
    }

    /**
     * Write provided list of strings as comment lines into provided node with comment type.
     *
     * @param comment     the comment lines to write.
     * @param node        the node to append comment lines.
     * @param commentType the type of comment.
     */
    public void writeCommentLines(@Nullable List<String> comment, @NotNull Node node, @NotNull CommentType commentType) {
        if (comment == null) {
            return;
        }
        final List<CommentLine> list = new ArrayList<>();
        for (String line : comment) {
            if (line == null || line.trim().isEmpty()) {
                list.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
            } else {
                list.add(new CommentLine(null, null, ' ' + line, commentType));
            }
        }
        if (commentType == CommentType.BLOCK) {
            node.setBlockComments(list);
        } else if (commentType == CommentType.IN_LINE) {
            node.setInLineComments(list);
        }
    }

    /**
     * Yaml wrapped instance with public method to get used constructor and representer instances.
     */
    public static final class PublicYaml extends Yaml {

        private final PublicConstructor constructor;
        private final Representer representer;

        /**
         * Constructs a public yaml wrapped instance.
         *
         * @param constructor   the used constructor to read yaml objects.
         * @param representer   the used representer to write yaml objects.
         * @param dumperOptions the dump options to write data.
         * @param loadingConfig the loader configuration to read data.
         */
        public PublicYaml(@NotNull PublicConstructor constructor, @NotNull Representer representer, @NotNull DumperOptions dumperOptions, @NotNull LoaderOptions loadingConfig) {
            super(constructor, representer, dumperOptions, loadingConfig);
            this.constructor = constructor;
            this.representer = representer;
        }

        /**
         * Get the used constructor instance that read yaml objects.
         *
         * @return a public constructor wrapped class.
         */
        @NotNull
        public PublicConstructor getConstructor() {
            return constructor;
        }

        /**
         * Get the used representer instance that write yaml objects.
         *
         * @return a representer.
         */
        @NotNull
        public Representer getRepresenter() {
            return representer;
        }
    }

    /**
     * Constructor wrapped instance to read java objects from yaml nodes.
     */
    public static class PublicConstructor extends Constructor {

        /**
         * Constructs a public constructor wrapped instance.
         *
         * @param options the loader options to read data.
         */
        public PublicConstructor(@NotNull LoaderOptions options) {
            super(options);
        }

        @Override
        public Object constructObject(Node node) {
            return super.constructObject(node);
        }
    }
}
