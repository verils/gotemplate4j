package io.github.verils.gotemplate.internal;

import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

final class LexerTestSupport {

    private LexerTestSupport() {
    }

    static Token[] lexDefault(String input) {
        Lexer lexer = new Lexer(input, true);
        return lexer.getTokens().toArray(new Token[0]);
    }

    static Token[] lexDelim(String input) {
        return lexCustom(input, "$$", "@@");
    }

    static Token[] lexCustom(String input, String leftDelimiter, String rightDelimiter) {
        Lexer lexer = new Lexer(input, true, leftDelimiter, rightDelimiter, "/*", "*/");
        return lexer.getTokens().toArray(new Token[0]);
    }

    static Token token(TokenType type, String value, int pos, int line, int column) {
        return new Token(type, value, pos, line, column);
    }

    static Arguments defaultCase(String input, Token... expectedTokens) {
        return arguments(input, expectedTokens);
    }

    static Arguments delimiterCase(String input, Token... expectedTokens) {
        return arguments(input, expectedTokens);
    }

    static void assertDefaultTokens(String input, Token... expectedTokens) {
        assertTokens(expectedTokens, lexDefault(input));
    }

    static void assertDelimiterTokens(String input, Token... expectedTokens) {
        assertTokens(expectedTokens, lexDelim(input));
    }

    static void assertTokens(Token[] expectedTokens, Token[] actualTokens) {
        assertEquals(expectedTokens.length, actualTokens.length,
                String.format("expected %d tokens, got %d tokens", expectedTokens.length, actualTokens.length));

        for (int i = 0; i < expectedTokens.length; i++) {
            assertToken(expectedTokens[i], actualTokens[i], i);
        }
    }

    private static void assertToken(Token expected, Token actual, int index) {
        assertNotNull(actual, String.format("Error on token[%d], expected \"%s\" but got null",
                index, expected.value()));
        assertEquals(expected.type(), actual.type(), String.format("Error on token[%d], expected token type:\"%s\" but got \"%s\"",
                index, expected.type(), actual.type()));
        assertEquals(expected.value(), actual.value(), String.format("Error on token[%d], expected item value: '%s', value: '%s'",
                index, expected.value(), actual.value()));
        assertEquals(expected.pos(), actual.pos());
        assertEquals(expected.line(), actual.line());

        if (expected.column() > 0) {
            assertEquals(expected.column(), actual.column());
        }
    }
}
