package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for enhanced block action with proper overriding semantics.
 */
public class BlockOverrideTest {

    @Test
    void testBlockWithDefaultContent() throws IOException, TemplateException {
        // Test basic block with default content
        Template template = new Template("master");
        template.parse("Before{{block \"content\" .}}Default Content{{end}}After");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("BeforeDefault ContentAfter", writer.toString());
    }

    @Test
    void testBlockOverriddenByDefine() throws IOException, TemplateException {
        // Test block being overridden by a subsequent define
        Template template = new Template("master");
        template.parse("Before{{block \"content\" .}}Default Content{{end}}After");
        
        // Override the block
        template.parse("{{define \"content\"}}Overridden Content{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("BeforeOverridden ContentAfter", writer.toString());
    }

    @Test
    void testBlockWithData() throws IOException, TemplateException {
        // Test block with data context
        Template template = new Template("master");
        template.parse("{{block \"item\" .}}Item: {{.name}}{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "TestItem");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Item: TestItem", writer.toString());
    }

    @Test
    void testBlockOverriddenWithData() throws IOException, TemplateException {
        // Test block override with data context
        Template template = new Template("master");
        template.parse("{{block \"item\" .}}Default: {{.name}}{{end}}");
        
        // Override with different content
        template.parse("{{define \"item\"}}Custom: {{.name}}{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "TestItem");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Custom: TestItem", writer.toString());
    }

    @Test
    void testMultipleBlocks() throws IOException, TemplateException {
        // Test multiple blocks in same template
        Template template = new Template("master");
        template.parse(
            "{{block \"header\" .}}Default Header{{end}}" +
            "{{block \"body\" .}}Default Body{{end}}" +
            "{{block \"footer\" .}}Default Footer{{end}}"
        );
        
        // Override only body
        template.parse("{{define \"body\"}}Custom Body{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("Default HeaderCustom BodyDefault Footer", writer.toString());
    }

    @Test
    void testNestedBlocks() throws IOException, TemplateException {
        // Test nested blocks
        Template template = new Template("master");
        template.parse("{{block \"outer\" .}}Outer {{block \"inner\" .}}Inner{{end}} End{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("Outer Inner End", writer.toString());
    }

    @Test
    void testBlockOverriddenNested() throws IOException, TemplateException {
        // Test nested blocks with override
        Template template = new Template("master");
        template.parse("{{block \"outer\" .}}Outer {{block \"inner\" .}}Inner{{end}} End{{end}}");
        
        // Override inner block
        template.parse("{{define \"inner\"}}Custom Inner{{end}}");
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("Outer Custom Inner End", writer.toString());
    }

    @Test
    void testBlockWithRange() throws IOException, TemplateException {
        // Test block with range
        Template template = new Template("master");
        template.parse("{{block \"list\" .}}{{range .items}}{{.}},{{end}}{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", new String[]{"a", "b", "c"});
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("a,b,c,", writer.toString());
    }

    @Test
    void testBlockOverriddenWithDifferentStructure() throws IOException, TemplateException {
        // Test block override with completely different structure
        Template template = new Template("master");
        template.parse("{{block \"content\" .}}<div>{{.text}}</div>{{end}}");
        
        // Override with different HTML structure
        template.parse("{{define \"content\"}}<section><p>{{.text}}</p></section>{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("text", "Hello");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("<section><p>Hello</p></section>", writer.toString());
    }
}
