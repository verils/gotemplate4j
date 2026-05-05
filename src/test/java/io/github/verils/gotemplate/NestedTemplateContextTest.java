package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for nested template execution and context passing.
 */
public class NestedTemplateContextTest {

    @Test
    void testNestedTemplateWithDotContext() throws IOException, TemplateException {
        // Test that dot context is properly passed to nested templates
        Template template = new Template("master");
        template.parse(
            "{{define \"inner\"}}Inner: {{.Name}}{{end}}" +
            "{{template \"inner\" .}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Outer");
        template.execute(writer, data);
        
        assertEquals("Inner: Outer", writer.toString());
    }

    @Test
    void testNestedTemplateWithDifferentContext() throws IOException, TemplateException {
        // Test passing a different context to nested template
        Template template = new Template("master");
        template.parse(
            "{{define \"inner\"}}Name: {{.Name}}{{end}}" +
            "{{template \"inner\" .User}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> user = new HashMap<>();
        user.put("Name", "Alice");
        
        Map<String, Object> data = new HashMap<>();
        data.put("User", user);
        
        template.execute(writer, data);
        
        assertEquals("Name: Alice", writer.toString());
    }

    @Test
    void testNestedTemplateVariableIsolation() throws IOException, TemplateException {
        // Test that variables in nested templates don't leak to parent
        Template template = new Template("master");
        template.parse(
            "{{define \"inner\"}}{{$x := \"inner\"}}Inner: {{$x}}{{end}}" +
            "{{$x := \"outer\"}}" +
            "{{template \"inner\" .}}" +
            " Outer: {{$x}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        template.execute(writer, data);
        
        // Variables should be isolated - inner template's $x shouldn't affect outer
        assertEquals("Inner: inner Outer: outer", writer.toString());
    }

    @Test
    void testNestedTemplateAccessingParentFields() throws IOException, TemplateException {
        // Test accessing fields through nested structure
        Template template = new Template("master");
        template.parse(
            "{{define \"user_detail\"}}User: {{.Name}}, Email: {{.Email}}{{end}}" +
            "{{template \"user_detail\" .CurrentUser}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> currentUser = new HashMap<>();
        currentUser.put("Name", "Bob");
        currentUser.put("Email", "bob@example.com");
        
        Map<String, Object> data = new HashMap<>();
        data.put("CurrentUser", currentUser);
        
        template.execute(writer, data);
        
        assertEquals("User: Bob, Email: bob@example.com", writer.toString());
    }

    @Test
    void testDeeplyNestedTemplates() throws IOException, TemplateException {
        // Test multiple levels of template nesting
        Template template = new Template("master");
        template.parse(
            "{{define \"level3\"}}Level3: {{.Value}}{{end}}" +
            "{{define \"level2\"}}Level2: {{template \"level3\" .}}{{end}}" +
            "{{define \"level1\"}}Level1: {{template \"level2\" .}}{{end}}" +
            "{{template \"level1\" .}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Value", "deep");
        
        template.execute(writer, data);
        
        assertEquals("Level1: Level2: Level3: deep", writer.toString());
    }

    @Test
    void testNestedTemplateWithNullContext() throws IOException, TemplateException {
        // Test nested template with null context
        Template template = new Template("master");
        template.parse(
            "{{define \"inner\"}}Value: {{if .}}{{.}}{{else}}null{{end}}{{end}}" +
            "{{template \"inner\" .OptionalValue}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("OptionalValue", null);
        
        template.execute(writer, data);
        
        assertEquals("Value: null", writer.toString());
    }

    @Test
    void testNestedTemplateWithPipelineArgument() throws IOException, TemplateException {
        // Test nested template with pipeline as argument
        Template template = new Template("master");
        template.parse(
            "{{define \"greet\"}}Hello, {{.}}!{{end}}" +
            "{{template \"greet\" (.Name | printf \"%s\")}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        template.execute(writer, data);
        
        assertEquals("Hello, World!", writer.toString());
    }

    @Test
    void testNestedTemplateInRange() throws IOException, TemplateException {
        // Test nested template invocation inside range loop
        Template template = new Template("master");
        template.parse(
            "{{define \"item\"}}[{{.}}]{{end}}" +
            "{{range .Items}}{{template \"item\" .}}{{end}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        
        template.execute(writer, data);
        
        assertEquals("[a][b][c]", writer.toString());
    }

    @Test
    void testNestedTemplateInIfBlock() throws IOException, TemplateException {
        // Test nested template invocation inside if block
        Template template = new Template("master");
        template.parse(
            "{{define \"yes\"}}YES{{end}}" +
            "{{define \"no\"}}NO{{end}}" +
            "{{if .Condition}}{{template \"yes\" .}}{{else}}{{template \"no\" .}}{{end}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Condition", true);
        
        template.execute(writer, data);
        
        assertEquals("YES", writer.toString());
    }

    @Test
    void testNestedTemplateWithContextChange() throws IOException, TemplateException {
        // Test with block changing context before nested template call
        Template template = new Template("master");
        template.parse(
            "{{define \"detail\"}}Detail: {{.Info}}{{end}}" +
            "{{with .Data}}{{template \"detail\" .}}{{end}}"
        );
        
        Writer writer = new StringWriter();
        Map<String, Object> dataObj = new HashMap<>();
        dataObj.put("Info", "some info");
        
        Map<String, Object> data = new HashMap<>();
        data.put("Data", dataObj);
        
        template.execute(writer, data);
        
        assertEquals("Detail: some info", writer.toString());
    }
}
