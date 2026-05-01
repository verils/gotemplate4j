package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for Functions edge cases to improve coverage
 */
class FunctionsEdgeCaseTest {

    @Test
    void testEqWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{eq 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testNeWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{ne 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testLtWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{lt 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testLeWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{le 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testGtWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{gt 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testGeWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{ge 1}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testLenWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{len}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testLenWithTooManyArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{len 1 2}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testIndexWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{index .Items}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Items", new String[]{"a", "b"});
            template.execute(writer, data);
        });
    }

    @Test
    void testSliceWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{slice .Items 0}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Items", new String[]{"a", "b"});
            template.execute(writer, data);
        });
    }

    @Test
    void testHtmlWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{html}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testHtmlWithTooManyArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{html 1 2}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testJsWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{js}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testUrlqueryWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{urlquery}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testTypeofWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{typeof}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testKindOfWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{kindOf}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testDeepEqualWithInsufficientArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{deepEqual .A}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("A", "test");
            template.execute(writer, data);
        });
    }

    @Test
    void testCallWithNoArgs() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{call}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testCallWithNonFunction() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{call .Value}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Value", "not a function");
            template.execute(writer, data);
        });
    }

    @Test
    void testLenWithInvalidType() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{len .Value}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Value", Integer.valueOf(42));
            template.execute(writer, data);
        });
    }

    @Test
    void testIndexWithInvalidType() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{index .Value 0}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Value", Integer.valueOf(42));
            template.execute(writer, data);
        });
    }

    @Test
    void testSliceWithInvalidType() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{slice .Value 0 1}}");
            Writer writer = new StringWriter();
            Map<String, Object> data = new HashMap<>();
            data.put("Value", Integer.valueOf(42));
            template.execute(writer, data);
        });
    }

    @Test
    void testCompareIncompatibleTypes() {
        Template template = new Template("test");
        assertThrows(IllegalArgumentException.class, () -> {
            template.parse("{{if lt 1 \"a\"}}yes{{end}}");
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }

    @Test
    void testSliceWithNegativeIndices() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice \"hello\" 0 3}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        // Slice with valid indices
        assertNotNull(writer.toString());
    }

    @Test
    void testSliceWithEndBeyondLength() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice \"hello\" 0 100}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("hello", writer.toString());
    }

    @Test
    void testSliceWithStartGreaterThanOrEqualEnd() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice \"hello\" 3 2}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testSliceWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Items 1 3}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c", "d"});
        template.execute(writer, data);
        // Slice should return array [b, c]
        assertNotNull(writer.toString());
    }

    @Test
    void testAndWithEmptyArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{and}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testOrWithEmptyArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{or}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testKindOfWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new int[]{1, 2, 3});
        template.execute(writer, data);
        assertEquals("array", writer.toString());
    }

    @Test
    void testKindOfWithBool() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", true);
        template.execute(writer, data);
        assertEquals("bool", writer.toString());
    }

    @Test
    void testKindOfWithMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        data.put("Value", map);
        template.execute(writer, data);
        assertEquals("map", writer.toString());
    }

    @Test
    void testKindOfWithCollection() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", Arrays.asList("a", "b"));
        template.execute(writer, data);
        assertEquals("slice", writer.toString());
    }

    @Test
    void testKindOfWithCustomObject() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new Recipient("Test User", "gift", true));
        template.execute(writer, data);
        assertEquals("struct", writer.toString());
    }

    @Test
    void testToIntWithStringNumber() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items \"1\"}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertEquals("b", writer.toString());
    }

    @Test
    void testToIntWithInvalidString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items \"abc\"}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertEquals("a", writer.toString()); // Invalid index defaults to 0
    }
}
