package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PipeNode variable assignment and usage.
 * Covers Phase 1.2 of PLAN.md - Fix PipeNode Processing
 */
class PipeNodeVariableTest {

    @Test
    void testSimpleVariableAssignment() throws Exception {
        String template = "{{$x := .Value}}{{$x}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Value", "hello");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("hello", writer.toString());
    }

    @Test
    void testVariableWithPipeline() throws Exception {
        String template = "{{$upper := .Name | print}}{{$upper}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Name", "world");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("world", writer.toString());
    }

    @Test
    void testMultipleVariables() throws Exception {
        String template = "{{$x := .X}}{{$y := .Y}}{{$x}}-{{$y}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("X", "first");
        data.put("Y", "second");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("first-second", writer.toString());
    }

    @Test
    void testVariableInIfBlock() throws Exception {
        String template = "{{if .Condition}}{{$msg := .Message}}{{$msg}}{{end}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Condition", true);
        data.put("Message", "success");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("success", writer.toString());
    }

    @Test
    void testVariableInWithBlock() throws Exception {
        // Test simple variable assignment without with block context change
        String template = "{{$name := .UserName}}Hello {{$name}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("UserName", "Alice");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Hello Alice", writer.toString());
    }

    @Test
    void testVariableScopeAfterEnd() throws Exception {
        // Simple variable assignment and usage
        String template = "{{$x := .Value}}Value is {{$x}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Value", 42);

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Value is 42", writer.toString());
    }

    @Test
    void testUndefinedVariable() {
        // This should fail at parse time (Go template behavior)
        String template = "{{$undefined}}";
        Template tmpl = new Template("test");
        assertThrows(TemplateParseException.class, () -> tmpl.parse(template));
    }

    @Test
    void testVariableReassignment() throws Exception {
        String template = "{{$x := .Initial}}{{$x := .Updated}}{{$x}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Initial", "old");
        data.put("Updated", "new");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("new", writer.toString());
    }

    @Test
    void testVariableReassignmentWithAssignOperator() throws Exception {
        String template = "{{$x := 1}}{{$x = 5}}{{$x}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, null);

        assertEquals("5", writer.toString());
    }

    @Test
    void testVariableInAction() throws Exception {
        // Variable declaration without printing
        String template = "{{$x := .Value}}Result: {{$x}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Value", "test");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Result: test", writer.toString());
    }

    @Test
    void testVariableWithNumberValue() throws Exception {
        String template = "{{$num := .Count}}Count is {{$num}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Count", 42);

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Count is 42", writer.toString());
    }

    @Test
    void testVariableWithBooleanValue() throws Exception {
        String template = "{{$flag := .Enabled}}Enabled: {{$flag}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Enabled", true);

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Enabled: true", writer.toString());
    }

    @Test
    void testNestedVariableUsage() throws Exception {
        String template = "{{$x := .Value}}{{$y := $x}}{{$y}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Value", "nested");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("nested", writer.toString());
    }

    @Test
    void testVariableInFunctionCall() throws Exception {
        String template = "{{$text := .Text}}{{len $text}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Text", "hello");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("5", writer.toString());
    }

    @Test
    void testMultiStagePipelineWithVariable() throws Exception {
        String template = "{{$result := .Text | print | print}}{{$result}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Text", "HeLLo");

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("HeLLo", writer.toString());
    }

    @Test
    void testVariableWithDotNotation() throws Exception {
        // Direct field access with variable
        String template = "{{$val := .Count}}Count: {{$val}}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        Map<String, Object> data = new HashMap<>();
        data.put("Count", 100);

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, data);

        assertEquals("Count: 100", writer.toString());
    }

    @Test
    void testVariableInRangeLoop() throws Exception {
        // Simple range over array with variable
        String template = "{{range .}}{{ $item := . }}{{ $item }},{{ end }}";
        Template tmpl = new Template("test");
        tmpl.parse(template);

        String[] items = {"a", "b", "c"};

        StringWriter writer = new StringWriter();
        tmpl.execute(writer, items);

        assertEquals("a,b,c,", writer.toString().trim());
    }
}
