package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

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
        assertEquals("a", StringUtils.unquote("'a'"));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("'hello'"));
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
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("a"));
    }

    @Test
    void testUnquoteThrowsExceptionForMismatchedQuotes() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"hello'"));
    }

    @Test
    void testUnquoteThrowsExceptionForInvalidQuoteChar() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("#hello#"));
    }

    @Test
    void testUnquoteThrowsExceptionForNewlineInDoubleQuotes() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"hello\nworld\""));
    }

    @Test
    void testUnquoteThrowsExceptionForBacktickInsideBackticks() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("`hello`world`"));
    }

    @Test
    void unquoteDecodesSupportedEscapes() {
        assertEquals("\u0007\b\f\n\r\t\u000B\\\"'",
                StringUtils.unquote("\"\\a\\b\\f\\n\\r\\t\\v\\\\\\\"'\""));
        assertEquals("A", StringUtils.unquote("\"\\x41\""));
        assertEquals("A", StringUtils.unquote("\"\\101\""));
        assertEquals("中", StringUtils.unquote("\"\\u4E2D\""));
        assertEquals("😀", StringUtils.unquote("\"\\U0001F600\""));
        assertEquals("\u00FF", StringUtils.unquote("\"\\xFF\""));
    }

    @Test
    void unquoteRejectsInvalidEscapes() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\q\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\x1\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\400\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\0\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\uD800\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\U00110000\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\UFFFFFFFF\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("\"\\'\""));
        assertThrows(IllegalArgumentException.class, () -> StringUtils.unquote("'\\\"'"));
    }
}
