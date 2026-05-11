package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for map key sorting feature.
 */
public class MapKeySortingTest {

    @Test
    void testMapKeySortingEnabledByDefault() throws IOException, TemplateException {
        // Verify that map key sorting is enabled by default (Go template compatibility)
        Template template = new Template("test");
        assertTrue(template.mapKeySorting());
        
        // Test with String keys (Comparable) - should be sorted by default
        template.parse("{{range $k, $v := .Data}}{{$k}}={{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        // Insert in non-alphabetical order
        mapData.put("zebra", "z");
        mapData.put("apple", "a");
        mapData.put("mango", "m");
        data.put("Data", mapData);

        template.execute(writer, data);

        // With sorting enabled by default, keys should be in alphabetical order
        assertEquals("apple=a,mango=m,zebra=z,", writer.toString());
    }

    @Test
    void testMapKeySortingCanBeDisabled() throws IOException, TemplateException {
        // Test that sorting can be explicitly disabled to preserve insertion order
        Template template = new Template("test")
            .withMapKeySorting(false);
        assertFalse(template.mapKeySorting());
        
        template.parse("{{range $k, $v := .Data}}{{$k}}={{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        mapData.put("first", "1");
        mapData.put("second", "2");
        mapData.put("third", "3");
        data.put("Data", mapData);

        template.execute(writer, data);

        // With sorting disabled, should preserve insertion order
        assertEquals("first=1,second=2,third=3,", writer.toString());
    }

    @Test
    void testMapKeySortingWithComparableKeys() throws IOException, TemplateException {
        // Test with String keys (Comparable) - explicit enable for clarity
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$k}}={{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        // Insert in non-alphabetical order
        mapData.put("zebra", "z");
        mapData.put("apple", "a");
        mapData.put("mango", "m");
        data.put("Data", mapData);

        template.execute(writer, data);

        // With sorting enabled, keys should be in alphabetical order
        assertEquals("apple=a,mango=m,zebra=z,", writer.toString());
    }

    @Test
    void testMapKeySortingWithIntegerKeys() throws IOException, TemplateException {
        // Test with Integer keys (Comparable) - explicit enable for clarity
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$k}}={{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<Integer, String> mapData = new LinkedHashMap<>();
        // Insert in non-sequential order
        mapData.put(3, "three");
        mapData.put(1, "one");
        mapData.put(2, "two");
        data.put("Data", mapData);

        template.execute(writer, data);

        // With sorting enabled, keys should be in numerical order
        assertEquals("1=one,2=two,3=three,", writer.toString());
    }


    @Test
    void testMapKeySortingWithMixedCaseStrings() throws IOException, TemplateException {
        // Test case-sensitive string comparison
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$k}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        mapData.put("Zebra", "z");
        mapData.put("apple", "a");
        mapData.put("Banana", "b");
        data.put("Data", mapData);

        template.execute(writer, data);

        // Uppercase letters come before lowercase in ASCII/Unicode
        String result = writer.toString();
        assertTrue(result.indexOf("Banana") < result.indexOf("Zebra"));
        assertTrue(result.indexOf("Zebra") < result.indexOf("apple"));
    }

    @Test
    void testMapKeySortingWithValueOnlyVariable() throws IOException, TemplateException {
        // Test sorting when only value variable is specified
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $v := .Data}}{{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        mapData.put("charlie", "c");
        mapData.put("alpha", "a");
        mapData.put("bravo", "b");
        data.put("Data", mapData);

        template.execute(writer, data);

        // Values should appear in key-sorted order: alpha, bravo, charlie
        assertEquals("a,b,c,", writer.toString());
    }

    @Test
    void testMapKeySortingEmptyMap() throws IOException, TemplateException {
        // Test with empty map
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$k}}{{else}}empty{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new HashMap<>();
        data.put("Data", mapData);

        template.execute(writer, data);

        assertEquals("empty", writer.toString());
    }

    @Test
    void testMapKeySortingPreservedInClone() throws IOException, TemplateException {
        // Test that mapKeySorting setting is preserved when cloning templates
        Template original = new Template("test")
            .withMapKeySorting(true);
        
        Template cloned = new Template(original);
        assertTrue(cloned.mapKeySorting());

        // Verify it works in cloned template
        cloned.parse("{{range $k, $v := .Data}}{{$k}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> mapData = new LinkedHashMap<>();
        mapData.put("z", "1");
        mapData.put("a", "2");
        data.put("Data", mapData);

        cloned.execute(writer, data);

        assertEquals("a,z,", writer.toString());
    }

    @Test
    void testMapKeySortingWithNestedMaps() throws IOException, TemplateException {
        // Test sorting with nested maps
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Outer}}{{$k}}:{{range $ik, $iv := $v}}{{$ik}}={{$iv}},{{end}};{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        Map<String, Object> inner1 = new LinkedHashMap<>();
        inner1.put("y", 1);
        inner1.put("x", 2);
        
        Map<String, Object> inner2 = new LinkedHashMap<>();
        inner2.put("b", 3);
        inner2.put("a", 4);
        
        Map<String, Object> outer = new LinkedHashMap<>();
        outer.put("beta", inner2);
        outer.put("alpha", inner1);
        
        data.put("Outer", outer);

        template.execute(writer, data);

        // Both outer and inner maps should be sorted
        assertEquals("alpha:x=2,y=1,;beta:a=4,b=3,;", writer.toString());
    }

    @Test
    void testMapKeySortingWithNonComparableKeys() throws IOException, TemplateException {
        // Test with custom objects that don't implement Comparable
        // Should fall back to toString() comparison
        Template template = new Template("test")
            .withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$k}}={{$v}},{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        // Use a simple wrapper class without Comparable
        Map<CustomKey, String> mapData = new LinkedHashMap<>();
        mapData.put(new CustomKey("Charlie"), "c");
        mapData.put(new CustomKey("Alpha"), "a");
        mapData.put(new CustomKey("Bravo"), "b");
        data.put("Data", mapData);

        template.execute(writer, data);

        // Should sort by toString() which returns the name
        String result = writer.toString();
        assertEquals("Alpha=a,Bravo=b,Charlie=c,", result);
    }

    /**
     * Simple custom key class for testing non-Comparable keys
     */
    private static class CustomKey {
        private final String name;

        CustomKey(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CustomKey that = (CustomKey) obj;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
