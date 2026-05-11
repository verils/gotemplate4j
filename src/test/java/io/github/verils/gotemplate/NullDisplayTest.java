package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for custom null value display (Go compatibility feature).
 */
public class NullDisplayTest {

    @Test
    public void testDefaultNullDisplay() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.MissingField}}");

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        // Default Go behavior: "<no value>"
        assertEquals("Value: <no value>", writer.toString());
    }

    @Test
    public void testCustomNullDisplay() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.MissingField}}");
        template.withNullDisplay("(null)");

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Value: (null)", writer.toString());
    }

    @Test
    public void testCustomNullDisplayEmptyString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.MissingField}}");
        template.withNullDisplay("");

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Value: ", writer.toString());
    }

    @Test
    public void testCustomNullDisplayWithOption() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.MissingField}}");
        template.option("nulldisplay=N/A");

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Value: N/A", writer.toString());
    }

    @Test
    public void testNullDisplayWithNonNullValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.Name}}");
        template.withNullDisplay("(null)");

        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Alice");
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        // Non-null values should not be affected
        assertEquals("Value: Alice", writer.toString());
    }

    @Test
    public void testNullDisplayInNestedTemplate() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse(
            "{{define \"inner\"}}Inner: {{.Missing}}{{end}}" +
            "{{template \"inner\" .}}"
        );
        template.withNullDisplay("[empty]");

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Inner: [empty]", writer.toString());
    }

    @Test
    public void testNullDisplayGetter() {
        Template template1 = new Template("test");
        assertEquals(null, template1.nullDisplay());

        Template template2 = new Template("test");
        template2.withNullDisplay("custom");
        assertEquals("custom", template2.nullDisplay());
    }

    @Test
    public void testNullDisplayResetToDefault() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.MissingField}}");
        template.withNullDisplay("custom");
        template.withNullDisplay(null); // Reset to default

        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Value: <no value>", writer.toString());
    }

    @Test
    public void testNullDisplayWithClone() throws IOException, TemplateException {
        Template original = new Template("test");
        original.parse("Value: {{.MissingField}}");
        original.withNullDisplay("CUSTOM");

        Template cloned = new Template(original);
        
        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        cloned.execute(writer, data);

        // Cloned template should preserve null display setting
        assertEquals("Value: CUSTOM", writer.toString());
    }
}
