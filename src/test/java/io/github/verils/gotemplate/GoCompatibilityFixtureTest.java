package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoCompatibilityFixtureTest {

    @Test
    void controlFlowFixture() throws Exception {
        assertEquals("yes", TemplateTestSupport.render("{{if .OK}}yes{{else}}no{{end}}",
                TemplateTestSupport.data("OK", true)));
        assertEquals("empty", TemplateTestSupport.render("{{range .Items}}{{.}}{{else}}empty{{end}}",
                TemplateTestSupport.data("Items", Collections.emptyList())));
        assertEquals("inner", TemplateTestSupport.render("{{with .User}}{{.Name}}{{end}}",
                TemplateTestSupport.data("User", TemplateTestSupport.data("Name", "inner"))));
    }

    @Test
    void templateInvocationAndVariablesFixture() throws Exception {
        assertEquals("Hello Bob", TemplateTestSupport.render(
                "{{$name := .Name}}{{template \"greet\" $name}}{{define \"greet\"}}Hello {{.}}{{end}}",
                TemplateTestSupport.data("Name", "Bob")));
    }

    @Test
    void escapingAndFormattingFixture() throws Exception {
        assertEquals("&lt;x&gt; 7", TemplateTestSupport.render("{{html .Value}} {{printf \"%d\" .Count}}",
                TemplateTestSupport.data("Value", "<x>", "Count", 7)));
    }
}
