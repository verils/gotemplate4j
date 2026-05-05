package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.LexerTestSupport.assertDefaultTokens;
import static io.github.verils.gotemplate.internal.LexerTestSupport.token;

class LexerLiteralTest {

    @Test
    void quotedString() {
        assertDefaultTokens("{{\"abc \\n\\t\\\" \"}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.STRING, "\"abc \\n\\t\\\" \"", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 15, 1, 16),
                token(TokenType.EOF, "", 17, 1, 18));
    }

    @Test
    void rawQuotedString() {
        assertDefaultTokens("{{`abc\\n\\t\\\" `}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.RAW_STRING, "`abc\\n\\t\\\" `", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 14, 1, 15),
                token(TokenType.EOF, "", 16, 1, 17));
    }

    @Test
    void rawQuotedStringMayContainDelimiters() {
        assertDefaultTokens("{{`now is{{\\n}}the time`}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.RAW_STRING, "`now is{{\\n}}the time`", 2, 1, 3),
                token(TokenType.RIGHT_DELIM, "}}", 24, 1, 25),
                token(TokenType.EOF, "", 26, 1, 27));
    }

    @Test
    void numberForms() {
        assertDefaultTokens("{{1 02 0x14 0X14 -7.2i 1e3 1E3 +1.2e-4 4.2i 1+2i 1_2 0x1.e_fp4 0X1.E_FP4}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 1),
                token(TokenType.NUMBER, "1", 2, 1, 3),
                token(TokenType.SPACE, " ", 3, 1, 4),
                token(TokenType.NUMBER, "02", 4, 1, 5),
                token(TokenType.SPACE, " ", 6, 1, 7),
                token(TokenType.NUMBER, "0x14", 7, 1, 8),
                token(TokenType.SPACE, " ", 11, 1, 12),
                token(TokenType.NUMBER, "0X14", 12, 1, 13),
                token(TokenType.SPACE, " ", 16, 1, 17),
                token(TokenType.NUMBER, "-7.2i", 17, 1, 18),
                token(TokenType.SPACE, " ", 22, 1, 23),
                token(TokenType.NUMBER, "1e3", 23, 1, 24),
                token(TokenType.SPACE, " ", 26, 1, 27),
                token(TokenType.NUMBER, "1E3", 27, 1, 28),
                token(TokenType.SPACE, " ", 30, 1, 31),
                token(TokenType.NUMBER, "+1.2e-4", 31, 1, 32),
                token(TokenType.SPACE, " ", 38, 1, 39),
                token(TokenType.NUMBER, "4.2i", 39, 1, 40),
                token(TokenType.SPACE, " ", 43, 1, 44),
                token(TokenType.COMPLEX, "1+2i", 44, 1, 45),
                token(TokenType.SPACE, " ", 48, 1, 49),
                token(TokenType.NUMBER, "1_2", 49, 1, 50),
                token(TokenType.SPACE, " ", 52, 1, 53),
                token(TokenType.NUMBER, "0x1.e_fp4", 53, 1, 54),
                token(TokenType.SPACE, " ", 62, 1, 63),
                token(TokenType.NUMBER, "0X1.E_FP4", 63, 1, 64),
                token(TokenType.RIGHT_DELIM, "}}", 72, 1, 73),
                token(TokenType.EOF, "", 74, 1, 75));
    }

    @Test
    void characterConstants() {
        assertDefaultTokens("{{'a' '\\n' '\\'' '\\\\' '\\u00FF' '\\xFF' '本'}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'a'", 2, 1, 0),
                token(TokenType.SPACE, " ", 5, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'\\n'", 6, 1, 0),
                token(TokenType.SPACE, " ", 10, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'\\''", 11, 1, 0),
                token(TokenType.SPACE, " ", 15, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'\\\\'", 16, 1, 0),
                token(TokenType.SPACE, " ", 20, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'\\u00FF'", 21, 1, 0),
                token(TokenType.SPACE, " ", 29, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'\\xFF'", 30, 1, 0),
                token(TokenType.SPACE, " ", 36, 1, 0),
                token(TokenType.CHAR_CONSTANT, "'本'", 37, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 40, 1, 0),
                token(TokenType.EOF, "", 42, 1, 0));
    }

    @Test
    void booleanConstants() {
        assertDefaultTokens("{{true false}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.BOOL, "true", 2, 1, 0),
                token(TokenType.SPACE, " ", 6, 1, 0),
                token(TokenType.BOOL, "false", 7, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 12, 1, 0),
                token(TokenType.EOF, "", 14, 1, 0));
    }

    @Test
    void nilConstant() {
        assertDefaultTokens("{{nil}}",
                token(TokenType.LEFT_DELIM, "{{", 0, 1, 0),
                token(TokenType.NIL, "nil", 2, 1, 0),
                token(TokenType.RIGHT_DELIM, "}}", 5, 1, 0),
                token(TokenType.EOF, "", 7, 1, 0));
    }
}
