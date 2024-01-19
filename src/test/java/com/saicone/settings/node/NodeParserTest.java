package com.saicone.settings.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.saicone.settings.SettingsParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeParserTest {

    @Test
    public void testParse() {
        MapNode expected = new MapNode();
        expected.child("key1", "Number: 1234");
        expected.child("key2", 55);
        expected.child("key3", 1234);

        MapNode actual = new MapNode();
        actual.child("key1", "Number: [replace this]");
        actual.child("key2", 55);
        actual.child("key3", "[replace this]");
        actual.parse((node, s) -> {
            if (s.equals("[replace this]")) {
                return 1234;
            } else {
                return s.replace("[replace this]", "1234");
            }
        });
        assertEquals(expected, actual);
    }

    @Test
    public void testArgs() {
        MapNode expected = new MapNode();
        expected.child("key1", "Number: 55 and 1234");
        expected.child("key2", 55);
        expected.child("key3", 1234);

        MapNode actual = new MapNode();
        actual.child("key1", "Number: {number} and {0}{1}{2}4");
        actual.child("key2", "{number}");
        actual.child("key3", "{3}");
        actual.replaceArgs(1, 2, 3, 1234).replaceArgs(ImmutableMap.of("number", 55));
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleParser() {
        MapNode expected = new MapNode();
        expected.child("key1", "test: {0}");
        expected.child("key2", "asd");
        expected.child("key3", ImmutableMap.of(
                "sub1", 1234,
                "sub2", true,
                "sub3", ImmutableList.of(1234, 1234)
        ));
        expected.child("key4", ImmutableMap.of(
                "sub1", "test: 1234",
                "sub2", "asd: true",
                "sub3", 1234
        ));

        MapNode actual = new MapNode();
        actual.child("key1", "test: {0}");
        actual.child("key2", "asd");
        actual.child("key3", ImmutableMap.of(
                "sub1", 1234,
                "sub2", true,
                "sub3", ImmutableList.of("${key3.sub1}", 1234)
        ));
        actual.child("key4", ImmutableMap.of(
                "sub1", "${node:key1_$[key3.sub1]}",
                "sub2", "asd: ${key3.sub2}",
                "sub3", "${key3.sub1}"
        ));
        SettingsParser.simple().parse(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testAllParser() {
        MapNode expected = new MapNode();
        expected.child("key1", "test: {0}");
        expected.child("key2", "asd");
        expected.child("key3", ImmutableMap.of(
                "sub1", 1234,
                "sub2", ImmutableSet.of(1234, true, false)
        ));
        expected.child("key4", ImmutableMap.of(
                "sub1", "test: 1234",
                "sub2", "joined: 1234._.true._.false",
                "sub3", 2
        ));

        MapNode actual = new MapNode();
        actual.child("key1", "test: {0}");
        actual.child("key2", "asd");
        actual.child("key3", ImmutableMap.of(
                "sub1", 1234,
                "sub2", ImmutableSet.of(1234, true, false)
        ));
        actual.child("key4", ImmutableMap.of(
                "sub1", "${node:key1_$[key3.sub1]}",
                "sub2", "joined: ${join:key3.sub2_.\\_.}",
                "sub3", "${size:key3}"
        ));
        SettingsParser.all().parse(actual);
        assertEquals(expected, actual);
    }
}
