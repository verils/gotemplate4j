package com.github.verils.gotemplate.lex;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void unescape() {
        String unescaped = StringUtils.unescape("\\n");
        assertEquals("\n", unescaped);
    }
}