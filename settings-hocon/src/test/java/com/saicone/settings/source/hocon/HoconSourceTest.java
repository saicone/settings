package com.saicone.settings.source.hocon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.saicone.settings.Settings;
import com.saicone.settings.SettingsData;
import com.saicone.settings.data.DataType;
import com.saicone.settings.node.MapNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HoconSourceTest {

    @Test
    public void testDataLoad() {
        MapNode expected = new MapNode();
        expected.put("key1", "test");
        expected.put("key2", 1234);
        expected.put("key3", ImmutableMap.of(
                "sub1", "asd",
                "sub2", 1234
        ));
        expected.put("key4", ImmutableMap.of(
                "sub1", 55,
                "sub2", ImmutableList.of("value1", "value2")
        ));

        SettingsData<Settings> data = SettingsData.of(DataType.FILE_RESOURCE, "/example.conf");
        Settings actual = data.load();
        assertEquals(expected, actual);

        assertEquals(ImmutableList.of("Key #1 comment"), actual.get("key1").getTopComment());
        assertEquals(ImmutableList.of("Comment"), actual.get("key3").getTopComment());
        assertEquals(ImmutableList.of("List comment"), actual.get("key4", "sub2").getTopComment());
    }
}
