package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {


    @Test
    void testEmpty() {
        Token[] actualTokens = lexDefault("");

        Token[] expectedTokens = {
                makeToken(TokenType.EOF, "", 0, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testSpaceChars() {
        Token[] actualTokens = lexDefault(" \t\n");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, " \t\n", 0, 1),
                makeToken(TokenType.EOF, "", 3, 2)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testSingleLine() {
        Token[] actualTokens = lexDefault("now is the time");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "now is the time", 0, 1),
                makeToken(TokenType.EOF, "", 15, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testTextWithComment() {
        Token[] actualTokens = lexDefault("hello-{{/* this is a comment */}}-world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-", 0, 1),
                makeToken(TokenType.COMMENT, "/* this is a comment */", 8, 1),
                makeToken(TokenType.TEXT, "-world", 33, 1),
                makeToken(TokenType.EOF, "", 39, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testActionSpecialChars() {
        Token[] actualTokens = lexDefault("{{,@% }}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.CHAR, ",", 2, 1),
                makeToken(TokenType.CHAR, "@", 3, 1),
                makeToken(TokenType.CHAR, "%", 4, 1),
                makeToken(TokenType.SPACE, " ", 5, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 6, 1),
                makeToken(TokenType.EOF, "", 8, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testActionNumber() {
        Token[] actualTokens = lexDefault("{{((3))}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.LEFT_PAREN, "(", 2, 1),
                makeToken(TokenType.LEFT_PAREN, "(", 3, 1),
                makeToken(TokenType.NUMBER, "3", 4, 1),
                makeToken(TokenType.RIGHT_PAREN, ")", 5, 1),
                makeToken(TokenType.RIGHT_PAREN, ")", 6, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 7, 1),
                makeToken(TokenType.EOF, "", 9, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testActionEmpty() {
        Token[] actualTokens = lexDefault("{{}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 2, 1),
                makeToken(TokenType.EOF, "", 4, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testActionFor() {
        Token[] actualTokens = lexDefault("{{for}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.IDENTIFIER, "for", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 5, 1),
                makeToken(TokenType.EOF, "", 7, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testBlock() {
        Token[] actualTokens = lexDefault("{{block \"foo\" .}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.BLOCK, "block", 2, 1),
                makeToken(TokenType.SPACE, " ", 7, 1),
                makeToken(TokenType.STRING, "\"foo\"", 8, 1),
                makeToken(TokenType.SPACE, " ", 13, 1),
                makeToken(TokenType.DOT, ".", 14, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 15, 1),
                makeToken(TokenType.EOF, "", 17, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testQuote() {
        Token[] actualTokens = lexDefault("{{\"abc \\n\\t\\\" \"}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.STRING, "\"abc \\n\\t\\\" \"", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 15, 1),
                makeToken(TokenType.EOF, "", 17, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testRawQuote() {
        Token[] actualTokens = lexDefault("{{`abc\\n\\t\\\" `}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.RAW_STRING, "`abc\\n\\t\\\" `", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 14, 1),
                makeToken(TokenType.EOF, "", 16, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testRawQuoteWithNewline() {
        Token[] actualTokens = lexDefault("{{`now is{{\\n}}the time`}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.RAW_STRING, "`now is{{\\n}}the time`", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 24, 1),
                makeToken(TokenType.EOF, "", 26, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testNumber() {
        Token[] actualTokens = lexDefault("{{1 02 0x14 0X14 -7.2i 1e3 1E3 +1.2e-4 4.2i 1+2i 1_2 0x1.e_fp4 0X1.E_FP4}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.NUMBER, "1", 2, 1),
                makeToken(TokenType.SPACE, " ", 3, 1),
                makeToken(TokenType.NUMBER, "02", 4, 1),
                makeToken(TokenType.SPACE, " ", 6, 1),
                makeToken(TokenType.NUMBER, "0x14", 7, 1),
                makeToken(TokenType.SPACE, " ", 11, 1),
                makeToken(TokenType.NUMBER, "0X14", 12, 1),
                makeToken(TokenType.SPACE, " ", 16, 1),
                makeToken(TokenType.NUMBER, "-7.2i", 17, 1),
                makeToken(TokenType.SPACE, " ", 22, 1),
                makeToken(TokenType.NUMBER, "1e3", 23, 1),
                makeToken(TokenType.SPACE, " ", 26, 1),
                makeToken(TokenType.NUMBER, "1E3", 27, 1),
                makeToken(TokenType.SPACE, " ", 30, 1),
                makeToken(TokenType.NUMBER, "+1.2e-4", 31, 1),
                makeToken(TokenType.SPACE, " ", 38, 1),
                makeToken(TokenType.NUMBER, "4.2i", 39, 1),
                makeToken(TokenType.SPACE, " ", 43, 1),
                makeToken(TokenType.COMPLEX, "1+2i", 44, 1),
                makeToken(TokenType.SPACE, " ", 48, 1),
                makeToken(TokenType.NUMBER, "1_2", 49, 1),
                makeToken(TokenType.SPACE, " ", 52, 1),
                makeToken(TokenType.NUMBER, "0x1.e_fp4", 53, 1),
                makeToken(TokenType.SPACE, " ", 62, 1),
                makeToken(TokenType.NUMBER, "0X1.E_FP4", 63, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 72, 1),
                makeToken(TokenType.EOF, "", 74, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testCharacter() {
        Token[] actualTokens = lexDefault("{{'a' '\\n' '\\'' '\\\\' '\\u00FF' '\\xFF' '本'}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'a'", 2, 1),
                makeToken(TokenType.SPACE, " ", 5, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'\\n'", 6, 1),
                makeToken(TokenType.SPACE, " ", 10, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'\\''", 11, 1),
                makeToken(TokenType.SPACE, " ", 15, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'\\\\'", 16, 1),
                makeToken(TokenType.SPACE, " ", 20, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'\\u00FF'", 21, 1),
                makeToken(TokenType.SPACE, " ", 29, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'\\xFF'", 30, 1),
                makeToken(TokenType.SPACE, " ", 36, 1),
                makeToken(TokenType.CHAR_CONSTANT, "'本'", 37, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 40, 1),
                makeToken(TokenType.EOF, "", 42, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testBool() {
        Token[] actualTokens = lexDefault("{{true false}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.BOOL, "true", 2, 1),
                makeToken(TokenType.SPACE, " ", 6, 1),
                makeToken(TokenType.BOOL, "false", 7, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 12, 1),
                makeToken(TokenType.EOF, "", 14, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testDot() {
        Token[] actualTokens = lexDefault("{{.}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.DOT, ".", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 3, 1),
                makeToken(TokenType.EOF, "", 5, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testNil() {
        Token[] actualTokens = lexDefault("{{nil}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.NIL, "nil", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 5, 1),
                makeToken(TokenType.EOF, "", 7, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testDots() {
        Token[] actualTokens = lexDefault("{{.x . .2 .x.y.z}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.FIELD, ".x", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.DOT, ".", 5, 1),
                makeToken(TokenType.SPACE, " ", 6, 1),
                makeToken(TokenType.FIELD, ".2", 7, 1),
                makeToken(TokenType.SPACE, " ", 9, 1),
                makeToken(TokenType.FIELD, ".x", 10, 1),
                makeToken(TokenType.FIELD, ".y", 12, 1),
                makeToken(TokenType.FIELD, ".z", 14, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 16, 1),
                makeToken(TokenType.EOF, "", 18, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testKeyword() {
        Token[] actualTokens = lexDefault("{{range if else end with}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.RANGE, "range", 2, 1),
                makeToken(TokenType.SPACE, " ", 7, 1),
                makeToken(TokenType.IF, "if", 8, 1),
                makeToken(TokenType.SPACE, " ", 10, 1),
                makeToken(TokenType.ELSE, "else", 11, 1),
                makeToken(TokenType.SPACE, " ", 15, 1),
                makeToken(TokenType.END, "end", 16, 1),
                makeToken(TokenType.SPACE, " ", 19, 1),
                makeToken(TokenType.WITH, "with", 20, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 24, 1),
                makeToken(TokenType.EOF, "", 26, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testVariable() {
        Token[] actualTokens = lexDefault("{{$c := printf $ $hello $23 $ $var.Field .Method}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.VARIABLE, "$c", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.DECLARE, ":=", 5, 1),
                makeToken(TokenType.SPACE, " ", 7, 1),
                makeToken(TokenType.IDENTIFIER, "printf", 8, 1),
                makeToken(TokenType.SPACE, " ", 14, 1),
                makeToken(TokenType.VARIABLE, "$", 15, 1),
                makeToken(TokenType.SPACE, " ", 16, 1),
                makeToken(TokenType.VARIABLE, "$hello", 17, 1),
                makeToken(TokenType.SPACE, " ", 23, 1),
                makeToken(TokenType.VARIABLE, "$23", 24, 1),
                makeToken(TokenType.SPACE, " ", 27, 1),
                makeToken(TokenType.VARIABLE, "$", 28, 1),
                makeToken(TokenType.SPACE, " ", 29, 1),
                makeToken(TokenType.VARIABLE, "$var", 30, 1),
                makeToken(TokenType.FIELD, ".Field", 34, 1),
                makeToken(TokenType.SPACE, " ", 40, 1),
                makeToken(TokenType.FIELD, ".Method", 41, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 48, 1),
                makeToken(TokenType.EOF, "", 50, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testVariableInvocation() {
        Token[] actualTokens = lexDefault("{{$x 23}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.VARIABLE, "$x", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.NUMBER, "23", 5, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 7, 1),
                makeToken(TokenType.EOF, "", 9, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testPipeline() {
        Token[] actualTokens = lexDefault("intro {{echo hi 1.2 |noargs|args 1 \"hi\"}} outro");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "intro ", 0, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 6, 1),
                makeToken(TokenType.IDENTIFIER, "echo", 8, 1),
                makeToken(TokenType.SPACE, " ", 12, 1),
                makeToken(TokenType.IDENTIFIER, "hi", 13, 1),
                makeToken(TokenType.SPACE, " ", 15, 1),
                makeToken(TokenType.NUMBER, "1.2", 16, 1),
                makeToken(TokenType.SPACE, " ", 19, 1),
                makeToken(TokenType.PIPE, "|", 20, 1),
                makeToken(TokenType.IDENTIFIER, "noargs", 21, 1),
                makeToken(TokenType.PIPE, "|", 27, 1),
                makeToken(TokenType.IDENTIFIER, "args", 28, 1),
                makeToken(TokenType.SPACE, " ", 32, 1),
                makeToken(TokenType.NUMBER, "1", 33, 1),
                makeToken(TokenType.SPACE, " ", 34, 1),
                makeToken(TokenType.STRING, "\"hi\"", 35, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 39, 1),
                makeToken(TokenType.TEXT, " outro", 41, 1),
                makeToken(TokenType.EOF, "", 47, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testDeclaration() {
        Token[] actualTokens = lexDefault("{{$v := 3}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.VARIABLE, "$v", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.DECLARE, ":=", 5, 1),
                makeToken(TokenType.SPACE, " ", 7, 1),
                makeToken(TokenType.NUMBER, "3", 8, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 9, 1),
                makeToken(TokenType.EOF, "", 11, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testMultiDeclarations() {
        Token[] actualTokens = lexDefault("{{$v , $w := 3}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.VARIABLE, "$v", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.CHAR, ",", 5, 1),
                makeToken(TokenType.SPACE, " ", 6, 1),
                makeToken(TokenType.VARIABLE, "$w", 7, 1),
                makeToken(TokenType.SPACE, " ", 9, 1),
                makeToken(TokenType.DECLARE, ":=", 10, 1),
                makeToken(TokenType.SPACE, " ", 12, 1),
                makeToken(TokenType.NUMBER, "3", 13, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 14, 1),
                makeToken(TokenType.EOF, "", 16, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testParenthesizedField() {
        Token[] actualTokens = lexDefault("{{(.X).Y}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.LEFT_PAREN, "(", 2, 1),
                makeToken(TokenType.FIELD, ".X", 3, 1),
                makeToken(TokenType.RIGHT_PAREN, ")", 5, 1),
                makeToken(TokenType.FIELD, ".Y", 6, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 8, 1),
                makeToken(TokenType.EOF, "", 10, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testTrim() {
        Token[] actualTokens = lexDefault("hello- {{- 3 -}} -world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-", 0, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 7, 1),
                makeToken(TokenType.NUMBER, "3", 11, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 14, 1),
                makeToken(TokenType.TEXT, "-world", 17, 1),
                makeToken(TokenType.EOF, "", 23, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testTrimWithComment() {
        Token[] actualTokens = lexDefault("hello- {{- /* hello */ -}} -world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-", 0, 1),
                makeToken(TokenType.COMMENT, "/* hello */", 11, 1),
                makeToken(TokenType.TEXT, "-world", 27, 1),
                makeToken(TokenType.EOF, "", 33, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidChar() {
        Token[] actualTokens = lexDefault("#{{\1}}");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "#", 0, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 1, 1),
                makeToken(TokenType.ERROR, "bad character in action: \1", 3, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedAction() {
        Token[] actualTokens = lexDefault("{{");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.ERROR, "unclosed action", 2, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedAction1() {
        Token[] actualTokens = lexDefault("{{range");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.RANGE, "range", 2, 1),
                makeToken(TokenType.ERROR, "unclosed action", 7, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedQuote() {
        Token[] actualTokens = lexDefault("{{\"\n\"}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.ERROR, "unterminated quoted string", 2, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedRawQuote() {
        Token[] actualTokens = lexDefault("{{`xx}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.ERROR, "unclosed raw quote", 2, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedChar() {
        Token[] actualTokens = lexDefault("{{'\n}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.ERROR, "unclosed character constant", 2, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidNumber() {
        Token[] actualTokens = lexDefault("{{3k}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.ERROR, "bad number: 3k", 2, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedParenthesis() {
        Token[] actualTokens = lexDefault("{{(3}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.LEFT_PAREN, "(", 2, 1),
                makeToken(TokenType.NUMBER, "3", 3, 1),
                makeToken(TokenType.ERROR, "unclosed left paren", 4, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidParenthesis() {
        Token[] actualTokens = lexDefault("{{3)}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.NUMBER, "3", 2, 1),
                makeToken(TokenType.ERROR, "unexpected right paren", 3, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidPipeline() {
        Token[] actualTokens = lexDefault("{{|||||}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.PIPE, "|", 2, 1),
                makeToken(TokenType.PIPE, "|", 3, 1),
                makeToken(TokenType.PIPE, "|", 4, 1),
                makeToken(TokenType.PIPE, "|", 5, 1),
                makeToken(TokenType.PIPE, "|", 6, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 7, 1),
                makeToken(TokenType.EOF, "", 9, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testUnclosedComment() {
        Token[] actualTokens = lexDefault("hello-{{/*/}}-world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-", 0, 1),
                makeToken(TokenType.ERROR, "unclosed comment", 8, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidComment() {
        Token[] actualTokens = lexDefault("hello-{{/* */ }}-world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-", 0, 1),
                makeToken(TokenType.ERROR, "comment closed leaving delim still open", 8, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidDelimiter() {
        Token[] actualTokens = lexDefault("hello-{.}}-world");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "hello-{.}}-world", 0, 1),
                makeToken(TokenType.EOF, "", 16, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testComment() {
        Token[] actualTokens = lexDefault("{{/* this is a comment */}}");

        Token[] expectedTokens = {
                makeToken(TokenType.COMMENT, "/* this is a comment */", 2, 1),
                makeToken(TokenType.EOF, "", 27, 1)
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testInvalidVariable() {
        Token[] actualTokens = lexDefault("{{$v : 3}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.VARIABLE, "$v", 2, 1),
                makeToken(TokenType.SPACE, " ", 4, 1),
                makeToken(TokenType.ERROR, "expected :=", 5, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testPunctuation() {
        Token[] actualTokens = lexDelim("$$,@%{{}}@@");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "$$", 0, 1),
                makeToken(TokenType.CHAR, ",", 2, 1),
                makeToken(TokenType.CHAR, "@", 3, 1),
                makeToken(TokenType.CHAR, "%", 4, 1),
                makeToken(TokenType.CHAR, "{", 5, 1),
                makeToken(TokenType.CHAR, "{", 6, 1),
                makeToken(TokenType.CHAR, "}", 7, 1),
                makeToken(TokenType.CHAR, "}", 8, 1),
                makeToken(TokenType.RIGHT_DELIM, "@@", 9, 1),
                makeToken(TokenType.EOF, "", 11, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testEmptyAction() {
        Token[] actualTokens = lexDelim("$$@@");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "$$", 0, 1),
                makeToken(TokenType.RIGHT_DELIM, "@@", 2, 1),
                makeToken(TokenType.EOF, "", 4, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testForAction() {
        Token[] actualTokens = lexDelim("$$for@@");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "$$", 0, 1),
                makeToken(TokenType.IDENTIFIER, "for", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "@@", 5, 1),
                makeToken(TokenType.EOF, "", 7, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testQuoteForDelim() {
        Token[] actualTokens = lexDelim("$$\"abc \\n\\t\\\" \"@@");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "$$", 0, 1),
                makeToken(TokenType.STRING, "\"abc \\n\\t\\\" \"", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "@@", 15, 1),
                makeToken(TokenType.EOF, "", 17, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testRawQuoteForDelim() {
        Token[] actualTokens = lexDelim("$$`abc\\n\\t\\\" `@@");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "$$", 0, 1),
                makeToken(TokenType.RAW_STRING, "`abc\\n\\t\\\" `", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "@@", 14, 1),
                makeToken(TokenType.EOF, "", 16, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testDelimiter() {
        Lexer lexer = new Lexer("{{hub .host hub}}",
                true, "{{hub", "hub}}",
                "/*", "*/");
        Token[] actualTokens = lexer.getTokens().toArray(new Token[0]);

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{hub", 0, 1),
                makeToken(TokenType.SPACE, " ", 5, 1),
                makeToken(TokenType.FIELD, ".host", 6, 1),
                makeToken(TokenType.SPACE, " ", 11, 1),
                makeToken(TokenType.RIGHT_DELIM, "hub}}", 12, 1),
                makeToken(TokenType.EOF, "", 17, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testDelimiterWithMarker() {
        Lexer lexer = new Lexer("{{- .x -}} {{- - .x - -}}",
                true, "{{- ", " -}}",
                "/*", "*/");
        Token[] actualTokens = lexer.getTokens().toArray(new Token[0]);

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{- ", 0, 1),
                makeToken(TokenType.FIELD, ".x", 4, 1),
                makeToken(TokenType.RIGHT_DELIM, " -}}", 6, 1),
                makeToken(TokenType.LEFT_DELIM, "{{- ", 11, 1),
                makeToken(TokenType.FIELD, ".x", 17, 1),
                makeToken(TokenType.RIGHT_DELIM, " -}}", 21, 1),
                makeToken(TokenType.EOF, "", 25, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testPunctuationPos() {
        Token[] actualTokens = lexDefault("{{,@%#}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.CHAR, ",", 2, 1),
                makeToken(TokenType.CHAR, "@", 3, 1),
                makeToken(TokenType.CHAR, "%", 4, 1),
                makeToken(TokenType.CHAR, "#", 5, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 6, 1),
                makeToken(TokenType.EOF, "", 8, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testPos() {
        Token[] actualTokens = lexDefault("0123{{hello}}xyz");

        Token[] expectedTokens = {
                makeToken(TokenType.TEXT, "0123", 0, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 4, 1),
                makeToken(TokenType.IDENTIFIER, "hello", 6, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 11, 1),
                makeToken(TokenType.TEXT, "xyz", 13, 1),
                makeToken(TokenType.EOF, "", 16, 1),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testTrimAfterPos() {
        Token[] actualTokens = lexDefault("{{x -}}\n{{y}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.IDENTIFIER, "x", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 5, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 8, 2),
                makeToken(TokenType.IDENTIFIER, "y", 10, 2),
                makeToken(TokenType.RIGHT_DELIM, "}}", 11, 2),
                makeToken(TokenType.EOF, "", 13, 2),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    @Test
    void testTrimBeforePos() {
        Token[] actualTokens = lexDefault("{{x}}\n{{- y}}");

        Token[] expectedTokens = {
                makeToken(TokenType.LEFT_DELIM, "{{", 0, 1),
                makeToken(TokenType.IDENTIFIER, "x", 2, 1),
                makeToken(TokenType.RIGHT_DELIM, "}}", 3, 1),
                makeToken(TokenType.LEFT_DELIM, "{{", 6, 2),
                makeToken(TokenType.IDENTIFIER, "y", 10, 2),
                makeToken(TokenType.RIGHT_DELIM, "}}", 11, 2),
                makeToken(TokenType.EOF, "", 13, 2),
        };

        assertTokens(expectedTokens, actualTokens);
    }

    private Token[] lexDefault(String input) {
        Lexer lexer = new Lexer(input, true);
        return lexer.getTokens().toArray(new Token[0]);
    }

    private Token[] lexDelim(String input) {
        Lexer lexer = new Lexer(input,
                true, "$$", "@@",
                "/*", "*/");
        return lexer.getTokens().toArray(new Token[0]);
    }

    private static Token makeToken(TokenType type, String value, int pos, int line) {
        return new Token(type, value, pos, line, 0);
    }

    private void assertTokens(Token[] expectedTokens, Token[] actualTokens) {
        assertEquals(expectedTokens.length, actualTokens.length,
                String.format("expected %d tokens, got %d tokens", expectedTokens.length, actualTokens.length));


        for (int i = 0; i < expectedTokens.length; i++) {
            Token expectedToken = expectedTokens[i];
            Token actualToken = actualTokens[i];
            assertToken(expectedToken, actualToken, i);
        }
    }

    private void assertToken(Token expected, Token actual, int index) {
        assertNotNull(actual, String.format("Error on token[%d], expected \"%s\" but got null",
                index, expected.value()));
        assertEquals(expected.type(), actual.type(), String.format("Error on token[%d], expected token type:\"%s\" but got \"%s\"",
                index, expected.type(), actual.type()));
        assertEquals(expected.value(), actual.value(), String.format("Error on token[%d], expected item value: '%s', value: '%s'",
                index, expected.value(), actual.value()));
        assertEquals(expected.pos(), actual.pos());
        assertEquals(expected.line(), actual.line());
    }

}