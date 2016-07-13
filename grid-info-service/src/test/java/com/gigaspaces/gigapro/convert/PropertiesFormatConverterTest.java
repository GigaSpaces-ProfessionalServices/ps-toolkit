package com.gigaspaces.gigapro.convert;

import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class PropertiesFormatConverterTest {

    private Converter converter = new PropertiesFormatConverter();

    @Test
    public void testConvertNull() {
        assertEquals("Output should be empty", "", converter.convert(null));
    }

    @Test
    public void testConvertPropertyKeySpecified() {
        class TestData {
            @PropertyKey("test-field1")
            private int simpleType = 1;
            @PropertyKey("test-field2")
            private Object object = "foo";
        }

        TestData testData = new TestData();

        String output = converter.convert(testData);
        assertNotNull("Outupt should not be null", output);
        String[] lines = output.split("\n");
        assertEquals("Output should have 2 lines", 2, lines.length);
        Arrays.sort(lines);
        assertEquals("Invalid line", String.format("test-field1=%s", testData.simpleType), lines[0]);
        assertEquals("Invalid line", String.format("test-field2=%s", testData.object), lines[1]);
    }

    @Test
    public void testConvertNoPropertyKeySpecified() {
        class TestData {
            private int simpleType = 1;
            private Object object = "foo";
            private String[] array = { "foo", "boo" };
            private List<Long> list = Arrays.asList(1l, 2l, 3l);
            private Set<Double> set = new HashSet<>(Arrays.asList(1.1, 2.2, 3.3));
            private Map<String, Integer> map = new HashMap<>();
            {
                map.put("first", 1);
                map.put("second", 2);
            }
        }

        TestData testData = new TestData();

        String output = converter.convert(testData);
        assertNotNull("Output should not be null", output);
        String[] lines = output.split("\n");
        assertEquals("Output should have 6 lines", 6, lines.length);
        Arrays.sort(lines);
        assertEquals("Invalid line", String.format("array=%s,%s", testData.array[0], testData.array[1]), lines[0]);
        assertEquals("Invalid line", String.format("list=%s,%s,%s", testData.list.get(0), testData.list.get(1), testData.list.get(2)), lines[1]);
        assertTrue("Invalid line format", lines[2].matches("map=\\w+:\\d,\\w+:\\d"));
        for (Entry<String, Integer> entry : testData.map.entrySet()) {
            assertTrue(lines[2].contains(entry.getKey()));
            assertTrue(lines[2].contains(entry.getValue().toString()));
        }
        assertEquals("Invalid line", String.format("object=%s", testData.object), lines[3]);
        assertTrue("Invalid line format", lines[4].matches("set=\\d\\.\\d,\\d\\.\\d,\\d\\.\\d"));
        for (Double d : testData.set) {
            assertTrue(lines[4].contains(d.toString()));
        }
        assertEquals("Invalid line", String.format("simpleType=%d", testData.simpleType), lines[5]);
    }

    @Test
    public void testConvertTransientField() {
        class TestData {
            private transient int simpleType = 1;
        }
        TestData testData = new TestData();
        assertEquals("Output should be empty", "", converter.convert(testData));
    }
}
