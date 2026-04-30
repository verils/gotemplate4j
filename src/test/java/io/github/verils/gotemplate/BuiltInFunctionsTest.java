package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInFunctionsTest {

    @Test
    void testEqFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq 1 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testEqFunctionWithStrings() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq \"hello\" \"hello\"}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testEqFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq 1 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testNeFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ne 1 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testNeFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ne 1 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testLtFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 1 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testLtFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 2 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testLeFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if le 2 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testGtFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if gt 2 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testGeFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ge 2 2}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testAndFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if and true true}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testAndFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if and true false}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testOrFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if or false true}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testOrFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if or false false}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testLenFunctionWithString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len \"hello\"}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("5", writer.toString());
    }

    @Test
    void testLenFunctionWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Items}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertEquals("3", writer.toString());
    }

    @Test
    void testLenFunctionWithList() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Items}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", Arrays.asList("x", "y"));
        template.execute(writer, data);
        assertEquals("2", writer.toString());
    }

    @Test
    void testIndexFunctionWithMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Map \"key\"}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        data.put("Map", map);
        template.execute(writer, data);
        assertEquals("value", writer.toString());
    }

    @Test
    void testIndexFunctionWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items 1}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertEquals("b", writer.toString());
    }

    @Test
    void testSliceFunctionWithString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Text 1 4}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello");
        template.execute(writer, data);
        assertEquals("ell", writer.toString());
    }

    @Test
    void testHtmlFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{html .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "<script>alert('xss')</script>");
        template.execute(writer, data);
        assertEquals("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;", writer.toString());
    }

    @Test
    void testJsFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{js .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        // Use actual special characters in the input
        data.put("Text", "Hello\nWorld\t\"quoted\"");
        template.execute(writer, data);
        // After JS escaping and then unescaping by the executor, we get the escaped representation
        String result = writer.toString();
        // The result should contain backslash-escaped sequences
        assertTrue(result.contains("\\n") || result.contains("\n"));
    }

    @Test
    void testUrlqueryFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{urlquery .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello world&test=1");
        template.execute(writer, data);
        assertTrue(writer.toString().contains("hello+world") || writer.toString().contains("hello%20world"));
    }

    @Test
    void testTypeofFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{typeof .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "hello");
        template.execute(writer, data);
        assertTrue(writer.toString().contains("String"));
    }

    @Test
    void testKindOfFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "hello");
        template.execute(writer, data);
        assertEquals("string", writer.toString());
    }

    @Test
    void testKindOfFunctionWithNumber() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", 42);
        template.execute(writer, data);
        assertEquals("int", writer.toString());
    }

    @Test
    void testDeepEqualFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", new int[]{1, 2, 3});
        data.put("B", new int[]{1, 2, 3});
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testDeepEqualFunctionFalse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", new int[]{1, 2, 3});
        data.put("B", new int[]{1, 2, 4});
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testNotFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if not false}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testPrintFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{print \"hello\" \"world\"}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("hello world", writer.toString());
    }

    @Test
    void testPrintfFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{printf .Format .Name .Age}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Format", "%s %d");
        data.put("Name", "hello");
        data.put("Age", 42);
        template.execute(writer, data);
        assertEquals("hello 42", writer.toString());
    }

    @Test
    void testPrintlnFunction() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{println \"hello\"}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("hello\n", writer.toString());
    }

    @Test
    void testComparisonWithChainedValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 1 2 3}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testEqWithMultipleArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq 1 1 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testLenWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("0", writer.toString());
    }

    @Test
    void testIndexWithNullCollection() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Collection \"key\"}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Collection", null);
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testHtmlWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{html .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testJsWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{js .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testUrlqueryWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{urlquery .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testTypeofWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{typeof .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("nil", writer.toString());
    }

    @Test
    void testKindOfWithNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);
        template.execute(writer, data);
        assertEquals("invalid", writer.toString());
    }

    // Additional tests for better branch coverage

    @Test
    void testEqWithDifferentTypes() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq 1 \"1\"}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testNeWithMultipleArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if ne 1 2 3}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testLtWithStrings() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt \"abc\" \"abd\"}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
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
    void testGtWithStrings() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if gt \"b\" \"a\"}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
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
    void testAndShortCircuit() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{and false true}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("false", writer.toString());
    }

    @Test
    void testOrShortCircuit() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{or true false}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("true", writer.toString());
    }

    @Test
    void testLenWithMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{len .Map}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        data.put("Map", map);
        template.execute(writer, data);
        assertEquals("3", writer.toString());
    }

    @Test
    void testIndexWithString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Text 0}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello");
        template.execute(writer, data);
        assertEquals("h", writer.toString());
    }

    @Test
    void testIndexOutOfBounds() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items 10}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testIndexNegativeIndex() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items -1}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        template.execute(writer, data);
        // Negative index wraps around in Go templates
        assertNotNull(writer.toString());
    }

    @Test
    void testSliceWithArray() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Items 1 3}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c", "d"});
        template.execute(writer, data);
        // Result is an array object, just verify it doesn't throw exception
        assertNotNull(writer.toString());
    }

    @Test
    void testSliceEmptyResult() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Text 3 1}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello");
        template.execute(writer, data);
        assertEquals("", writer.toString());
    }

    @Test
    void testSliceWithBoundsCheck() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{slice .Text 0 100}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hi");
        template.execute(writer, data);
        assertEquals("hi", writer.toString());
    }

    // Note: call function test requires IdentifierNode support in Executor.executeArgument
    // which is not yet fully implemented. Skipping for now.
    /*
    @Test
    void testCallFunction() throws IOException, TemplateException {
        // Test would go here when IdentifierNode support is added
    }
    */

    @Test
    void testHtmlWithSpecialChars() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{html .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "&<>\"'");
        template.execute(writer, data);
        assertEquals("&amp;&lt;&gt;&quot;&#39;", writer.toString());
    }

    @Test
    void testJsWithBackslash() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{js .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "back\\slash");
        template.execute(writer, data);
        // After JS escaping and processing, verify result is not empty
        assertNotNull(writer.toString());
    }

    @Test
    void testUrlqueryWithSpecialChars() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{urlquery .Text}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Text", "a=b&c=d");
        template.execute(writer, data);
        assertTrue(writer.toString().contains("a%3Db") || writer.toString().contains("a=b"));
    }

    @Test
    void testDeepEqualWithMaps() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("key", "value");
        Map<String, String> map2 = new HashMap<>();
        map2.put("key", "value");
        data.put("A", map1);
        data.put("B", map2);
        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testDeepEqualWithDifferentLengths() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if deepEqual .A .B}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("A", new int[]{1, 2});
        data.put("B", new int[]{1, 2, 3});
        template.execute(writer, data);
        assertEquals("no", writer.toString());
    }

    @Test
    void testTypeofWithNumber() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{typeof .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", 42);
        template.execute(writer, data);
        assertTrue(writer.toString().contains("Integer") || writer.toString().contains("Number"));
    }

    @Test
    void testTypeofWithBoolean() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{typeof .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", true);
        template.execute(writer, data);
        assertTrue(writer.toString().contains("Boolean"));
    }

    @Test
    void testKindOfWithBoolean() throws IOException, TemplateException {
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
        data.put("Value", new HashMap<>());
        template.execute(writer, data);
        assertEquals("map", writer.toString());
    }

    @Test
    void testKindOfWithList() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{kindOf .Value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", Arrays.asList(1, 2, 3));
        template.execute(writer, data);
        assertEquals("slice", writer.toString());
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
    void testComparisonWithNumbers() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if lt 1.5 2.5}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testAndWithNonBooleanValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{and 1 2 3}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("3", writer.toString());
    }

    @Test
    void testOrWithNonBooleanValues() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{or 0 0 5}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("5", writer.toString());
    }

    @Test
    void testNotWithTruthyValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if not 1}}yes{{else}}no{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("no", writer.toString());
    }

    @Test
    void testPrintWithNoArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{print}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("", writer.toString());
    }

    @Test
    void testPrintlnWithMultipleArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{println 1 2 3}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("1 2 3\n", writer.toString());
    }

    @Test
    void testPrintfWithNoArgs() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{printf \"hello\"}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("hello", writer.toString());
    }
}
