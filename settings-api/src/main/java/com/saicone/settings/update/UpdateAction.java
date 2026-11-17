package com.saicone.settings.update;

/**
 * Node update action types.
 *
 * @author Rubenicos
 */
public enum UpdateAction {

    /**
     * Custom update action not listed on {@link UpdateAction}.
     */
    CUSTOM,
    /**
     * Add non-existent node with value.
     */
    ADD,
    /**
     * Delete node from path.
     */
    DELETE,
    /**
     * Replace node value from path.
     */
    REPLACE,
    /**
     * Move existent node to path.
     */
    MOVE;

}
