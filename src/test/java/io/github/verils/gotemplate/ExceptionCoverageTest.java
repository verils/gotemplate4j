package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for exception classes to improve coverage
 */
class ExceptionCoverageTest {

    @Test
    void testTemplateNotFoundException() {
        TemplateNotFoundException exception = new TemplateNotFoundException("template not found");
        assertEquals("template not found", exception.getMessage());
    }

    @Test
    void testTemplateNotFoundExceptionWithCause() {
        Exception cause = new RuntimeException("cause");
        TemplateNotFoundException exception = new TemplateNotFoundException("template not found", cause);
        assertEquals("template not found", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testTemplateExecutionException() {
        TemplateExecutionException exception = new TemplateExecutionException("execution error");
        assertEquals("execution error", exception.getMessage());
    }

    @Test
    void testTemplateExecutionExceptionWithCause() {
        Exception cause = new RuntimeException("cause");
        TemplateExecutionException exception = new TemplateExecutionException("execution error", cause);
        assertEquals("execution error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testTemplateParseException() {
        TemplateParseException exception = new TemplateParseException("parse error");
        assertEquals("parse error", exception.getMessage());
    }

    @Test
    void testTemplateParseExceptionWithCause() {
        Exception cause = new RuntimeException("cause");
        TemplateParseException exception = new TemplateParseException("parse error", cause);
        assertEquals("parse error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testTemplateException() {
        TemplateException exception = new TemplateException("general error");
        assertEquals("general error", exception.getMessage());
    }

    @Test
    void testTemplateExceptionWithCause() {
        Exception cause = new RuntimeException("cause");
        TemplateException exception = new TemplateException("general error", cause);
        assertEquals("general error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionHierarchy() {
        // Verify that all specific exceptions are instances of TemplateException
        assertInstanceOf(TemplateException.class, new TemplateNotFoundException("test"));
        assertInstanceOf(TemplateException.class, new TemplateExecutionException("test"));
        assertInstanceOf(TemplateException.class, new TemplateParseException("test"));
    }
}
