package com.github.verils.gotemplate.lex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringEscapeUtilsTest {

    @Test
    void unescape() {
        String unescaped = StringEscapeUtils.unescape("\\n");
        assertEquals("\n", unescaped);

        unescaped = StringEscapeUtils.unescape("Name:");
        assertEquals("Name:", unescaped);
    }
}