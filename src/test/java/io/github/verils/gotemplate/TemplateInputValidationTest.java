package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Template constructor input validation.
 */
public class TemplateInputValidationTest {

    // Test null template name
    
    @Test
    void testNullTemplateName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template((String) null));
        
        assertTrue(exception.getMessage().contains("Template name cannot be null or empty"));
    }

    // Test empty template name
    
    @Test
    void testEmptyTemplateName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template(""));
        
        assertTrue(exception.getMessage().contains("Template name cannot be null or empty"));
    }

    // Test valid template name
    
    @Test
    void testValidTemplateName() {
        Template template = new Template("test");
        assertNotNull(template);
        assertEquals("test", template.name());
    }

    // Test null left delimiter (should use default)
    
    @Test
    void testNullLeftDelimiter() {
        Template template = new Template("test", null, "}}");
        assertNotNull(template);
    }

    // Test null right delimiter (should use default)
    
    @Test
    void testNullRightDelimiter() {
        Template template = new Template("test", "{{", null);
        assertNotNull(template);
    }

    // Test empty left delimiter
    
    @Test
    void testEmptyLeftDelimiter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template("test", "", "}}"));
        
        assertTrue(exception.getMessage().contains("Left delimiter cannot be empty"));
    }

    // Test empty right delimiter
    
    @Test
    void testEmptyRightDelimiter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template("test", "{{", ""));
        
        assertTrue(exception.getMessage().contains("Right delimiter cannot be empty"));
    }

    // Test empty left comment delimiter
    
    @Test
    void testEmptyLeftCommentDelimiter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template("test", "{{", "}}", "", "*/"));
        
        assertTrue(exception.getMessage().contains("Left comment delimiter cannot be empty"));
    }

    // Test empty right comment delimiter
    
    @Test
    void testEmptyRightCommentDelimiter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Template("test", "{{", "}}", "/*", ""));
        
        assertTrue(exception.getMessage().contains("Right comment delimiter cannot be empty"));
    }

    // Test null comment delimiters (should use defaults)
    
    @Test
    void testNullCommentDelimiters() {
        Template template = new Template("test", "{{", "}}", null, null);
        assertNotNull(template);
    }

    // Test all parameters with functions map
    
    @Test
    void testAllParametersWithFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("custom", args -> "test");
        
        Template template = new Template("test", functions, "{{", "}}", "/*", "*/");
        assertNotNull(template);
        assertEquals("test", template.name());
    }

    // Test null functions map (should use empty map)
    
    @Test
    void testNullFunctionsMap() {
        Template template = new Template("test", null);
        assertNotNull(template);
    }

    // Test custom delimiters work correctly
    
    @Test
    void testCustomDelimitersWork() throws Exception {
        Template template = new Template("test", "[[", "]]");
        template.parse("[[.Name]]");
        
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        java.io.StringWriter writer = new java.io.StringWriter();
        template.execute(writer, data);
        
        assertEquals("World", writer.toString());
    }

    // Test custom comment delimiters work correctly
    
    @Test
    void testCustomCommentDelimitersWork() throws Exception {
        Template template = new Template("test", "{{", "}}", "<!--", "-->");
        template.parse("Hello{{<!-- comment -->}} World");
        
        java.io.StringWriter writer = new java.io.StringWriter();
        template.execute(writer, null);
        
        assertEquals("Hello World", writer.toString());
    }

    @Test
    void customCommentMarkerInsideStringRemainsLiteral() throws Exception {
        Template template = new Template("test", "{{", "}}", "<!--", "-->");
        template.parse("{{printf \"<!--\"}}");

        java.io.StringWriter writer = new java.io.StringWriter();
        template.execute(writer, null);

        assertEquals("<!--", writer.toString());
    }

    @Test
    void customCommentDelimitersPreserveTrimMarkers() throws Exception {
        Template template = new Template("test", "[[", "]]", "<!--", "-->");
        template.parse("Hello   [[- <!-- comment --> -]]   World");

        java.io.StringWriter writer = new java.io.StringWriter();
        template.execute(writer, null);

        assertEquals("HelloWorld", writer.toString());
    }

    // Test validation in copy constructor doesn't break
    
    @Test
    void testCopyConstructorPreservesValidation() throws Exception {
        Template original = new Template("original");
        original.parse("test");
        
        Template copy = new Template(original);
        assertNotNull(copy);
        assertEquals("original", copy.name());
    }

    @Test
    void parsingDoesNotCloseCallerOwnedInputStream() throws Exception {
        TrackingInputStream input = new TrackingInputStream(
                new ByteArrayInputStream("stream".getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        new Template("test").parse(input);

        assertFalse(input.closed);
    }

    @Test
    void parsingDoesNotCloseCallerOwnedReader() throws Exception {
        TrackingReader reader = new TrackingReader("reader");

        new Template("test").parse(reader);

        assertFalse(reader.closed);
    }

    private static final class TrackingInputStream extends FilterInputStream {
        private boolean closed;

        private TrackingInputStream(InputStream input) {
            super(input);
        }

        @Override
        public void close() throws java.io.IOException {
            closed = true;
            super.close();
        }
    }

    private static final class TrackingReader extends StringReader {
        private boolean closed;

        private TrackingReader(String text) {
            super(text);
        }

        @Override
        public void close() {
            closed = true;
            super.close();
        }
    }
}
