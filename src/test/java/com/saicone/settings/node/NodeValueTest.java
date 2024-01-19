package com.saicone.settings.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.saicone.settings.SettingsNode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeValueTest {

    @Test
    public void testValue() {
        SettingsNode node = NodeValue.of("test");
        assertEquals("test", node.getValue());

        node.setValue(1234);
        assertEquals(1234, node.getValue());
        assertEquals("test", node.getSourceValue());

        assertEquals(NodeValue.of(1234), node);

        node = node.setValue(ImmutableMap.of("number", 1234));
        assertEquals(NodeValue.of(ImmutableMap.of("number", 1234)), node);
        assertEquals("test", node.getSourceValue());

        node = node.setValue(ImmutableList.of(1, 2, 3));
        assertEquals(NodeValue.of(ImmutableList.of(1, 2, 3)), node);
    }

    @Test
    public void testMapValue() {
        MapNode node = new MapNode();
        node.child("test", "asd");
        node.child("number", 1234);
        node.child("key", true);
        node.child("sub", ImmutableMap.of("value", false, "list", ImmutableList.of("value1", "value2", 3)));

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("test", "asd");
        expected.put("number", 1234);
        expected.put("key", true);
        expected.put("sub", ImmutableMap.of("value", false, "list", ImmutableList.of("value1", "value2", 3)));
        assertEquals(expected, node.asLiteralObject());

        String json = "{\"test\": \"asd\", \"number\": 1234, \"key\": true, \"sub\": {\"value\": false, \"list\": [\"value1\", \"value2\", 3]}}";
        assertEquals(json, node.asJson());
    }

    @Test
    public void testMapMerge() {
        MapNode expected = new MapNode();
        expected.child("test", "asd");
        expected.child("number", 55);
        // Replace value at "number" key
        expected.merge(ImmutableMap.of("number", 1234), true);

        MapNode actual = new MapNode();
        actual.merge(ImmutableMap.of("test", "asd", "number", 1234));
        // "number" key already exists
        actual.merge(ImmutableMap.of("number", 55));

        assertEquals(expected, actual);
    }

    @Test
    public void testMapDeepMerge() {
        MapNode expected = new MapNode();
        expected.child("test", "asd");
        expected.child("sub", ImmutableMap.of("number", 55, "key", true));
        // Replace value at "number" key
        expected.deepMerge(ImmutableMap.of("sub", ImmutableMap.of("number", 1234)), true);

        MapNode actual = new MapNode();
        actual.merge(ImmutableMap.of("test", "asd", "sub", ImmutableMap.of("number", 1234, "key", true)));
        // "number" key already exists
        actual.deepMerge(ImmutableMap.of("sub", ImmutableMap.of("number", 55)));

        assertEquals(expected, actual);
    }
}
