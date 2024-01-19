package com.saicone.settings.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeParserTest {

    @Test
    public void testString() {
        assertEquals("test", ValueType.of("test").asString());
        assertEquals("t", ValueType.of('t').asString());
        assertEquals("true", ValueType.of(true).asString());
        assertEquals("1234", ValueType.of(1234).asString());
    }

    @Test
    public void testCharacter() {
        assertEquals('t', ValueType.of("test").asChar());
        assertEquals('a', ValueType.of('a').asChar());
        assertEquals('t', ValueType.of(true).asChar());
        assertEquals('1', ValueType.of(1234).asChar());
    }

    @Test
    public void testBoolean() {
        assertEquals(true, ValueType.of("true").asBoolean());
        assertEquals(true, ValueType.of('y').asBoolean());
        assertEquals(true, ValueType.of(true).asBoolean());
        assertEquals(true, ValueType.of(1).asBoolean());
    }

    @Test
    public void testNumber() {
        assertEquals(1234, ValueType.of("1234").asInt());
        assertEquals(3f, ValueType.of('3').asFloat());
        assertEquals(1L, ValueType.of(true).asLong());
        assertEquals(20.5D, ValueType.of(20.5D).asDouble());
    }

    @Test
    public void testUniqueId() {
        UUID expected = UUID.fromString("7ca003dc-175f-4f1f-b490-5651045311ad");
        assertEquals(expected, ValueType.of("7ca003dc-175f-4f1f-b490-5651045311ad").asUniqueId());
        assertEquals(expected, ValueType.of(new int[] { 2090861532, 392122143, -1265609135, 72552877 }).asUniqueId());
    }

    @Test
    public void testList() {
        assertEquals("test", ValueType.of(ImmutableList.of("test")).asString());
        assertEquals("1234", ValueType.of(ImmutableList.of(1234)).asString());
        assertEquals(ImmutableList.of("test"), ValueType.of("test").asStringList());
        assertEquals(ImmutableList.of(1234), ValueType.of("1234").asIntList());
        assertEquals(ImmutableList.of("true", "true", "false"), ValueType.of(ImmutableList.of(true, true, false)).asStringList());
        assertEquals(ImmutableList.of(1, 2, 3), ValueType.of(ImmutableList.of("1", "2", "3")).asIntList());
    }

    @Test
    public void testSet() {
        assertEquals(ImmutableSet.of("true", "false"), ValueType.of(ImmutableList.of(true, true, false)).asSet(Types.STRING));
        assertEquals(ImmutableSet.of(1, 2, 3), ValueType.of(ImmutableList.of("1", "2", "2", "3", "3", "3")).asSet(Types.INT));
    }
}
