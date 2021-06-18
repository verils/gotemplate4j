package com.github.verils.gotemplate.parse;

public enum NodeType {

    TEXT(0),
    ACTION(1),
    BOOL(2),
    CHAIN(3),
    COMMAND(4),
    DOT(5),
    ELSE(6),
    END(7),
    FIELD(8),
    IDENTIFIER(9),
    IF(10),
    LIST(11),
    NIL(12),
    NUMBER(13),
    PIPE(14),
    RANGE(15),
    STRING(16),
    TEMPLATE(17),
    VARIABLE(18),
    WITH(19),
    COMMENT(20);


    private final int code;

    NodeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
