package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for improved null-safety with default values.
 * This feature provides ways to handle null/missing values gracefully.
 */
public class NullSafetyTest {

    @Test
    void testDefaultFunctionWithNullValue() throws IOException, TemplateException {
        // Test using 'default' function to provide fallback for null values
        Template template = new Template("test");
        template.parse("Name: {{default .name \"Anonymous\"}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", null);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Name: Anonymous", writer.toString());
    }

    @Test
    void testDefaultFunctionWithNonNullValue() throws IOException, TemplateException {
        // Test that default doesn't override non-null values
        Template template = new Template("test");
        template.parse("Name: {{default .name \"Anonymous\"}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Name: Alice", writer.toString());
    }

    @Test
    void testDefaultFunctionWithEmptyString() throws IOException, TemplateException {
        // Test default with empty string (should use default)
        Template template = new Template("test");
        template.parse("Name: {{default .name \"Anonymous\"}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        // Empty string is falsy in Go templates, so should use default
        assertEquals("Name: Anonymous", writer.toString());
    }

    @Test
    void testDefaultFunctionWithZero() throws IOException, TemplateException {
        // Test default with zero (should use default since 0 is falsy)
        Template template = new Template("test");
        template.parse("Count: {{default .count 10}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("count", 0);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Count: 10", writer.toString());
    }

    @Test
    void testDefaultFunctionWithFalse() throws IOException, TemplateException {
        // Test default with false (should use default since false is falsy)
        Template template = new Template("test");
        template.parse("Flag: {{default .flag true}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("flag", false);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Flag: true", writer.toString());
    }

    @Test
    void testIfElseForNullHandling() throws IOException, TemplateException {
        // Test traditional if/else for null handling
        Template template = new Template("test");
        template.parse("{{if .name}}{{.name}}{{else}}No name provided{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", null);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("No name provided", writer.toString());
    }

    @Test
    void testNestedFieldNullSafety() throws IOException, TemplateException {
        // Test null safety with nested fields
        Template template = new Template("test");
        template.parse("Email: {{if .user.email}}{{.user.email}}{{else}}Not provided{{end}}");
        
        Map<String, Object> user = new HashMap<>();
        user.put("email", null);
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Email: Not provided", writer.toString());
    }

    @Test
    void testDefaultWithComplexType() throws IOException, TemplateException {
        // Test default with complex types
        Template template = new Template("test");
        template.parse("Items: {{default .items \"No items\"}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", null);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Items: No items", writer.toString());
    }

    @Test
    void testChainedDefaultValues() throws IOException, TemplateException {
        // Test chaining multiple defaults
        Template template = new Template("test");
        template.parse("Value: {{default .primary (default .secondary \"Fallback\")}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("primary", null);
        data.put("secondary", "Second choice");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Value: Second choice", writer.toString());
    }

    @Test
    void testDefaultInRange() throws IOException, TemplateException {
        // Test default value in range loop
        Template template = new Template("test");
        template.parse("{{range .items}}{{default . \"N/A\"}},{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", new String[]{"a", null, "c"});
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("a,N/A,c,", writer.toString());
    }

    @Test
    void testDefaultWithOptional() throws IOException, TemplateException {
        // Test default with Java Optional
        Template template = new Template("test");
        template.parse("Value: {{default .value \"default\"}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("value", java.util.Optional.empty());
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        // Empty optional unwraps to null, so should use default
        assertEquals("Value: default", writer.toString());
    }

    @Test
    void testMultipleDefaultStrategies() throws IOException, TemplateException {
        // Test combining different null-handling strategies
        Template template = new Template("test");
        template.parse(
            "{{if .name}}" +
            "  Name: {{.name}}" +
            "{{else}}" +
            "  Name: {{default .nickname \"Guest\"}}" +
            "{{end}}"
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", null);
        data.put("nickname", "Buddy");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("  Name: Buddy", writer.toString());
    }

    @Test
    void testDefaultWithNumericTypes() throws IOException, TemplateException {
        // Test default with different numeric types
        Template template = new Template("test");
        template.parse("Int: {{default .intVal 42}}, Long: {{default .longVal 100}}, Double: {{default .doubleVal 3.14}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("intVal", null);
        data.put("longVal", null);
        data.put("doubleVal", null);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Int: 42, Long: 100, Double: 3.14", writer.toString());
    }
}
