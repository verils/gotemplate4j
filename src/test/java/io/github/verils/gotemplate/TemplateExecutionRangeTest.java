package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for range loop execution.
 */
class TemplateExecutionRangeTest {

    @Test
    void testExecuteRangeWithEmptyCollection() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Items}}item{{else}}empty{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        List<String> emptyList = new ArrayList<>();
        data.put("Items", emptyList);

        template.execute(writer, data);
        assertNotNull(writer.toString());
    }

    @Test
    void testExecuteRangeWithNullCollection() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Items}}item{{else}}empty{{end}}");

        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", null);

        template.execute(writer, data);
        assertEquals("empty", writer.toString());
    }

    @Test
    void testPipelineFinalValueForIndex() throws IOException, TemplateException {
        assertEquals("b", render("{{.Index | index .Items}}",
                data("Items", new String[]{"a", "b"}, "Index", 1)));
    }

    @Test
    void testRangeBreakCoversArrayCollectionAndMapPaths() throws IOException, TemplateException {
        assertEquals("a", render("{{range .Items}}{{.}}{{break}}{{end}}",
                data("Items", new String[]{"a", "b"})));
        assertEquals("a", render("{{range .Items}}{{.}}{{break}}{{end}}",
                data("Items", Arrays.asList("a", "b"))));

        Map<String, Object> items = new LinkedHashMap<>();
        items.put("a", 1);
        items.put("b", 2);
        assertEquals("a=1", render("{{range $k, $v := .Items}}{{$k}}={{$v}}{{break}}{{end}}",
                data("Items", items)));
    }
}
