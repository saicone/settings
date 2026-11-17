package com.saicone.settings.node;

import com.google.common.collect.ImmutableMap;
import com.saicone.settings.update.SettingsUpdater;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeUpdaterTest {

    @Test
    public void testSimpleUpdate() {
        MapNode expected = new MapNode();
        expected.child("key1", "Number: 1234");
        expected.child("key2", 55);
        expected.child("key3", ImmutableMap.of("sub1", true, "sub2", false));

        MapNode actual = new MapNode();
        actual.child("key2", 55);
        actual.child("key3", ImmutableMap.of("sub1", true));

        MapNode provider = new MapNode();
        provider.child("key1", "Number: 1234");
        provider.child("key3", ImmutableMap.of("sub1", false, "sub2", false));
        SettingsUpdater.simple().update(actual, provider);

        assertEquals(expected, actual);
    }
}
