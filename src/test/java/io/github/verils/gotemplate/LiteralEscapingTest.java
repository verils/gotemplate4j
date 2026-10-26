package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;

import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiteralEscapingTest {

    @Test
    void quotedStringsDecodeGoEscapes() throws IOException, TemplateException {
        assertEquals("a\"b", render("{{\"a\\\"b\"}}"));
        assertEquals("A", render("{{\"\\x41\"}}"));
        assertEquals("\u00FF", render("{{\"\\xFF\"}}"));
        assertEquals("A", render("{{\"\\101\"}}"));
        assertEquals("中", render("{{\"\\u4E2D\"}}"));
        assertEquals("😀", render("{{\"\\U0001F600\"}}"));
    }

    @Test
    void rawStringsAndCharacterConstantsPreserveTheirSemantics() throws IOException, TemplateException {
        assertEquals("a\\nb", render("{{`a\\nb`}}"));
        assertEquals("65", render("{{'A'}}"));
        assertEquals("10", render("{{'\\n'}}"));
        assertEquals("255", render("{{'\\xFF'}}"));
        assertEquals("128512", render("{{'\\U0001F600'}}"));
        assertEquals("128512", render("{{'😀'}}"));
    }

    @Test
    void escapedTemplateNamesAreDecoded() throws IOException, TemplateException {
        Template template = new Template("root");
        template.parse("{{define \"greet\\nname\"}}decoded{{end}}{{template \"greet\\nname\"}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("decoded", writer.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{{\"\\q\"}}",
            "{{\"\\x1\"}}",
            "{{\"\\400\"}}",
            "{{\"\\uD800\"}}",
            "{{\"\\U00110000\"}}",
            "{{'\\0'}}",
            "{{'ab'}}"
    })
    void invalidLiteralsReportSourceLocations(String source) {
        Template template = new Template("test");

        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> template.parse("\n  " + source));
        assertTrue(exception.getMessage().contains("invalid"));
        assertEquals(2, exception.getLine());
        assertTrue(exception.getColumn() > 0);
    }
}
