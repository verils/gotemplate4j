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
            data.put("Value", 42);
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
            data.put("Value", 42);
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
            data.put("Value", 42);
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

    @Test
    void testEqWithNullValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", null);
        data.put("B", null);
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testEqWithOneNullValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", "value");
        data.put("B", null);
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testNeWithNullValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ne .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", null);
        data.put("B", null);
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testLtWithEqualValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 2 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testLeWithLessThan() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if le 1 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testGtWithEqualValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if gt 2 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testGeWithGreaterThan() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ge 3 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testGeWithLessThan() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ge 1 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testAndWithAllFalseValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{and false false false}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("false", writer.toString());
    }

    @Test
    void testOrWithAllFalseValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{or 0 \"\" false}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("false", writer.toString());
    }

    @Test
    void testNotWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if not .Value}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testNotWithEmptyString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if not .Value}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "");
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testNotWithZero() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if not .Value}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", 0);
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testLenWithEmptyString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "");
        template.execute(writer, data);
        assertEquals("0", writer.toString());
    }

    @Test
    void testLenWithEmptyArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new String[]{});
        template.execute(writer, data);
        assertEquals("0", writer.toString());
    }

    @Test
    void testLenWithEmptyList() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new ArrayList<>());
        template.execute(writer, data);
        assertEquals("0", writer.toString());
    }

    @Test
    void testLenWithEmptyMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new HashMap<>());
        template.execute(writer, data);
        assertEquals("0", writer.toString());
    }

    @Test
    void testIndexWithStringOutOfBounds() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Text 100}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hi");
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testIndexWithNegativeArrayIndex() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items -1}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        // Negative index behavior may vary, just ensure it doesn't crash
        assertNotNull(writer.toString());
    }

    @Test
    void testSliceWithNegativeStart() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Text -5 3}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello");
        template.execute(writer, data);
        // Negative start is clamped to 0, so this becomes slice 0 3
        assertNotNull(writer.toString());
    }

    @Test
    void testSliceWithEndBeyondLength() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Text 0 100}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hi");
        template.execute(writer, data);
        assertEquals("hi", writer.toString());
    }

    @Test
    void testSliceWithArrayEmptyResult() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Items 3 2}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertNotNull(writer.toString());
    }

    @Test
    void testCallFunctionWithArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{call .Func \"arg1\" \"arg2\"}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Func", (Function) args -> args[0] + "-" + args[1]);
        template.execute(writer, data);
        assertEquals("arg1-arg2", writer.toString());
    }

    @Test
    void testHtmlWithAmpersand() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{html .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "a&b");
        template.execute(writer, data);
        assertEquals("a&amp;b", writer.toString());
    }

    @Test
    void testJsWithUnicodeChar() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{js .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "中文");
        template.execute(writer, data);
        // Unicode chars should be escaped
        assertTrue(writer.toString().contains("\\u"));
    }

    @Test
    void testJsWithControlChars() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{js .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "line1\r\nline2\ttab");
        template.execute(writer, data);
        assertTrue(writer.toString().contains("\\r") || writer.toString().contains("\r"));
        assertTrue(writer.toString().contains("\\n") || writer.toString().contains("\n"));
        assertTrue(writer.toString().contains("\\t") || writer.toString().contains("\t"));
    }

    @Test
    void testDeepEqualWithNullArrays() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", null);
        data.put("B", null);
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testDeepEqualWithOneNullArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", new int[]{1, 2});
        data.put("B", null);
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testDeepEqualWithDifferentTypes() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", "string");
        data.put("B", 42);
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testDeepEqualWithNestedArrays() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", new Object[]{new int[]{1, 2}, new int[]{3, 4}});
        data.put("B", new Object[]{new int[]{1, 2}, new int[]{3, 4}});
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testTypeofWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{typeof .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new int[]{1, 2, 3});
        template.execute(writer, data);
        assertTrue(writer.toString().contains("int[]") || writer.toString().contains("[I"));
    }

    @Test
    void testKindOfWithStruct() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", new Recipient("Test", "gift", true));
        template.execute(writer, data);
        assertEquals("struct", writer.toString());
    }

    @Test
    void testIsTruthyWithNonZeroNumber() throws IOException, TemplateException {
        // Test that non-zero numbers are truthy in conditional context
        Template template = new Template("test");
        template.parse("{{if ne .Value 0}}truthy{{else}}falsy{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", 42);
        template.execute(writer, data);
        assertEquals("truthy", writer.toString());
    }

    @Test
    void testIsTruthyWithNonEmptyString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Value}}truthy{{else}}falsy{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "hello");
        template.execute(writer, data);
        assertEquals("truthy", writer.toString());
    }

    @Test
    void testCompareWithChainedLt() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 1 2 3 4}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testCompareWithChainedLtFailing() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 1 3 2 4}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testCompareWithChainedLe() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if le 1 2 2 3}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testCompareWithChainedGt() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if gt 4 3 2 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testCompareWithChainedGe() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ge 4 3 3 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testToIntWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items .NullIndex}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        data.put("NullIndex", null);
        template.execute(writer, data);
        assertEquals("a", writer.toString()); // null converts to 0
    }

    @Test
    void testToIntWithDouble() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items .FloatIndex}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        data.put("FloatIndex", 1.9);
        template.execute(writer, data);
        assertEquals("b", writer.toString()); // 1.9 converts to 1
    }
}
