package com.github.verils.gotemplate.lex;

public enum ItemType {

    ERROR(0),
    BOOL(1),
    CHAR(2),
    CHAR_CONSTANT(3),
    COMMENT(4),
    COMPLEX(5),
    ASSIGN(6),
    DECLARE(7),
    EOF(8),
    FIELD(9),
    IDENTIFIER(10),
    LEFT_DELIM(11),
    LEFT_PAREN(12),
    NUMBER(13),
    PIPE(14),
    RAW_STRING(15),
    RIGHT_DELIM(16),
    RIGHT_PAREN(17),
    SPACE(18),
    STRING(19),
    TEXT(20),
    VARIABLE(21),
    KEYWORD(22),
    BLOCK(23),
    DOT(24),
    DEFINE(25),
    ELSE(26),
    END(27),
    IF(28),
    NIL(29),
    RANGE(30),
    TEMPLATE(31),
    WITH(32);


    private final int code;

    ItemType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
