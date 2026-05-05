package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerVariablePipelineTest {

    @Test
    void dotToken() {
        assertDefaultTokens("{{.}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.DOT, ".", 2, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 3, 1, 0),
                token(TokenType.EOF, "", 5, 1, 0));
    }

    @Test
    void dottedFields() {
        assertDefaultTokens("{{.x . .2 .x.y.z}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.FIELD, ".x", 2, 1, 0),
                token(TokenType.SPACE, " ", 4, 1, 0),
                token(TokenType.DOT, ".", 5, 1, 0),
                token(TokenType.SPACE, " ", 6, 1, 0),
                token(TokenType.FIELD, ".2", 7, 1, 0),
                token(TokenType.SPACE, " ", 9, 1, 0),
                token(TokenType.FIELD, ".x", 10, 1, 0),
                token(TokenType.FIELD, ".y", 12, 1, 0),
                token(TokenType.FIELD, ".z", 14, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 16, 1, 0),
                token(TokenType.EOF, "", 18, 1, 0));
    }

    @Test
    void keywords() {
        assertDefaultTokens("{{range if else end with}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.RANGE, "range", 2, 1, 0),
                token(TokenType.SPACE, " ", 7, 1, 0),
                token(TokenType.IF, "if", 8, 1, 0),
                token(TokenType.SPACE, " ", 10, 1, 0),
                token(TokenType.ELSE, "else", 11, 1, 0),
                token(TokenType.SPACE, " ", 15, 1, 0),
                token(TokenType.END, "end", 16, 1, 0),
                token(TokenType.SPACE, " ", 19, 1, 0),
                token(TokenType.WITH, "with", 20, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 24, 1, 0),
                token(TokenType.EOF, "", 26, 1, 0));
    }

    @Test
    void variablesAndFieldAccess() {
        assertDefaultTokens("{{$c := printf $ $hello $23 $ $var.Field .Method}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.VARIABLE, "$c", 2, 1, 0),
                token(TokenType.SPACE, " ", 4, 1, 0),
                token(TokenType.DECLARE, ":=", 5, 1, 0),
                token(TokenType.SPACE, " ", 7, 1, 0),
                token(TokenType.IDENTIFIER, "printf", 8, 1, 0),
                token(TokenType.SPACE, " ", 14, 1, 0),
                token(TokenType.VARIABLE, "$", 15, 1, 0),
                token(TokenType.SPACE, " ", 16, 1, 0),
                token(TokenType.VARIABLE, "$hello", 17, 1, 0),
                token(TokenType.SPACE, " ", 23, 1, 0),
                token(TokenType.VARIABLE, "$23", 24, 1, 0),
                token(TokenType.SPACE, " ", 27, 1, 0),
                token(TokenType.VARIABLE, "$", 28, 1, 0),
                token(TokenType.SPACE, " ", 29, 1, 0),
                token(TokenType.VARIABLE, "$var", 30, 1, 0),
                token(TokenType.FIELD, ".Field", 34, 1, 0),
                token(TokenType.SPACE, " ", 40, 1, 0),
                token(TokenType.FIELD, ".Method", 41, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 48, 1, 0),
                token(TokenType.EOF, "", 50, 1, 0));
    }

    @Test
    void variableInvocation() {
        assertDefaultTokens("{{$x 23}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.VARIABLE, "$x", 2, 1, 0),
                token(TokenType.SPACE, " ", 4, 1, 0),
                token(TokenType.NUMBER, "23", 5, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 7, 1, 0),
                token(TokenType.EOF, "", 9, 1, 0));
    }

    @Test
    void pipeline() {
        assertDefaultTokens("intro {{echo hi 1.2 |noargs|args 1 \"hi\"}} outro",
                token(TokenType.TEXT, "intro ", 0, 1, 0),
                token(TokenType.LEFT_DELIM, "{{", 6, 1, 0),
                token(TokenType.IDENTIFIER, "echo", 8, 1, 0),
                token(TokenType.SPACE, " ", 12, 1, 0),
                token(TokenType.IDENTIFIER, "hi", 13, 1, 0),
                token(TokenType.SPACE, " ", 15, 1, 0),
                token(TokenType.NUMBER, "1.2", 16, 1, 0),
                token(TokenType.SPACE, " ", 19, 1, 0),
                token(TokenType.PIPE, "|", 20, 1, 0),
                token(TokenType.IDENTIFIER, "noargs", 21, 1, 0),
                token(TokenType.PIPE, "|", 27, 1, 0),
                token(TokenType.IDENTIFIER, "args", 28, 1, 0),
                token(TokenType.SPACE, " ", 32, 1, 0),
                token(TokenType.NUMBER, "1", 33, 1, 0),
                token(TokenType.SPACE, " ", 34, 1, 0),
                token(TokenType.STRING, "\"hi\"", 35, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 39, 1, 0),
                token(TokenType.TEXT, " outro", 41, 1, 0),
                token(TokenType.EOF, "", 47, 1, 0));
    }

    @Test
    void declaration() {
        assertDefaultTokens("{{$v := 3}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.VARIABLE, "$v", 2, 1, 0),
                token(TokenType.SPACE, " ", 4, 1, 0),
                token(TokenType.DECLARE, ":=", 5, 1, 0),
                token(TokenType.SPACE, " ", 7, 1, 0),
                token(TokenType.NUMBER, "3", 8, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 9, 1, 0),
                token(TokenType.EOF, "", 11, 1, 0));
    }

    @Test
    void multiDeclaration() {
        assertDefaultTokens("{{$v , $w := 3}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.VARIABLE, "$v", 2, 1, 0),
                token(TokenType.SPACE, " ", 4, 1, 0),
                token(TokenType.CHAR, ",", 5, 1, 0),
                token(TokenType.SPACE, " ", 6, 1, 0),
                token(TokenType.VARIABLE, "$w", 7, 1, 0),
                token(TokenType.SPACE, " ", 9, 1, 0),
                token(TokenType.DECLARE, ":=", 10, 1, 0),
                token(TokenType.SPACE, " ", 12, 1, 0),
                token(TokenType.NUMBER, "3", 13, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 14, 1, 0),
                token(TokenType.EOF, "", 16, 1, 0));
    }

    @Test
    void parenthesizedField() {
        assertDefaultTokens("{{(.X).Y}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.LEFT_PAREN, "(", 2, 1, 0),
                token(TokenType.FIELD, ".X", 3, 1, 0),
                token(TokenType.RIGHT_PAREN, ")", 5, 1, 0),
                token(TokenType.FIELD, ".Y", 6, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 8, 1, 0),
                token(TokenType.EOF, "", 10, 1, 0));
    }
}
