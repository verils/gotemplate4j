package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for template inheritance patterns using block and define.
 * Template inheritance allows creating base templates with overridable sections.
 */
public class TemplateInheritanceTest {

    @Test
    void testBaseTemplateWithBlocks() throws IOException, TemplateException {
        // Base template defines structure with blocks
        String baseTemplate = 
            "<!DOCTYPE html>" +
            "<html>" +
            "  <head><title>{{block \"title\" .}}Default Title{{end}}</title></head>" +
            "  <body>" +
            "    {{block \"content\" .}}Default Content{{end}}" +
            "  </body>" +
            "</html>";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals(
            "<!DOCTYPE html>" +
            "<html>" +
            "  <head><title>Default Title</title></head>" +
            "  <body>" +
            "    Default Content" +
            "  </body>" +
            "</html>",
            writer.toString()
        );
    }

    @Test
    void testChildTemplateOverridesBlocks() throws IOException, TemplateException {
        // Base template
        String baseTemplate = 
            "<!DOCTYPE html>" +
            "<html>" +
            "  <head><title>{{block \"title\" .}}Default Title{{end}}</title></head>" +
            "  <body>{{block \"content\" .}}Default Content{{end}}</body>" +
            "</html>";
        
        // Child template overrides blocks
        String childTemplate = 
            "{{define \"title\"}}My Page{{end}}" +
            "{{define \"content\"}}<h1>Hello World</h1>{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals(
            "<!DOCTYPE html>" +
            "<html>" +
            "  <head><title>My Page</title></head>" +
            "  <body><h1>Hello World</h1></body>" +
            "</html>",
            writer.toString()
        );
    }

    @Test
    void testPartialOverride() throws IOException, TemplateException {
        // Base template with multiple blocks
        String baseTemplate = 
            "{{block \"header\" .}}Header{{end}}" +
            "{{block \"body\" .}}Body{{end}}" +
            "{{block \"footer\" .}}Footer{{end}}";
        
        // Child only overrides body
        String childTemplate = 
            "{{define \"body\"}}Custom Body{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("HeaderCustom BodyFooter", writer.toString());
    }

    @Test
    void testMultiLevelInheritance() throws IOException, TemplateException {
        // Grandparent template
        String grandparentTemplate = 
            "{{block \"content\" .}}Grandparent{{end}}";
        
        // Parent template overrides grandparent
        String parentTemplate = 
            "{{define \"content\"}}Parent{{end}}";
        
        // Child template overrides parent
        String childTemplate = 
            "{{define \"content\"}}Child{{end}}";
        
        Template template = new Template("base");
        template.parse(grandparentTemplate);
        template.parse(parentTemplate);
        template.parse(childTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        // Last definition wins
        assertEquals("Child", writer.toString());
    }

    @Test
    void testInheritanceWithData() throws IOException, TemplateException {
        // Base template with data context
        String baseTemplate = 
            "<html>" +
            "  <head><title>{{block \"title\" .}}{{.SiteName}}{{end}}</title></head>" +
            "  <body>{{block \"content\" .}}{{.Message}}{{end}}</body>" +
            "</html>";
        
        // Child template customizes content
        String childTemplate = 
            "{{define \"content\"}}Welcome, {{.UserName}}!{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Map<String, Object> data = new HashMap<>();
        data.put("SiteName", "My Site");
        data.put("Message", "Hello");
        data.put("UserName", "Alice");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals(
            "<html>" +
            "  <head><title>My Site</title></head>" +
            "  <body>Welcome, Alice!</body>" +
            "</html>",
            writer.toString()
        );
    }

    @Test
    void testNestedBlockInheritance() throws IOException, TemplateException {
        // Base template with nested blocks
        String baseTemplate = 
            "{{block \"outer\" .}}" +
            "  Outer Start" +
            "  {{block \"inner\" .}}Inner Default{{end}}" +
            "  Outer End" +
            "{{end}}";
        
        // Child overrides inner block
        String childTemplate = 
            "{{define \"inner\"}}Inner Custom{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals("  Outer Start  Inner Custom  Outer End", writer.toString());
    }

    @Test
    void testLayoutPattern() throws IOException, TemplateException {
        // Common layout pattern: base layout with content injection
        String layoutTemplate = 
            "{{block \"layout\" .}}" +
            "<div class=\"container\">" +
            "  <header>{{block \"header\" .}}Default Header{{end}}</header>" +
            "  <main>{{block \"main\" .}}Default Main{{end}}</main>" +
            "  <footer>{{block \"footer\" .}}Default Footer{{end}}</footer>" +
            "</div>" +
            "{{end}}";
        
        // Page template that uses the layout
        String pageTemplate = 
            "{{define \"header\"}}Page Header{{end}}" +
            "{{define \"main\"}}Page Content{{end}}" +
            "{{define \"footer\"}}Page Footer{{end}}";
        
        Template template = new Template("layout");
        template.parse(layoutTemplate);
        template.parse(pageTemplate);
        
        Writer writer = new StringWriter();
        template.execute(writer, null);
        
        assertEquals(
            "<div class=\"container\">" +
            "  <header>Page Header</header>" +
            "  <main>Page Content</main>" +
            "  <footer>Page Footer</footer>" +
            "</div>",
            writer.toString()
        );
    }

    @Test
    void testConditionalBlockOverride() throws IOException, TemplateException {
        // Base template with conditional blocks
        String baseTemplate = 
            "{{if .ShowHeader}}{{block \"header\" .}}Default Header{{end}}{{end}}" +
            "{{block \"content\" .}}Content{{end}}" +
            "{{if .ShowFooter}}{{block \"footer\" .}}Default Footer{{end}}{{end}}";
        
        // Child overrides content
        String childTemplate = 
            "{{define \"content\"}}Custom Content{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Map<String, Object> data = new HashMap<>();
        data.put("ShowHeader", true);
        data.put("ShowFooter", false);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Default HeaderCustom Content", writer.toString());
    }

    @Test
    void testBlockWithRangeInheritance() throws IOException, TemplateException {
        // Base template with range in block
        String baseTemplate = 
            "{{block \"list\" .}}" +
            "{{range .Items}}" +
            "  <li>{{.}}</li>" +
            "{{end}}" +
            "{{end}}";
        
        // Child overrides with different rendering
        String childTemplate = 
            "{{define \"list\"}}" +
            "{{range .Items}}" +
            "  <div>{{.}}</div>" +
            "{{end}}" +
            "{{end}}";
        
        Template template = new Template("base");
        template.parse(baseTemplate);
        template.parse(childTemplate);
        
        Map<String, Object> data = new HashMap<>();
        data.put("Items", new String[]{"a", "b", "c"});
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("  <div>a</div>  <div>b</div>  <div>c</div>", writer.toString());
    }
}
