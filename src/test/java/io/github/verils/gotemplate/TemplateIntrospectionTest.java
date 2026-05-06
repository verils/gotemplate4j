package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateIntrospectionTest {

    @Test
    void rootOnlyTemplateIsVisibleAfterParse() throws Exception {
        Template template = new Template("root");
        template.parse("Hello");

        assertEquals("root", template.name());
        assertTrue(template.hasTemplate("root"));
        assertEquals(Collections.singletonList("root"), Arrays.asList(template.definedTemplates().toArray(new String[0])));
    }

    @Test
    void definedTemplatesReturnsStableNamesForDefinitions() throws Exception {
        Template template = new Template("root");
        template.parse("Root{{define \"header\"}}Header{{end}}{{define \"footer\"}}Footer{{end}}");

        Set<String> names = template.definedTemplates();

        assertTrue(names.contains("root"));
        assertTrue(names.contains("header"));
        assertTrue(names.contains("footer"));
        assertEquals(3, names.size());
        assertThrows(UnsupportedOperationException.class, () -> names.add("other"));
    }

    @Test
    void lookupReturnsExecutableIndependentTemplateCopy() throws Exception {
        Template template = new Template("root");
        template.parse("Root{{define \"child\"}}Child {{.Name}}{{end}}");

        Template child = template.lookup("child");

        assertNotNull(child);
        assertEquals("child", child.name());
        StringWriter writer = new StringWriter();
        child.execute(writer, TemplateTestSupport.data("Name", "A"));
        assertEquals("Child A", writer.toString());

        child.parse("{{define \"child\"}}Changed{{end}}");
        StringWriter originalWriter = new StringWriter();
        template.executeTemplate(originalWriter, "child", TemplateTestSupport.data("Name", "B"));
        assertEquals("Child B", originalWriter.toString());
    }

    @Test
    void lookupReturnsNullForUnknownTemplate() throws Exception {
        Template template = new Template("root");
        template.parse("Root");

        assertFalse(template.hasTemplate("missing"));
        assertNull(template.lookup("missing"));
    }

    @Test
    void templatesReturnsImmutableIndependentTemplateCopies() throws Exception {
        Template template = new Template("root");
        template.parse("Root{{define \"child\"}}Child{{end}}");

        List<Template> templates = template.templates();

        assertEquals(2, templates.size());
        assertThrows(UnsupportedOperationException.class, () -> templates.add(new Template("other")));
        assertEquals(Arrays.asList("root", "child"), Arrays.asList(
                templates.get(0).name(), templates.get(1).name()));
    }

    @Test
    void reparseOverrideIsVisibleInLookup() throws Exception {
        Template template = new Template("root");
        template.parse("{{define \"child\"}}First{{end}}");
        template.parse("{{define \"child\"}}Second{{end}}");

        StringWriter writer = new StringWriter();
        template.lookup("child").execute(writer, null);

        assertEquals("Second", writer.toString());
    }

    @Test
    void emptyDefinitionDoesNotOverrideExistingTemplate() throws Exception {
        Template template = new Template("root");
        template.parse("{{define \"child\"}}First{{end}}");
        template.parse("{{define \"child\"}}   {{end}}");

        StringWriter writer = new StringWriter();
        template.lookup("child").execute(writer, null);

        assertEquals("First", writer.toString());
    }

    @Test
    void copyConstructorPreservesIntrospectionState() throws Exception {
        Template template = new Template("root");
        template.parse("Root{{define \"child\"}}Child{{end}}");

        Template copy = new Template(template);

        assertEquals(template.name(), copy.name());
        assertEquals(template.definedTemplates(), copy.definedTemplates());
        assertTrue(copy.hasTemplate("child"));
    }
}
