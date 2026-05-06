package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused v0.5.0 audit tests for high-risk Go text/template compatibility semantics.
 */
class GoCompatibilityAuditTest {

    @Test
    void rangeElseExecutesForEmptyListArrayMapAndNull() throws Exception {
        assertEquals("empty", TemplateTestSupport.render("{{range .}}value{{else}}empty{{end}}", Collections.emptyList()));
        assertEquals("empty", TemplateTestSupport.render("{{range .}}value{{else}}empty{{end}}", new String[0]));
        assertEquals("empty", TemplateTestSupport.render("{{range .}}value{{else}}empty{{end}}", Collections.emptyMap()));
        assertEquals("empty", TemplateTestSupport.render("{{range .Missing}}value{{else}}empty{{end}}", TemplateTestSupport.data()));
    }

    @Test
    void rangeElseKeepsOuterDot() throws Exception {
        Map<String, Object> data = TemplateTestSupport.data("Items", Collections.emptyList(), "Name", "root");

        assertEquals("root", TemplateTestSupport.render("{{range .Items}}{{.}}{{else}}{{.Name}}{{end}}", data));
    }

    @Test
    void rangeVariablesDoNotLeakOutsideRangeBlock() {
        Template template = new Template("test");
        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> template.parse("{{range $item := .}}{{$item}}{{end}}|{{$item}}"));
        assertTrue(exception.getMessage().contains("undefined variable $item"));
    }

    @Test
    void ifPipelineVariablesDoNotLeakAfterEnd() {
        Template template = new Template("test");
        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> template.parse("{{if $x := .Value}}{{$x}}{{end}}|{{$x}}"));
        assertTrue(exception.getMessage().contains("undefined variable $x"));
    }

    @Test
    void variableReassignmentUpdatesExistingVariableInScope() throws Exception {
        assertEquals("new", TemplateTestSupport.render("{{$x := .Old}}{{$x = .New}}{{$x}}",
                TemplateTestSupport.data("Old", "old", "New", "new")));
    }

    @Test
    void withElseWithUsesExpectedContext() throws Exception {
        Map<String, Object> data = TemplateTestSupport.data(
                "Primary", null,
                "Fallback", TemplateTestSupport.data("Name", "fallback"));

        assertEquals("fallback", TemplateTestSupport.render(
                "{{with .Primary}}{{.Name}}{{else with .Fallback}}{{.Name}}{{else}}none{{end}}", data));
    }

    @Test
    void templateActionWithoutPipelineExecutesWithNullDot() throws Exception {
        Template template = new Template("root");
        template.parse("before {{template \"child\"}} after{{define \"child\"}}child{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, TemplateTestSupport.data("Name", "root"));

        assertEquals("before child after", writer.toString());
    }

    @Test
    void nestedTemplateDefinitionOverrideOrderIsStable() throws Exception {
        Template template = new Template("root");
        template.parse("{{define \"item\"}}first{{end}}{{template \"item\"}}");
        template.parse("{{define \"item\"}}second{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("second", writer.toString());
    }

    @Test
    void logicalAndShortCircuitsBeforeLaterArguments() throws Exception {
        assertEquals("false", TemplateTestSupport.render("{{and false .Missing}}", TemplateTestSupport.data()));
    }

    @Test
    void logicalOrShortCircuitsBeforeLaterArguments() throws Exception {
        assertEquals("true", TemplateTestSupport.render("{{or true .Missing}}", TemplateTestSupport.data()));
    }

    @Test
    void publicNoArgMethodsAreAvailableInFieldChains() throws Exception {
        assertEquals("value", TemplateTestSupport.render("{{.Name.toLowerCase}}",
                TemplateTestSupport.data("Name", "VALUE")));
    }

    @Test
    void functionRuntimeErrorsAreWrappedAsTemplateExecutionException() throws Exception {
        Map<String, Function> functions = new HashMap<>();
        functions.put("boom", args -> {
            throw new IllegalArgumentException("boom");
        });
        Template template = new Template("test", functions);
        template.parse("before {{boom}} after");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class, () -> {
            StringWriter writer = new StringWriter();
            template.execute(writer, null);
        });

        assertTrue(exception.getMessage().contains("function 'boom' failed"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void writerErrorsPropagateAsIOException() throws Exception {
        Template template = new Template("test");
        template.parse("before {{.Value}} after");

        IOException expected = new IOException("writer failed");
        Writer writer = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw expected;
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        IOException actual = assertThrows(IOException.class,
                () -> template.execute(writer, TemplateTestSupport.data("Value", "x")));
        assertSame(expected, actual);
    }

    @Test
    void mapIterationFollowsJavaMapOrder() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("b", 2);
        data.put("a", 1);

        assertEquals("b=2,a=1,", TemplateTestSupport.render("{{range $k, $v := .}}{{$k}}={{$v}},{{end}}", data));
    }
}
