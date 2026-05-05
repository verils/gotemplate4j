package io.github.verils.gotemplate.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.defaultCase;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerErrorTest {

    @ParameterizedTest
    @MethodSource("errorCases")
    void errorTokens(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> errorCases() {
        return Stream.of(
                defaultCase("#{{\1}}",
                        token(TokenType.TEXT, "#", 0, 1, 0),
                        token(TokenType.LEFT_DELIM, "{{", 1, 1, 0),
                        token(TokenType.ERROR, "bad character in action: \1", 3, 1, 0)),
                defaultCase("{{",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.ERROR, "unclosed action", 2, 1, 0)),
                defaultCase("{{range",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.RANGE, "range", 2, 1, 0),
                        token(TokenType.ERROR, "unclosed action", 7, 1, 0)),
                defaultCase("{{\"\n\"}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                        token(TokenType.ERROR, "unterminated quoted string", 2, 1, 3)),
                defaultCase("{{`xx}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.ERROR, "unclosed raw quote", 2, 1, 0)),
                defaultCase("{{'\n}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                        token(TokenType.ERROR, "unclosed character constant", 2, 1, 3)),
                defaultCase("{{3k}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.ERROR, "bad number: 3k", 2, 1, 0)),
                defaultCase("{{(3}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.LEFT_PAREN, "(", 2, 1, 0),
                        token(TokenType.NUMBER, "3", 3, 1, 0),
                        token(TokenType.ERROR, "unclosed left paren", 4, 1, 0)),
                defaultCase("{{3)}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.NUMBER, "3", 2, 1, 0),
                        token(TokenType.ERROR, "unexpected right paren", 3, 1, 0)),
                defaultCase("hello-{{/*/}}-world",
                        token(TokenType.TEXT, "hello-", 0, 1, 0),
                        token(TokenType.ERROR, "unclosed comment", 8, 1, 0)),
                defaultCase("hello-{{/* */ }}-world",
                        token(TokenType.TEXT, "hello-", 0, 1, 0),
                        token(TokenType.ERROR, "comment closed leaving delim still open", 8, 1, 0)),
                defaultCase("{{$v : 3}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.VARIABLE, "$v", 2, 1, 0),
                        token(TokenType.SPACE, " ", 4, 1, 0),
                        token(TokenType.ERROR, "expected :=", 5, 1, 0)));
    }

    @ParameterizedTest
    @MethodSource("nonErrorEdgeCases")
    void nonErrorEdgeCases(String input, Token[] expectedTokens) {
        assertDefaultTokens(input, expectedTokens);
    }

    private static Stream<Arguments> nonErrorEdgeCases() {
        return Stream.of(
                defaultCase("{{|||||}}",
                        token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                        token(TokenType.PIPE, "|", 2, 1, 0),
                        token(TokenType.PIPE, "|", 3, 1, 0),
                        token(TokenType.PIPE, "|", 4, 1, 0),
                        token(TokenType.PIPE, "|", 5, 1, 0),
                        token(TokenType.PIPE, "|", 6, 1, 0),
                        token(TokenType.RIGHT_DELIM, "}}", 7, 1, 0),
                        token(TokenType.EOF, "", 9, 1, 0)),
                defaultCase("hello-{.}}-world",
                        token(TokenType.TEXT, "hello-{.}}-world", 0, 1, 0),
                        token(TokenType.EOF, "", 16, 1, 0)));
    }
}
