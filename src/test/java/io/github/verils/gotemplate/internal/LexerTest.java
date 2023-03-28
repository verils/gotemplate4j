package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.runtime.simple.lex.LexerViewer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {


    private static final Token BLOCK_TOKEN = mkItem(TokenType.BLOCK, "block");
    private static final Token DOT_TOKEN = mkItem(TokenType.DOT, ".");
    private static final Token EOF_TOKEN = mkItem(TokenType.EOF, "");
    private static final Token FOR_TOKEN = mkItem(TokenType.IDENTIFIER, "for");
    private static final Token NIL_TOKEN = mkItem(TokenType.NIL, "nil");
    private static final Token PIPE_TOKEN = mkItem(TokenType.PIPE, "|");
    private static final Token RANGE_TOKEN = mkItem(TokenType.RANGE, "range");
    private static final Token SPACE_TOKEN = mkItem(TokenType.SPACE, " ");

    private static final Token LEFT_DELIM_TOKEN = mkItem(TokenType.LEFT_DELIM, "{{");
    private static final Token RIGHT_DELIM_TOKEN = mkItem(TokenType.RIGHT_DELIM, "}}");

    private static final Token LEFT_PAREN_TOKEN = mkItem(TokenType.LEFT_PAREN, "(");
    private static final Token RIGHT_PAREN_TOKEN = mkItem(TokenType.RIGHT_PAREN, ")");


    @BeforeEach
    void setUp() {
    }

    @Test
    void test() {
        class Test {
            private final String input;
            private final Token[] items;

            public Test(String input, Token[] items) {
                this.input = input;
                this.items = items;
            }
        }

        Test[] tests = new Test[]{
                new Test("", new Token[]{mkItem(TokenType.EOF, "")}),
                new Test(" \t\n", new Token[]{
                        mkItem(TokenType.TEXT, " \t\n"),
                        EOF_TOKEN
                }),
                new Test("now is the time", new Token[]{
                        mkItem(TokenType.TEXT, "now is the time"),
                        EOF_TOKEN
                }),
                new Test("hello-{{/* this is a comment */}}-world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-"),
                        mkItem(TokenType.COMMENT, "/* this is a comment */"),
                        mkItem(TokenType.TEXT, "-world"),
                        EOF_TOKEN
                }),
                new Test("{{,@% }}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.CHAR, ","),
                        mkItem(TokenType.CHAR, "@"),
                        mkItem(TokenType.CHAR, "%"),
                        SPACE_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{((3))}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        LEFT_PAREN_TOKEN,
                        LEFT_PAREN_TOKEN,
                        mkItem(TokenType.NUMBER, "3"),
                        RIGHT_PAREN_TOKEN,
                        RIGHT_PAREN_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{for}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        FOR_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{block \"foo\" .}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        BLOCK_TOKEN,
                        SPACE_TOKEN,
                        mkItem(TokenType.STRING, "\"foo\""),
                        SPACE_TOKEN,
                        DOT_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{\"abc \\n\\t\\\" \"}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.STRING, "\"abc \\n\\t\\\" \""),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{" + ("`" + "abc\n\t\" " + "`") + "}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.STRING, "`" + "abc\n\t\" " + "`"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{" + "`now is{{\n}}the time`" + "}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.STRING, "`now is{{\n}}the time`"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{1 02 0x14 0X14 -7.2i 1e3 1E3 +1.2e-4 4.2i 1+2i 1_2 0x1.e_fp4 0X1.E_FP4}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.NUMBER, "1"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "02"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "0x14"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "0X14"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "-7.2i"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "1e3"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "1E3"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "+1.2e-4"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "4.2i"),
                        SPACE_TOKEN,
                        mkItem(TokenType.COMPLEX, "1+2i"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "1_2"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "0x1.e_fp4"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "0X1.E_FP4"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{'a' '\\n' '\\'' '\\\\' '\\u00FF' '\\xFF' '本'}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'a'"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'\\n'"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'\\''"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'\\\\'"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'\\u00FF'"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'\\xFF'"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR_CONSTANT, "'本'"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{true false}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.BOOL, "true"),
                        SPACE_TOKEN,
                        mkItem(TokenType.BOOL, "false"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{.}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        DOT_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{nil}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        NIL_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{.x . .2 .x.y.z}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.FIELD, ".x"),
                        SPACE_TOKEN,
                        DOT_TOKEN,
                        SPACE_TOKEN,
                        mkItem(TokenType.FIELD, ".2"),
                        SPACE_TOKEN,
                        mkItem(TokenType.FIELD, ".x"),
                        mkItem(TokenType.FIELD, ".y"),
                        mkItem(TokenType.FIELD, ".z"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{range if else end with}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.RANGE, "range"),
                        SPACE_TOKEN,
                        mkItem(TokenType.IF, "if"),
                        SPACE_TOKEN,
                        mkItem(TokenType.ELSE, "else"),
                        SPACE_TOKEN,
                        mkItem(TokenType.END, "end"),
                        SPACE_TOKEN,
                        mkItem(TokenType.WITH, "with"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{$c := printf $ $hello $23 $ $var.Field .Method}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.VARIABLE, "$c"),
                        SPACE_TOKEN,
                        mkItem(TokenType.DECLARE, ":="),
                        SPACE_TOKEN,
                        mkItem(TokenType.IDENTIFIER, "printf"),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$"),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$hello"),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$23"),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$"),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$var"),
                        mkItem(TokenType.FIELD, ".Field"),
                        SPACE_TOKEN,
                        mkItem(TokenType.FIELD, ".Method"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{$x 23}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.VARIABLE, "$x"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "23"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("intro {{echo hi 1.2 |noargs|args 1 \"hi\"}} outro", new Token[]{
                        mkItem(TokenType.TEXT, "intro "),
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.IDENTIFIER, "echo"),
                        SPACE_TOKEN,
                        mkItem(TokenType.IDENTIFIER, "hi"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "1.2"),
                        SPACE_TOKEN,
                        PIPE_TOKEN,
                        mkItem(TokenType.IDENTIFIER, "noargs"),
                        PIPE_TOKEN,
                        mkItem(TokenType.IDENTIFIER, "args"),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "1"),
                        SPACE_TOKEN,
                        mkItem(TokenType.STRING, "\"hi\""),
                        RIGHT_DELIM_TOKEN,
                        mkItem(TokenType.TEXT, " outro"),
                        EOF_TOKEN
                }),
                new Test("{{$v := 3}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.VARIABLE, "$v"),
                        SPACE_TOKEN,
                        mkItem(TokenType.DECLARE, ":="),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "3"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{$v , $w := 3}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.VARIABLE, "$v"),
                        SPACE_TOKEN,
                        mkItem(TokenType.CHAR, ","),
                        SPACE_TOKEN,
                        mkItem(TokenType.VARIABLE, "$w"),
                        SPACE_TOKEN,
                        mkItem(TokenType.DECLARE, ":="),
                        SPACE_TOKEN,
                        mkItem(TokenType.NUMBER, "3"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("{{(.X).Y}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        LEFT_PAREN_TOKEN,
                        mkItem(TokenType.FIELD, ".X"),
                        RIGHT_PAREN_TOKEN,
                        mkItem(TokenType.FIELD, ".Y"),
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("hello- {{- 3 -}} -world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-"),
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.NUMBER, "3"),
                        RIGHT_DELIM_TOKEN,
                        mkItem(TokenType.TEXT, "-world"),
                        EOF_TOKEN
                }),
                new Test("hello- {{- /* hello */ -}} -world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-"),
                        mkItem(TokenType.COMMENT, "/* hello */"),
                        mkItem(TokenType.TEXT, "-world"),
                        EOF_TOKEN
                }),
                new Test("#{{\1}}", new Token[]{
                        mkItem(TokenType.TEXT, "#"),
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "bad character in action: \1"),
                }),
                new Test("{{", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "unclosed action"),
                }),
                new Test("{{range", new Token[]{
                        LEFT_DELIM_TOKEN,
                        RANGE_TOKEN,
                        mkItem(TokenType.ERROR, "unclosed action"),
                }),
                new Test("{{\"\n\"}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "unterminated quoted string"),
                }),
                new Test("{{`xx}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "unclosed raw quote"),
                }),
                new Test("{{'\n}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "unclosed character constant"),
                }),
                new Test("{{3k}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.ERROR, "bad number: 3k"),
                }),
                new Test("{{(3}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        LEFT_PAREN_TOKEN,
                        mkItem(TokenType.NUMBER, "3"),
                        mkItem(TokenType.ERROR, "unclosed left paren"),
                }),
                new Test("{{|||||}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        PIPE_TOKEN,
                        PIPE_TOKEN,
                        PIPE_TOKEN,
                        PIPE_TOKEN,
                        PIPE_TOKEN,
                        RIGHT_DELIM_TOKEN,
                        EOF_TOKEN
                }),
                new Test("hello-{{/*/}}-world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-"),
                        mkItem(TokenType.ERROR, "unclosed comment"),
                }),
                new Test("hello-{{/* */ }}-world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-"),
                        mkItem(TokenType.ERROR, "comment closed leaving delim still open"),
                }),
                new Test("hello-{.}}-world", new Token[]{
                        mkItem(TokenType.TEXT, "hello-{.}}-world"),
                        EOF_TOKEN
                }),

                // Additional cases
                new Test("{{/* this is a comment */}}", new Token[]{
                        mkItem(TokenType.COMMENT, "/* this is a comment */"),
                        EOF_TOKEN
                }),
                new Test("{{$v : 3}}", new Token[]{
                        LEFT_DELIM_TOKEN,
                        mkItem(TokenType.VARIABLE, "$v"),
                        SPACE_TOKEN,
                        mkItem(TokenType.ERROR, "expected :="),
                })
        };

        for (Test test : tests) {
            Lexer lexer = new Lexer(test.input, true);
            LexerViewer lexerViewer = lexer.getViewer();
            for (Token token : test.items) {
                Token tgt = lexerViewer.nextItem();
                assertNotNull(tgt, String.format("Input: '%s', Expected item type: '%s'", test.input, token.type()));
                assertEquals(token.type(), tgt.type(), String.format("Input: '%s', Expected item value: '%s', value: '%s'", test.input, token.value(), tgt.value()));
                assertEquals(token.value(), tgt.value(), String.format("Input: '%s'", test.input));
            }
        }
    }

    private static Token mkItem(TokenType type, String value) {
        return new Token(type, value, 0);
    }
}