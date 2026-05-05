package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerTextAndActionTest {

    @Test
    void emptyTemplateEmitsOnlyEof() {
        assertDefaultTokens("",
                token(TokenType.EOF, "", 0, 1, 1));
    }

    @Test
    void spaceCharsAreTextOutsideActions() {
        assertDefaultTokens(" \t\n",
                token(TokenType.TEXT, " \t\n", 0, 1, 1),
                token(TokenType.EOF, "", 3, 2, 1));
    }

    @Test
    void singleLineTextIsOneTextToken() {
        assertDefaultTokens("now is the time",
                token(TokenType.TEXT, "now is the time", 0, 1, 1),
                token(TokenType.EOF, "", 15, 1, 16));
    }

    @Test
    void textWithCommentKeepsTextAroundComment() {
        assertDefaultTokens("hello-{{/* this is a comment */}}-world",
                token(TokenType.TEXT, "hello-", 0, 1, 1),
                token(TokenType.COMMENT, "/* this is a comment */", 8, 1, 9),
                token(TokenType.TEXT, "-world", 33, 1, 34),
                token(TokenType.EOF, "", 39, 1, 40));
    }

    @Test
    void actionSpecialCharsAreCharTokens() {
        assertDefaultTokens("{{,@% }}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.CHAR, ",", 2, 1, 3),
                token(TokenType.CHAR, "@", 3, 1, 4),
                token(TokenType.CHAR, "%", 4, 1, 5),
                token(TokenType.SPACE, " ", 5, 1, 6),
                token(TokenType.RIGHT_DELIM, "}}", 6, 1, 7),
                token(TokenType.EOF, "", 8, 1, 9));
    }

    @Test
    void actionNumberWithParentheses() {
        assertDefaultTokens("{{((3))}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.LEFT_PAREN, "(", 2, 1, 3),
                token(TokenType.LEFT_PAREN, "(", 3, 1, 4),
                token(TokenType.NUMBER, "3", 4, 1, 5),
                token(TokenType.RIGHT_PAREN, ")", 5, 1, 6),
                token(TokenType.RIGHT_PAREN, ")", 6, 1, 7),
                token(TokenType.RIGHT_DELIM, "}}", 7, 1, 8),
                token(TokenType.EOF, "", 9, 1, 10));
    }

    @Test
    void emptyActionEmitsBothDelimiters() {
        assertDefaultTokens("{{}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.RIGHT_DELIM, "}}", 2, 1, 3),
                token(TokenType.EOF, "", 4, 1, 5));
    }

    @Test
    void nonKeywordForIsIdentifier() {
        assertDefaultTokens("{{for}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.IDENTIFIER, "for", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 5, 1, 6),
                token(TokenType.EOF, "", 7, 1, 8));
    }

    @Test
    void blockKeywordAndArguments() {
        assertDefaultTokens("{{block \"foo\" .}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.BLOCK, "block", 2, 1, 3),
                token(TokenType.SPACE, " ", 7, 1, 8),
                token(TokenType.STRING, "\"foo\"", 8, 1, 9),
                token(TokenType.SPACE, " ", 13, 1, 14),
                token(TokenType.DOT, ".", 14, 1, 15),
                token(TokenType.RIGHT_DELIM, "}}", 15, 1, 16),
                token(TokenType.EOF, "", 17, 1, 18));
    }

    @Test
    void standaloneCommentOmitsActionDelimiters() {
        assertDefaultTokens("{{/* this is a comment */}}",
                token(TokenType.COMMENT, "/* this is a comment */", 2, 1, 0),
                token(TokenType.EOF, "", 27, 1, 0));
    }
}
