package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CharUtilsTest {

    @Test
    void unquoteChar() {
        assertThrows(IllegalArgumentException.class, () -> CharUtils.unquoteChar("\\'x"));
        assertThrows(IllegalArgumentException.class, () -> CharUtils.unquoteChar("'xx'"));
    }

}