package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDelimiterTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.assertTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.delimiterCase;
import static io.github.verils.gotemplate.internal.LexerTestSupport.lexCustom;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerTrimDelimiterTest {

    @Test
    void trimRemovesSurroundingWhitespace() {
        assertDefaultTokens("hello- {{- 3 -}} -world",
                token(TokenType.TEXT, "hello-", 0, 1, 0),
                token(TokenType.LEFT_DELIM, "{{", 7, 1, 0),
                token(TokenType.NUMBER, "3", 11, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 14, 1, 0),
                token(TokenType.TEXT, "-world", 17, 1, 0),
                token(TokenType.EOF, "", 23, 1, 0));
    }

    @Test
    void trimWithCommentRemovesSurroundingWhitespaceAndDelimiters() {
        assertDefaultTokens("hello- {{- /* hello */ -}} -world",
                token(TokenType.TEXT, "hello-", 0, 1, 0),
                token(TokenType.COMMENT, "/* hello */", 11, 1, 0),
                token(TokenType.TEXT, "-world", 27, 1, 0),
                token(TokenType.EOF, "", 33, 1, 0));
    }

    @ParameterizedTest
    @MethodSource("simpleCustomDelimiterCases")
    void customDelimiters(String input, Token[] expectedTokens) {
        assertDelimiterTokens(input, expectedTokens);
    }

    private static Stream<Arguments> simpleCustomDelimiterCases() {
        return Stream.of(
                delimiterCase("$$,@%{{}}@@",
                        token(TokenType.LEFT_DELIM, "$$", 0, 1, 0),
                        token(TokenType.CHAR, ",", 2, 1, 0),
                        token(TokenType.CHAR, "@", 3, 1, 0),
                        token(TokenType.CHAR, "%", 4, 1, 0),
                        token(TokenType.CHAR, "{", 5, 1, 0),
                        token(TokenType.CHAR, "{", 6, 1, 0),
                        token(TokenType.CHAR, "}", 7, 1, 0),
                        token(TokenType.CHAR, "}", 8, 1, 0),
                        token(TokenType.RIGHT_DELIM, "@@", 9, 1, 0),
                        token(TokenType.EOF, "", 11, 1, 0)),
                delimiterCase("$$@@",
                        token(TokenType.LEFT_DELIM, "$$", 0, 1, 0),
                        token(TokenType.RIGHT_DELIM, "@@", 2, 1, 0),
                        token(TokenType.EOF, "", 4, 1, 0)),
                delimiterCase("$$for@@",
                        token(TokenType.LEFT_DELIM, "$$", 0, 1, 0),
                        token(TokenType.IDENTIFIER, "for", 2, 1, 0),
                        token(TokenType.RIGHT_DELIM, "@@", 5, 1, 0),
                        token(TokenType.EOF, "", 7, 1, 0)),
                delimiterCase("$$\"abc \\n\\t\\\" \"@@",
                        token(TokenType.LEFT_DELIM, "$$", 0, 1, 1),
                        token(TokenType.STRING, "\"abc \\n\\t\\\" \"", 2, 1, 3),
                        token(TokenType.RIGHT_DELIM, "@@", 15, 1, 16),
                        token(TokenType.EOF, "", 17, 1, 18)),
                delimiterCase("$$`abc\\n\\t\\\" `@@",
                        token(TokenType.LEFT_DELIM, "$$", 0, 1, 1),
                        token(TokenType.RAW_STRING, "`abc\\n\\t\\\" `", 2, 1, 3),
                        token(TokenType.RIGHT_DELIM, "@@", 14, 1, 15),
                        token(TokenType.EOF, "", 16, 1, 17)));
    }

    @Test
    void customDelimiterMayContainTemplateDelimiterRunes() {
        assertTokens(new Token[]{
                        token(TokenType.LEFT_DELIM, "{{hub", 0, 1, 0),
                        token(TokenType.SPACE, " ", 5, 1, 0),
                        token(TokenType.FIELD, ".host", 6, 1, 0),
                        token(TokenType.SPACE, " ", 11, 1, 0),
                        token(TokenType.RIGHT_DELIM, "hub}}", 12, 1, 0),
                        token(TokenType.EOF, "", 17, 1, 0),
                },
                lexCustom("{{hub .host hub}}", "{{hub", "hub}}"));
    }

    @Test
    void customDelimiterWithTrimMarkers() {
        assertTokens(new Token[]{
                        token(TokenType.LEFT_DELIM, "{{- ", 0, 1, 0),
                        token(TokenType.FIELD, ".x", 4, 1, 0),
                        token(TokenType.RIGHT_DELIM, " -}}", 6, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{- ", 11, 1, 0),
                        token(TokenType.FIELD, ".x", 17, 1, 0),
                        token(TokenType.RIGHT_DELIM, " -}}", 21, 1, 0),
                        token(TokenType.EOF, "", 25, 1, 0),
                },
                lexCustom("{{- .x -}} {{- - .x - -}}", "{{- ", " -}}"));
    }
}
