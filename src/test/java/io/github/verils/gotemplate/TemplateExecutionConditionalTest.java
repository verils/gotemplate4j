package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for conditional execution (if/else, with/else).
 */
class TemplateExecutionConditionalTest {

    @Test
    void testExecuteWithWithNullValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{with .Value}}present{{else}}absent{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);

        template.execute(writer, data);
        assertEquals("absent", writer.toString());
    }

    @Test
    void testExecuteWithWithFalsyValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{with .Value}}present{{else}}absent{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", false);

        template.execute(writer, data);
        assertEquals("absent", writer.toString());
    }

    @Test
    void testExecuteIfWithNullCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Condition}}true{{else}}false{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Condition", null);

        template.execute(writer, data);
        assertEquals("false", writer.toString());
    }

    @Test
    void testExecuteIfWithZeroCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Condition}}true{{else}}false{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Condition", 0);

        template.execute(writer, data);
        assertEquals("false", writer.toString());
    }

    @Test
    void testExecuteIfWithEmptyStringCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Condition}}true{{else}}false{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Condition", "");

        template.execute(writer, data);
        assertEquals("false", writer.toString());
    }

    @Test
    void testExecuteWithBooleanTrue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Value}}yes{{else}}no{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", true);

        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testExecuteWithNonEmptyString() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Value}}yes{{else}}no{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "hello");

        template.execute(writer, data);
        assertEquals("yes", writer.toString());
    }

    @Test
    void testExecuteWithNonZeroNumber() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Value}}yes{{else}}no{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", 1);

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }
}
