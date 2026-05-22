package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDelimiterTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.assertTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.defaultCase;
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
    @MethodSource("leadingWhitespaceTrimMarkerCases")
    void trimMarkerRemovesLeadingWhitespace(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> leadingWhitespaceTrimMarkerCases() {
        return Stream.of(
                defaultCase("{{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 4, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 5, 1, 0),
                        token(TokenType.EOF, "", 7, 1, 0)),
                defaultCase("  {{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 2, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 6, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 7, 1, 0),
                        token(TokenType.EOF, "", 9, 1, 0)),
                defaultCase("\t{{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 1, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 5, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 6, 1, 0),
                        token(TokenType.EOF, "", 8, 1, 0)),
                defaultCase("\n{{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 1, 2, 0),
                        token(TokenType.IDENTIFIER, "x", 5, 2, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 6, 2, 0),
                        token(TokenType.EOF, "", 8, 2, 0)),
                defaultCase("\r{{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 1, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 5, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 6, 1, 0),
                        token(TokenType.EOF, "", 8, 1, 0)),
                defaultCase(" \t\r\n{{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 4, 2, 0),
                        token(TokenType.IDENTIFIER, "x", 8, 2, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 9, 2, 0),
                        token(TokenType.EOF, "", 11, 2, 0)),
                defaultCase("      {{- x}}",
                        token(TokenType.LEFT_DELIM, "{{", 6, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 10, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 11, 1, 0),
                        token(TokenType.EOF, "", 13, 1, 0)));
    }

    @ParameterizedTest
    @MethodSource("emptyTrimActionCases")
    void trimMarkerSupportsEmptyActions(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> emptyTrimActionCases() {
        return Stream.of(
                defaultCase("{{- }}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 4, 1, 0),
                        token(TokenType.EOF, "", 6, 1, 0)),
                defaultCase("  {{- }}",
                        token(TokenType.LEFT_DELIM, "{{", 2, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 6, 1, 0),
                        token(TokenType.EOF, "", 8, 1, 0)));
    }

    @ParameterizedTest
    @MethodSource("trimCommentCases")
    void trimMarkerSupportsStandaloneComments(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> trimCommentCases() {
        return Stream.of(
                defaultCase("{{- /* comment */ -}}",
                        token(TokenType.COMMENT, "/* comment */", 4, 1, 0),
                        token(TokenType.EOF, "", 21, 1, 0)),
                defaultCase(" \t\n{{- /* comment */ -}}",
                        token(TokenType.COMMENT, "/* comment */", 7, 2, 0),
                        token(TokenType.EOF, "", 24, 2, 0)));
    }

    @ParameterizedTest
    @MethodSource("textBeforeTrimMarkerCases")
    void trimMarkerTrimsWhitespaceAfterPreviousText(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> textBeforeTrimMarkerCases() {
        return Stream.of(
                defaultCase("hello {{- x}}",
                        token(TokenType.TEXT, "hello", 0, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 6, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 10, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 11, 1, 0),
                        token(TokenType.EOF, "", 13, 1, 0)),
                defaultCase("a {{- x}}",
                        token(TokenType.TEXT, "a", 0, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 2, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 6, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 7, 1, 0),
                        token(TokenType.EOF, "", 9, 1, 0)),
                defaultCase("hello\n{{- x}}",
                        token(TokenType.TEXT, "hello", 0, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 6, 2, 0),
                        token(TokenType.IDENTIFIER, "x", 10, 2, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 11, 2, 0),
                        token(TokenType.EOF, "", 13, 2, 0)),
                defaultCase("0123{{hello}}  {{- x}}",
                        token(TokenType.TEXT, "0123", 0, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 4, 1, 0),
                        token(TokenType.IDENTIFIER, "hello", 6, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 11, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 15, 1, 0),
                        token(TokenType.IDENTIFIER, "x", 19, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 20, 1, 0),
                        token(TokenType.EOF, "", 22, 1, 0)));
    }

    @Test
    void trimMarkersOnBothSidesRemoveSurroundingWhitespace() {
        assertDefaultTokens("hello {{- x -}} world",
                token(TokenType.TEXT, "hello", 0, 1, 0),
                token(TokenType.LEFT_DELIM, "{{", 6, 1, 0),
                token(TokenType.IDENTIFIER, "x", 10, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 13, 1, 0),
                token(TokenType.TEXT, "world", 16, 1, 0),
                token(TokenType.EOF, "", 21, 1, 0));
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
