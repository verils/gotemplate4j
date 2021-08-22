package com.github.verils.gotemplate.lex;

public enum ItemType {

    ASSIGN,
    BLOCK,
    BOOL,
    CHAR,
    CHAR_CONSTANT,
    COMMENT,
    COMPLEX,
    DECLARE,
    DEFINE,
    DOT,
    ELSE,
    END,
    EOF,
    ERROR,
    FIELD,
    IDENTIFIER,
    IF,
    KEYWORD,
    LEFT_DELIM,
    LEFT_PAREN,
    NIL,
    NUMBER,
    PIPE,
    RANGE,
    RAW_STRING,
    RIGHT_DELIM,
    RIGHT_PAREN,
    SPACE,
    STRING,
    TEMPLATE,
    TEXT,
    VARIABLE,
    WITH;

    ItemType() {
    }

}
