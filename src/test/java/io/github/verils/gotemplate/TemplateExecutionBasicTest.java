package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for basic template execution scenarios.
 */
class TemplateExecutionBasicTest {

    @Test
    void testExecuteWithNullWriter() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.Name}}");
        Writer writer = null;
        assertThrows(NullPointerException.class, () -> {
            template.execute(writer, null);
        });
    }

    @Test
    void testExecuteWithUnparsedTemplate() {
        Template template = new Template("test");
        assertThrows(Exception.class, () -> {
            Writer writer = new StringWriter();
            template.execute(writer, null);
        });
    }
}
