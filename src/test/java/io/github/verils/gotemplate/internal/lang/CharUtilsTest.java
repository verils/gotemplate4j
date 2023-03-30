package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CharUtilsTest {

    @Test
    void unquotedChar() {
        assertThrows(IllegalArgumentException.class, () -> CharUtils.unquotedChar("\\'x"));
        assertThrows(IllegalArgumentException.class, () -> CharUtils.unquotedChar("'xx'"));
    }

}