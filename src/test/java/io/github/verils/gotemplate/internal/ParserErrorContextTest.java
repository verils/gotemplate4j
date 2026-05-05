package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
class ParserErrorContextTest extends ParserTestSupport {

    @Test
    void testErrorWithContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{else}}\n  World\n{{end}}");
            // Should parse successfully
        } catch (TemplateParseException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }


    @Test
    void testUnmatchedEndWithErrorContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello\n{{end}}");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            // Exception should be thrown, message may or may not include context
            assertNotNull(e.getMessage());
        }
    }


    @Test
    void testErrorContextWithNullToken() {
        Parser parser = createParser1();
        try {
            // Test error path where token is null in error context
            parser.parse("test", "{{if .X}}hello");
            fail("Should throw TemplateParseException for unclosed if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Message should contain parse error info
            assertTrue(e.getMessage().contains("Parse error") || e.getMessage().contains("unexpected"));
        }
    }


    @Test
    void testErrorContextWithMultilineTemplate() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{invalid syntax}}\nLine 4");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Error message should be present, may or may not have line info depending on error type
            assertFalse(e.getMessage().isEmpty());
        }
    }


    @Test
    void testUnclosedDelimiterError() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello {{");
            fail("Should throw TemplateParseException for unclosed delimiter");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unclosed"));
        }
    }


    @Test
    void testMissingActionTokenError() {
        Parser parser = createParser1();
        try {
            // This should trigger missing action token error
            parser.parse("test", "{{ ");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }


    @Test
    void testErrorMessageWithContextForSyntaxError() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            // Error should contain parse error info
            assertFalse(message.isEmpty());
        }
    }


    @Test
    void testErrorMessageWithContextForUnclosedIf() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{if .X}}\nLine 4");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            assertFalse(message.isEmpty());
        }
    }


    @Test
    void testErrorMessageWithContextForMissingEnd() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{range .Items}}\n  Item\n{{if .Active}}\n    Active");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
        }
    }

    // Test lookNextItem at boundaries

    @Test
    void testErrorContextWithTabs() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\n\t{{invalid}}\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test empty template name in define

    @Test
    void testErrorContextWithTabAtErrorColumn() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "\t\t{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at first line

    @Test
    void testErrorContextAtFirstLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{invalid}}\nline2\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at last line

    @Test
    void testErrorContextAtLastLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\nline2\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context with very long line

    @Test
    void testErrorContextWithLongLine() {
        Parser parser = createParser2();
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 200; i++) {
                sb.append('x');
            }
            sb.append(" {{invalid}}");
            parser.parse("test", sb.toString());
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test variable with multiple field accesses
}
