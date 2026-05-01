package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CharUtils to improve coverage
 */
class CharUtilsCoverageTest {

    @Test
    void testIsSpace() {
        assertTrue(CharUtils.isSpace(' '));
        assertTrue(CharUtils.isSpace('\t'));
        assertTrue(CharUtils.isSpace('\r'));
        assertTrue(CharUtils.isSpace('\n'));
        assertFalse(CharUtils.isSpace('a'));
        assertFalse(CharUtils.isSpace('0'));
    }

    @Test
    void testIsNewline() {
        assertTrue(CharUtils.isNewline('\n'));
        assertFalse(CharUtils.isNewline('\r'));
        assertFalse(CharUtils.isNewline(' '));
        assertFalse(CharUtils.isNewline('a'));
    }

    @Test
    void testIsAscii() {
        assertTrue(CharUtils.isAscii('a'));
        assertTrue(CharUtils.isAscii('Z'));
        assertTrue(CharUtils.isAscii('0'));
        assertTrue(CharUtils.isAscii('@'));
        assertTrue(CharUtils.isAscii('~'));
        assertFalse(CharUtils.isAscii('\u00E9')); // é
        assertFalse(CharUtils.isAscii('\u4E2D')); // 中
    }

    @Test
    void testIsVisible() {
        assertTrue(CharUtils.isVisible('a'));
        assertTrue(CharUtils.isVisible('A'));
        assertTrue(CharUtils.isVisible('0'));
        assertTrue(CharUtils.isVisible('@'));
        assertFalse(CharUtils.isVisible('\0')); // null character
        assertFalse(CharUtils.isVisible('\uFFFF')); // special
    }

    @Test
    void testIsNumeric() {
        assertTrue(CharUtils.isNumeric('0'));
        assertTrue(CharUtils.isNumeric('5'));
        assertTrue(CharUtils.isNumeric('9'));
        assertFalse(CharUtils.isNumeric('a'));
        assertFalse(CharUtils.isNumeric('-'));
        assertFalse(CharUtils.isNumeric('.'));
    }

    @Test
    void testIsAlphabetic() {
        assertTrue(CharUtils.isAlphabetic('_'));
        assertTrue(CharUtils.isAlphabetic('a'));
        assertTrue(CharUtils.isAlphabetic('Z'));
        assertTrue(CharUtils.isAlphabetic('0'));
        assertFalse(CharUtils.isAlphabetic('@'));
        assertFalse(CharUtils.isAlphabetic(' '));
    }

    @Test
    void testIsAnyOfWithVarArgs() {
        assertTrue(CharUtils.isAnyOf('a', 'a', 'b', 'c'));
        assertTrue(CharUtils.isAnyOf('b', 'a', 'b', 'c'));
        assertTrue(CharUtils.isAnyOf('c', 'a', 'b', 'c'));
        assertFalse(CharUtils.isAnyOf('d', 'a', 'b', 'c'));
    }

    @Test
    void testIsAnyOfWithCharSequence() {
        assertTrue(CharUtils.isAnyOf('a', "abc"));
        assertTrue(CharUtils.isAnyOf('b', "abc"));
        assertTrue(CharUtils.isAnyOf('c', "abc"));
        assertFalse(CharUtils.isAnyOf('d', "abc"));
    }

    @Test
    void testUnquoteChar() {
        assertEquals('a', CharUtils.unquoteChar("'a'"));
        assertEquals('0', CharUtils.unquoteChar("'0'"));
        assertEquals('_', CharUtils.unquoteChar("'_'"));
    }

    @Test
    void testUnquoteCharWithBackslash() {
        // This tests the backslash handling path
        assertEquals('\\', CharUtils.unquoteChar("'\\'"));
    }

    @Test
    void testUnquoteCharThrowsExceptionForEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            CharUtils.unquoteChar("");
        });
    }

    @Test
    void testUnquoteCharThrowsExceptionForNoLeadingQuote() {
        assertThrows(IllegalArgumentException.class, () -> {
            CharUtils.unquoteChar("a'");
        });
    }

    @Test
    void testUnquoteCharThrowsExceptionForNoTrailingQuote() {
        assertThrows(IllegalArgumentException.class, () -> {
            CharUtils.unquoteChar("'a");
        });
    }

    @Test
    void testUnquoteCharThrowsExceptionForTooLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            CharUtils.unquoteChar("'abc'");
        });
    }

    @Test
    void testConstants() {
        assertEquals((char) -1, CharUtils.EOF);
        assertEquals(' ', CharUtils.SPACE);
        assertEquals('\t', CharUtils.TAB);
        assertEquals('\r', CharUtils.RETURN);
        assertEquals('\n', CharUtils.NEW_LINE);
        assertEquals('_', CharUtils.UNDERSCORE);
        
        assertNotNull(CharUtils.DECIMAL_DIGITS);
        assertNotNull(CharUtils.HEX_DIGITS);
        assertNotNull(CharUtils.OCTET_DIGITS);
        assertNotNull(CharUtils.BINARY_DIGITS);
    }
}
