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
 * Tests for field access and navigation.
 */
class TemplateExecutionFieldAccessTest {

    @Test
    void testExecuteWithComplexNestedStructure() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.A.B.C.D}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> levelA = new HashMap<>();
        Map<String, Object> levelB = new HashMap<>();
        Map<String, Object> levelC = new HashMap<>();
        levelC.put("D", "deep value");
        levelB.put("C", levelC);
        levelA.put("B", levelB);
        data.put("A", levelA);

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }

    @Test
    void testExecuteWithMissingField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.NonExistent}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "test");

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }

    @Test
    void testExecuteWithArrayIndexOutOfBounds() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items 100}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});

        template.execute(writer, data);
        assertEquals("<no value>", writer.toString());
    }

    @Test
    void testExecuteWithNegativeArrayIndex() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Items -1}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }

    @Test
    void testExecuteWithDotNotationOnNull() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.Value.Field}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", null);

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }
}
