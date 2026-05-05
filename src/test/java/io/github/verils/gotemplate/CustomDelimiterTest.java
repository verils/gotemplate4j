package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for custom delimiter support in templates.
 */
public class CustomDelimiterTest {

    @Test
    void testDefaultDelimiters() throws IOException, TemplateException {
        // Test that default delimiters still work
        Template template = new Template("test");
        template.parse("Hello {{.Name}}!");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        template.execute(writer, data);
        
        assertEquals("Hello World!", writer.toString());
    }

    @Test
    void testCustomDelimiters() throws IOException, TemplateException {
        // Test with custom delimiters <% %>
        Template template = new Template("test", "<%", "%>");
        template.parse("Hello <% .Name %>!");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        template.execute(writer, data);
        
        assertEquals("Hello World!", writer.toString());
    }

    @Test
    void testCustomDelimitersWithIf() throws IOException, TemplateException {
        // Test conditional with custom delimiters
        Template template = new Template("test", "[[", "]]");
        template.parse("[[if .Show]]Visible[[end]]");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Show", true);
        template.execute(writer, data);
        
        assertEquals("Visible", writer.toString());
    }

    @Test
    void testCustomDelimitersWithRange() throws IOException, TemplateException {
        // Test range loop with custom delimiters
        Template template = new Template("test", "{{{", "}}}");
        template.parse("{{{range .Items}}}- {{{.}}}\n{{{end}}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"apple", "banana", "cherry"});
        template.execute(writer, data);
        
        assertEquals("- apple\n- banana\n- cherry\n", writer.toString());
    }

    @Test
    void testCustomDelimitersWithFunction() throws IOException, TemplateException {
        // Test function call with custom delimiters
        Template template = new Template("test", "<%", "%>");
        template.parse("<%printf \"%s %s\" .First .Last%>");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("First", "John");
        data.put("Last", "Doe");
        template.execute(writer, data);
        
        assertEquals("John Doe", writer.toString());
    }

    @Test
    void testCustomDelimitersWithTemplate() throws IOException, TemplateException {
        // Test template definition and invocation with custom delimiters
        Template template = new Template("test", "[[", "]]");
        template.parse("[[define \"greeting\"]]Hello [[.Name]][[end]][[template \"greeting\" .]]");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Alice");
        template.execute(writer, data);
        
        assertEquals("Hello Alice", writer.toString());
    }

    @Test
    void testCustomCommentDelimiters() throws IOException, TemplateException {
        // Test custom comment delimiters (comments still use /* */ but inside custom action delimiters)
        Template template = new Template("test", "[[", "]]");
        template.parse("Hello[[/* This is a comment */]] World");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        template.execute(writer, data);
        
        assertEquals("Hello World", writer.toString());
    }

    @Test
    void testCustomDelimitersWithNullValues() throws IOException, TemplateException {
        // Test that null delimiters fall back to defaults
        Template template = new Template("test", null, null);
        template.parse("Hello {{.Name}}!");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        template.execute(writer, data);
        
        assertEquals("Hello World!", writer.toString());
    }

    @Test
    void testCustomDelimitersWithWhitespaceControl() throws IOException, TemplateException {
        // Test trim markers with custom delimiters
        Template template = new Template("test", "{{", "}}");
        template.parse("Hello  {{- .Name -}}  !");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        template.execute(writer, data);
        
        assertEquals("HelloWorld!", writer.toString());
    }

    @Test
    void testCustomDelimitersWithComplexTemplate() throws IOException, TemplateException {
        // Test a complex template with multiple features using simple strings
        Template template = new Template("test", "[[", "]]");
        String tmpl = "[[if .Items]]" +
                "Items:\n" +
                "[[range .Items]]" +
                "- [[.]]\n" +
                "[[end]]" +
                "[[else]]" +
                "No items found." +
                "[[end]]";
        template.parse(tmpl);
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"apple", "banana", "cherry"});
        
        template.execute(writer, data);
        
        assertEquals("Items:\n- apple\n- banana\n- cherry\n", writer.toString());
    }
}
