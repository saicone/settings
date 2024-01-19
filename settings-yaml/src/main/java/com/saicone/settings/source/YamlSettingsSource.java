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
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlSettingsSource implements SettingsSource {

    private PublicYaml yaml;

    public YamlSettingsSource() {
        final LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true);
        final DumperOptions dumpOptions = new DumperOptions();
        dumpOptions.setPrettyFlow(true);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOptions.setProcessComments(true);

        this.yaml = new PublicYaml(new PublicConstructor(loaderOptions), new Representer(dumpOptions), dumpOptions, loaderOptions);
    }

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

    @NotNull
    public Yaml getYaml() {
        return yaml;
    }

    public void setYaml(@NotNull PublicYaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        return readMapNode(parent, (MappingNode) realNode(yaml.compose(reader)));
    }

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
                child.setTopComment(readComment(value.getBlockComments()));
                child.setSideComment(readComment(value.getInLineComments()));
                list.add(child);
            }
            return list;
        } else {
            return NodeKey.of(parent, key, yaml.getConstructor().constructObject(node));
        }
    }

    @Nullable
    @Contract("!null, _ -> !null")
    public <T extends MapNode> T readMapNode(@Nullable T parent, @NotNull MappingNode node) {
        if (node.getValue().isEmpty()) {
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
            child.setTopComment(readComment(keyNode.getBlockComments()));
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode) {
                child.setSideComment(readComment(keyNode.getInLineComments()));
            } else {
                child.setSideComment(readComment(valueNode.getInLineComments()));
            }

            // Save child node
            if (parent != null) {
                parent.put(key, child);
            }
        }
        return parent;
    }

    public List<String> readComment(@Nullable List<CommentLine> list) {
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
                    writeComment(node.getTopComment(), keyNode, CommentType.BLOCK);
                    if (node.isMap() || node.isList()) {
                        writeComment(node.getSideComment(), keyNode, CommentType.IN_LINE);
                    } else {
                        writeComment(node.getSideComment(), valueNode, CommentType.IN_LINE);
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
                    writeComment(node.getTopComment(), nodeValue, CommentType.BLOCK);
                    writeComment(node.getSideComment(), nodeValue, CommentType.IN_LINE);
                }
                values.add(nodeValue);
            }
            return new SequenceNode(Tag.SEQ, values, yaml.getRepresenter().getDefaultFlowStyle());
        } else {
            return yaml.getRepresenter().represent(object);
        }
    }

    public void writeComment(@Nullable List<String> comment, @NotNull Node node, @NotNull CommentType commentType) {
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

    public static final class PublicYaml extends Yaml {

        private final PublicConstructor constructor;
        private final Representer representer;

        public PublicYaml(@NotNull PublicConstructor constructor, @NotNull Representer representer, @NotNull DumperOptions dumperOptions, @NotNull LoaderOptions loadingConfig) {
            super(constructor, representer, dumperOptions, loadingConfig);
            this.constructor = constructor;
            this.representer = representer;
        }

        @NotNull
        public PublicConstructor getConstructor() {
            return constructor;
        }

        @NotNull
        public Representer getRepresenter() {
            return representer;
        }
    }

    public static class PublicConstructor extends Constructor {

        public PublicConstructor(@NotNull LoaderOptions options) {
            super(options);
        }

        @Override
        public Object constructObject(Node node) {
            return super.constructObject(node);
        }
    }
}
