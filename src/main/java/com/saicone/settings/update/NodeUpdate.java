package com.saicone.settings.update;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.node.MapNode;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Class to handle any node transformation to do a proper update.
 *
 * @author Rubenicos
 */
public class NodeUpdate {

    private final UpdateAction action;
    private Function<MapNode, SettingsNode> getter;
    private String[] path;
    private Object value;

    /**
     * Create a node update that aims to add the provided value.
     *
     * @param value the value to add.
     * @return      a node update that adds a value.
     */
    @NotNull
    public static NodeUpdate add(@NotNull Object value) {
        return new NodeUpdate(UpdateAction.ADD).value(value);
    }

    /**
     * Create a node update that aims to delete a node.
     *
     * @return a node update that deletes a node.
     */
    @NotNull
    public static NodeUpdate delete() {
        return new NodeUpdate(UpdateAction.DELETE);
    }

    /**
     * Create a node update that aims to replace a node value with the given value.
     *
     * @param value the value that wil be set.
     * @return      a node update that replace a node value.
     */
    @NotNull
    public static NodeUpdate replace(@NotNull Object value) {
        return new NodeUpdate(UpdateAction.REPLACE).value(value);
    }

    /**
     * Create a node update that aims to move an existent node.
     *
     * @return a node update that move a node.
     */
    @NotNull
    public static NodeUpdate move() {
        return new NodeUpdate(UpdateAction.MOVE);
    }

    /**
     * Create a node update that aims to do a custom transformation.
     *
     * @param function the function that accepts a parent map node and probably return itself.
     * @return         a node update that do a custom transformation.
     */
    @NotNull
    public static NodeUpdate custom(@NotNull Function<MapNode, MapNode> function) {
        return new NodeUpdate(UpdateAction.CUSTOM) {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends MapNode> T apply(@NotNull T parent) {
                return (T) function.apply(parent);
            }
        };
    }

    /**
     * Construct a node update with the given update action.
     *
     * @param action the action to apply.
     */
    public NodeUpdate(@NotNull UpdateAction action) {
        this.action = action;
    }

    /**
     * Get the update action.
     *
     * @return a node update action.
     */
    @NotNull
    public UpdateAction getAction() {
        return action;
    }

    /**
     * Get a node from the given map node using the current node getter.
     *
     * @param parent the parent node to get a node.
     * @return       a node from parent if getter exists, null otherwise.
     */
    @Nullable
    public SettingsNode getNode(@NotNull MapNode parent) {
        return getter == null ? null : getter.apply(parent);
    }

    /**
     * Get the path that this node update will be to.
     *
     * @return a key path if is set, null otherwise.
     */
    @Nullable
    public String[] getPath() {
        return path;
    }

    /**
     * Get the value that will be set into node.
     *
     * @return a node value if is set, null otherwise.
     */
    @Nullable
    public Object getValue() {
        return value;
    }

    /**
     * Set the current node getter of this update.
     *
     * @param getter a function that apply a map node.
     * @return       this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate from(@NotNull Function<MapNode, SettingsNode> getter) {
        this.getter = getter;
        return this;
    }

    /**
     * Set the node path where it will be got.
     *
     * @param path the key path.
     * @return     this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate from(@NotNull String... path) {
        return from(map -> map.get(path));
    }

    /**
     * Set the node path where it will be got.<br>
     * This type of path ignores key case.
     *
     * @param path the case-insensitive key path.
     * @return     this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate fromIgnoreCase(@NotNull String... path) {
        return from(map -> map.getIgnoreCase(path));
    }

    /**
     * Set the node path where it will be got.<br>
     * This type of path compares any children node key with the provided regex pattern array.
     *
     * @param path the regex pattern path.
     * @return     this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate fromRegex(@NotNull @Language("RegExp") String... path) {
        return from(map -> map.getRegex(path));
    }

    /**
     * Set the node path where it will be set.
     *
     * @param path the key path.
     * @return     this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate to(@NotNull String... path) {
        this.path = path;
        return this;
    }

    /**
     * Set the node value that will be set to updated node.
     *
     * @param value the value to set.
     * @return      this node update.
     */
    @NotNull
    @Contract("_ -> this")
    public NodeUpdate value(@Nullable Object value) {
        this.value = value;
        return this;
    }

    /**
     * Apply this node update into provided map node parent.
     *
     * @param parent the parent node to apply this update.
     * @return       the effective map node in this operation, normally the same map node.
     * @param <T>    the map node type.
     */
    public <T extends MapNode> T apply(@NotNull T parent) {
        final SettingsNode node = getNode(parent);
        if (node == null) {
            return parent;
        }
        switch (action) {
            case ADD:
                if (!node.isReal()) {
                    node.setValue(value);
                    if (node.getKey() == null) {
                        final String[] path = getPath();
                        if (path != null) {
                            node.move(path);
                        }
                    }
                }
                break;
            case DELETE:
                node.delete(true);
                break;
            case REPLACE:
                node.setValue(value);
                break;
            case MOVE:
                final String[] path = getPath();
                if (path != null) {
                    node.move(path);
                }
                break;
            default:
                break;
        }
        return parent;
    }
}
