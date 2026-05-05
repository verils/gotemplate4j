package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerPositionTest {

    @Test
    void punctuationPositions() {
        assertDefaultTokens("{{,@%#}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.CHAR, ",", 2, 1, 0),
                token(TokenType.CHAR, "@", 3, 1, 0),
                token(TokenType.CHAR, "%", 4, 1, 0),
                token(TokenType.CHAR, "#", 5, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 6, 1, 0),
                token(TokenType.EOF, "", 8, 1, 0));
    }

    @Test
    void textAndActionPositions() {
        assertDefaultTokens("0123{{hello}}xyz",
                token(TokenType.TEXT, "0123", 0, 1, 0),
                token(TokenType.LEFT_DELIM, "{{", 4, 1, 0),
                token(TokenType.IDENTIFIER, "hello", 6, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 11, 1, 0),
                token(TokenType.TEXT, "xyz", 13, 1, 0),
                token(TokenType.EOF, "", 16, 1, 0));
    }

    @Test
    void trimAfterUpdatesNextLinePositions() {
        assertDefaultTokens("{{x -}}\n{{y}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.IDENTIFIER, "x", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 5, 1, 6),
                token(TokenType.LEFT_DELIM, "{{", 8, 2, 1),
                token(TokenType.IDENTIFIER, "y", 10, 2, 3),
                token(TokenType.RIGHT_DELIM, "}}", 11, 2, 4),
                token(TokenType.EOF, "", 13, 2, 6));
    }

    @Test
    void trimBeforeUpdatesActionPositions() {
        assertDefaultTokens("{{x}}\n{{- y}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.IDENTIFIER, "x", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 3, 1, 4),
                token(TokenType.LEFT_DELIM, "{{", 6, 2, 1),
                token(TokenType.IDENTIFIER, "y", 10, 2, 5),
                token(TokenType.RIGHT_DELIM, "}}", 11, 2, 6),
                token(TokenType.EOF, "", 13, 2, 8));
    }
}
