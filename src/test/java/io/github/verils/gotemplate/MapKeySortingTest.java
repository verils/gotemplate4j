package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void mixedNumericKeyTypesPreserveSnapshotOrder() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$v}},{{end}}");

        Map<Number, String> mapData = new LinkedHashMap<>();
        mapData.put(2L, "long-two");
        mapData.put(1, "int-one");
        mapData.put(3, "int-three");

        assertEquals("long-two,int-one,int-three,",
                render(template, data("Data", mapData)));
    }

    @Test
    void mixedBasicKeyTypesPreserveSnapshotOrder() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$v}},{{end}}");

        Map<Object, String> mapData = new LinkedHashMap<>();
        mapData.put("z", "string-z");
        mapData.put(1, "number-one");
        mapData.put("a", "string-a");

        assertEquals("string-z,number-one,string-a,",
                render(template, data("Data", mapData)));
    }

    @Test
    void nullKeysPreserveSnapshotOrder() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$v}},{{end}}");

        Map<Object, String> mapData = new LinkedHashMap<>();
        mapData.put(null, "null-key");
        mapData.put("z", "string-z");

        assertEquals("null-key,string-z,", render(template, data("Data", mapData)));
    }

    @Test
    void customKeysPreserveSnapshotOrderWithoutToStringSorting() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$v}},{{end}}");

        Map<Object, String> mapData = new LinkedHashMap<>();
        mapData.put(new CustomKey("A"), "inserted-first");
        mapData.put(new CustomKey("Z"), "inserted-second");

        assertEquals("inserted-first,inserted-second,",
                render(template, data("Data", mapData)));
    }

    @Test
    void customComparableKeysAlsoPreserveSnapshotOrder() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Data}}{{$v}},{{end}}");

        Map<ComparableKey, String> mapData = new LinkedHashMap<>();
        mapData.put(new ComparableKey(2), "inserted-two");
        mapData.put(new ComparableKey(1), "inserted-one");

        assertEquals("inserted-two,inserted-one,",
                render(template, data("Data", mapData)));
    }

    @Test
    void nestedMapsChooseSortingIndependently() throws IOException, TemplateException {
        Template template = new Template("test").withMapKeySorting(true);
        template.parse("{{range $k, $v := .Outer}}{{$k}}:{{range $ik, $iv := $v}}{{$iv}},{{end}};{{end}}");

        Map<Number, String> mixedInner = new LinkedHashMap<>();
        mixedInner.put(2L, "long-two");
        mixedInner.put(1, "int-one");

        Map<String, Object> outer = new LinkedHashMap<>();
        outer.put("z", mixedInner);
        outer.put("a", mixedInner);

        assertEquals("a:long-two,int-one,;z:long-two,int-one,;",
                render(template, data("Outer", outer)));
    }

    private String render(Template template, Map<String, Object> data) throws IOException, TemplateException {
        Writer writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }

    private Map<String, Object> data(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }

    private static final class CustomKey {
        private final String sortValue;

        private CustomKey(String sortValue) {
            this.sortValue = sortValue;
        }

        @Override
        public String toString() {
            return sortValue;
        }
    }

    private static final class ComparableKey implements Comparable<ComparableKey> {
        private final int value;

        private ComparableKey(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(ComparableKey other) {
            return Integer.compare(value, other.value);
        }
    }

}
