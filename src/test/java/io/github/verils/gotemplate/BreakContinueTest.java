package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BreakContinueTest {

    @Test
    void breakStopsRangeIteration() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Items}}{{if eq . \"b\"}}{{break}}{{end}}{{.}}{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Items", new String[]{"a", "b", "c"}));

        assertEquals("a", writer.toString());
    }

    @Test
    void continueSkipsCurrentRangeIteration() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Items}}{{if eq . \"b\"}}{{continue}}{{end}}{{.}}{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Items", new String[]{"a", "b", "c"}));

        assertEquals("ac", writer.toString());
    }

    @Test
    void nestedRangeBreakOnlyExitsInnerRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Rows}}[{{range .}}{{if eq . \"stop\"}}{{break}}{{end}}{{.}}{{end}}]{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Rows", new String[][]{
                {"a", "stop", "x"},
                {"b", "c"}
        }));

        assertEquals("[a][bc]", writer.toString());
    }

    @Test
    void nestedRangeContinueOnlyContinuesInnerRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Rows}}[{{range .}}{{if eq . \"skip\"}}{{continue}}{{end}}{{.}}{{end}}]{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Rows", new String[][]{
                {"a", "skip", "b"},
                {"skip", "c"}
        }));

        assertEquals("[ab][c]", writer.toString());
    }

    @Test
    void breakOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{break}}"));
    }

    @Test
    void continueOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{continue}}"));
    }

    @Test
    void breakInsideIfOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{if .Show}}{{break}}{{end}}"));
    }

    @Test
    void continueInsideWithOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{with .Value}}{{continue}}{{end}}"));
    }

    @Test
    void breakInsideDefineOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{define \"inner\"}}{{break}}{{end}}"));
    }

    @Test
    void continueInsideBlockOutsideRangeIsParseError() {
        Template template = new Template("test");

        assertThrows(TemplateParseException.class, () -> template.parse("{{block \"inner\" .}}{{continue}}{{end}}"));
    }

    private Map<String, Object> data(String key, Object value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
