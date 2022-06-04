package io.github.verils.gotemplate.runtime.simple.lex;

import io.github.verils.gotemplate.runtime.simple.lex.StringEscapeUtils;
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