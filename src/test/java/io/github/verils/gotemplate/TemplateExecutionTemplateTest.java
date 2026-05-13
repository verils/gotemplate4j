package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for template definition and invocation.
 */
class TemplateExecutionTemplateTest {

    @Test
    void testExecuteMultipleTemplates() throws IOException, TemplateException {
        Template template = new Template("master");
        template.parse("{{define \"inner\"}}inner content{{end}}");
        template.parse("{{template \"inner\"}}");

        Writer writer = new StringWriter();
        template.execute(writer, null);
        assertEquals("inner content", writer.toString());
    }
}
