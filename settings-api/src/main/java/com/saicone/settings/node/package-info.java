/**
 * This package contains the node implementations that form the settings tree.
 *
 * <p>The base of any node is the {@link com.saicone.settings.node.NodeValue} class, extended by
 * {@link com.saicone.settings.node.NodeKey} which adds parent and key support.</p>
 *
 * <p>The concrete representations of a node are {@link com.saicone.settings.node.MapNode},
 * {@link com.saicone.settings.node.ListNode} and {@link com.saicone.settings.node.ObjectNode},
 * which hold map, list and single object values respectively.</p>
 *
 * @author Rubenicos
 */
package com.saicone.settings.node;
