package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StringUtils to improve coverage
 */
class StringUtilsCoverageTest {

    @Test
    void testQuote() {
        assertEquals("\"hello\"", StringUtils.quote("hello"));
    }

    @Test
    void testQuoteWithEmptyString() {
        assertEquals("\"\"", StringUtils.quote(""));
    }

    @Test
    void testQuoteWithSpecialChars() {
        assertEquals("\"hello world\"", StringUtils.quote("hello world"));
    }

    @Test
    void testUnquoteWithDoubleQuotes() {
        assertEquals("hello", StringUtils.unquote("\"hello\""));
    }

    @Test
    void testUnquoteWithSingleQuotes() {
        assertEquals("hello", StringUtils.unquote("'hello'"));
    }

    @Test
    void testUnquoteWithBackticks() {
        assertEquals("hello", StringUtils.unquote("`hello`"));
    }

    @Test
    void testUnquoteWithEmptyString() {
        assertEquals("", StringUtils.unquote("\"\""));
    }

    @Test
    void testUnquoteWithBackticksContainingCR() {
        assertEquals("helloworld", StringUtils.unquote("`hello\rworld`"));
    }

    @Test
    void testUnquoteThrowsExceptionForTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.unquote("a");
        });
    }

    @Test
    void testUnquoteThrowsExceptionForMismatchedQuotes() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.unquote("\"hello'");
        });
    }

    @Test
    void testUnquoteThrowsExceptionForInvalidQuoteChar() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.unquote("#hello#");
        });
    }

    @Test
    void testUnquoteThrowsExceptionForNewlineInDoubleQuotes() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.unquote("\"hello\nworld\"");
        });
    }

    @Test
    void testUnquoteThrowsExceptionForBacktickInsideBackticks() {
        assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.unquote("`hello`world`");
        });
    }
}
