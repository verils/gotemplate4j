package io.github.verils.gotemplate.internal;

public enum TokenType {

    /**
     * equals ('=') introducing an assignment
     */
    ASSIGN,

    /**
     * block keyword
     */
    BLOCK,

    /**
     * boolean constant
     */
    BOOL,

    /**
     * printable ASCII character; grab bag for comma etc.
     */
    CHAR,

    /**
     * character constant
     */
    CHAR_CONSTANT,

    /**
     * comment text
     */
    COMMENT,

    /**
     * complex constant (1+2i); imaginary is just a number
     */
    COMPLEX,

    /**
     * colon-equals (':=') introducing a declaration
     */
    DECLARE,

    /**
     * define keyword
     */
    DEFINE,

    /**
     * the cursor, spelled '.'
     */
    DOT,

    /**
     * else keyword
     */
    ELSE,

    /**
     * end keyword
     */
    END,

    /**
     * end of file
     */
    EOF,

    /**
     * error occurred; value is text of error
     */
    ERROR,

    /**
     * alphanumeric identifier starting with '.'
     */
    FIELD,

    /**
     * alphanumeric identifier not starting with '.'
     */
    IDENTIFIER,

    /**
     * if keyword
     */
    IF,

    /**
     * used only to delimit the keywords
     */
    KEYWORD,

    /**
     * left action delimiter
     */
    LEFT_DELIM,

    /**
     * '(' inside action
     */
    LEFT_PAREN,

    /**
     * the untyped nil constant, easiest to treat as a keyword
     */
    NIL,

    /**
     * simple number, including imaginary
     */
    NUMBER,

    /**
     * pipe symbol
     */
    PIPE,

    /**
     * range keyword
     */
    RANGE,

    /**
     * raw quoted string (includes quotes)
     */
    RAW_STRING,

    /**
     * right action delimiter
     */
    RIGHT_DELIM,

    /**
     * ')' inside action
     */
    RIGHT_PAREN,

    /**
     * run of spaces separating arguments
     */
    SPACE,

    /**
     * quoted string (includes quotes)
     */
    STRING,

    /**
     * template keyword
     */
    TEMPLATE,

    /**
     * plain text
     */
    TEXT,

    /**
     * variable starting with '$', such as '$' or  '$1' or '$hello'
     */
    VARIABLE,

    /**
     * with keyword
     */
    WITH;

    TokenType() {
    }

}
