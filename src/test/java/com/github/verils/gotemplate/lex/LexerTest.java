package com.github.verils.gotemplate.lex;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {


    private static final Item BLOCK_ITEM = mkItem(ItemType.BLOCK, "block");
    private static final Item DOT_ITEM = mkItem(ItemType.DOT, ".");
    private static final Item EOF_ITEM = mkItem(ItemType.EOF, "");
    private static final Item FOR_ITEM = mkItem(ItemType.IDENTIFIER, "for");
    private static final Item NIL_ITEM = mkItem(ItemType.NIL, "nil");
    private static final Item PIPE_ITEM = mkItem(ItemType.PIPE, "|");
    private static final Item RANGE_ITEM = mkItem(ItemType.RANGE, "range");
    private static final Item SPACE_ITEM = mkItem(ItemType.SPACE, " ");

    private static final Item LEFT_DELIM_ITEM = mkItem(ItemType.LEFT_DELIM, "{{");
    private static final Item RIGHT_DELIM_ITEM = mkItem(ItemType.RIGHT_DELIM, "}}");

    private static final Item LEFT_PAREN_ITEM = mkItem(ItemType.LEFT_PAREN, "(");
    private static final Item RIGHT_PAREN_ITEM = mkItem(ItemType.RIGHT_PAREN, ")");


    @BeforeEach
    void setUp() {
    }

    @Test
    void test() {
        @Data
        class Test {
            private final String input;
            private final Item[] items;
        }

        var tests = new Test[]{
                new Test("", new Item[]{mkItem(ItemType.EOF, "")}),
                new Test(" \t\n", new Item[]{
                        mkItem(ItemType.TEXT, " \t\n"),
                        EOF_ITEM
                }),
                new Test("now is the time", new Item[]{
                        mkItem(ItemType.TEXT, "now is the time"),
                        EOF_ITEM
                }),
                new Test("hello-{{/* this is a comment */}}-world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-"),
                        mkItem(ItemType.COMMENT, "/* this is a comment */"),
                        mkItem(ItemType.TEXT, "-world"),
                        EOF_ITEM
                }),
                new Test("{{,@% }}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.CHAR, ","),
                        mkItem(ItemType.CHAR, "@"),
                        mkItem(ItemType.CHAR, "%"),
                        SPACE_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{((3))}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        LEFT_PAREN_ITEM,
                        LEFT_PAREN_ITEM,
                        mkItem(ItemType.NUMBER, "3"),
                        RIGHT_PAREN_ITEM,
                        RIGHT_PAREN_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{for}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        FOR_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{block \"foo\" .}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        BLOCK_ITEM,
                        SPACE_ITEM,
                        mkItem(ItemType.STRING, "\"foo\""),
                        SPACE_ITEM,
                        DOT_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{\"abc \\n\\t\\\" \"}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.STRING, "\"abc \\n\\t\\\" \""),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{" + ("`" + "abc\n\t\" " + "`") + "}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.STRING, "`" + "abc\n\t\" " + "`"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{" + "`now is{{\n}}the time`" + "}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.STRING, "`now is{{\n}}the time`"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{1 02 0x14 0X14 -7.2i 1e3 1E3 +1.2e-4 4.2i 1+2i 1_2 0x1.e_fp4 0X1.E_FP4}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.NUMBER, "1"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "02"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "0x14"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "0X14"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "-7.2i"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "1e3"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "1E3"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "+1.2e-4"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "4.2i"),
                        SPACE_ITEM,
                        mkItem(ItemType.COMPLEX, "1+2i"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "1_2"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "0x1.e_fp4"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "0X1.E_FP4"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{'a' '\\n' '\\'' '\\\\' '\\u00FF' '\\xFF' '本'}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'a'"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'\\n'"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'\\''"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'\\\\'"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'\\u00FF'"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'\\xFF'"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR_CONSTANT, "'本'"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{true false}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.BOOL, "true"),
                        SPACE_ITEM,
                        mkItem(ItemType.BOOL, "false"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{.}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        DOT_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{nil}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        NIL_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{.x . .2 .x.y.z}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.FIELD, ".x"),
                        SPACE_ITEM,
                        DOT_ITEM,
                        SPACE_ITEM,
                        mkItem(ItemType.FIELD, ".2"),
                        SPACE_ITEM,
                        mkItem(ItemType.FIELD, ".x"),
                        mkItem(ItemType.FIELD, ".y"),
                        mkItem(ItemType.FIELD, ".z"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{range if else end with}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.RANGE, "range"),
                        SPACE_ITEM,
                        mkItem(ItemType.IF, "if"),
                        SPACE_ITEM,
                        mkItem(ItemType.ELSE, "else"),
                        SPACE_ITEM,
                        mkItem(ItemType.END, "end"),
                        SPACE_ITEM,
                        mkItem(ItemType.WITH, "with"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{$c := printf $ $hello $23 $ $var.Field .Method}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.VARIABLE, "$c"),
                        SPACE_ITEM,
                        mkItem(ItemType.DECLARE, ":="),
                        SPACE_ITEM,
                        mkItem(ItemType.IDENTIFIER, "printf"),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$"),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$hello"),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$23"),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$"),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$var"),
                        mkItem(ItemType.FIELD, ".Field"),
                        SPACE_ITEM,
                        mkItem(ItemType.FIELD, ".Method"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{$x 23}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.VARIABLE, "$x"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "23"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("intro {{echo hi 1.2 |noargs|args 1 \"hi\"}} outro", new Item[]{
                        mkItem(ItemType.TEXT, "intro "),
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.IDENTIFIER, "echo"),
                        SPACE_ITEM,
                        mkItem(ItemType.IDENTIFIER, "hi"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "1.2"),
                        SPACE_ITEM,
                        PIPE_ITEM,
                        mkItem(ItemType.IDENTIFIER, "noargs"),
                        PIPE_ITEM,
                        mkItem(ItemType.IDENTIFIER, "args"),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "1"),
                        SPACE_ITEM,
                        mkItem(ItemType.STRING, "\"hi\""),
                        RIGHT_DELIM_ITEM,
                        mkItem(ItemType.TEXT, " outro"),
                        EOF_ITEM
                }),
                new Test("{{$v := 3}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.VARIABLE, "$v"),
                        SPACE_ITEM,
                        mkItem(ItemType.DECLARE, ":="),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "3"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{$v , $w := 3}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.VARIABLE, "$v"),
                        SPACE_ITEM,
                        mkItem(ItemType.CHAR, ","),
                        SPACE_ITEM,
                        mkItem(ItemType.VARIABLE, "$w"),
                        SPACE_ITEM,
                        mkItem(ItemType.DECLARE, ":="),
                        SPACE_ITEM,
                        mkItem(ItemType.NUMBER, "3"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("{{(.X).Y}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        LEFT_PAREN_ITEM,
                        mkItem(ItemType.FIELD, ".X"),
                        RIGHT_PAREN_ITEM,
                        mkItem(ItemType.FIELD, ".Y"),
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("hello- {{- 3 -}} -world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-"),
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.NUMBER, "3"),
                        RIGHT_DELIM_ITEM,
                        mkItem(ItemType.TEXT, "-world"),
                        EOF_ITEM
                }),
                new Test("hello- {{- /* hello */ -}} -world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-"),
                        mkItem(ItemType.COMMENT, "/* hello */"),
                        mkItem(ItemType.TEXT, " -world"),
                        EOF_ITEM
                }),
                new Test("#{{\1}}", new Item[]{
                        mkItem(ItemType.TEXT, "#"),
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "bad character in action: \1"),
                }),
                new Test("{{", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "unclosed action"),
                }),
                new Test("{{range", new Item[]{
                        LEFT_DELIM_ITEM,
                        RANGE_ITEM,
                        mkItem(ItemType.ERROR, "unclosed action"),
                }),
                new Test("{{\"\n\"}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "unclosed quote"),
                }),
                new Test("{{`xx}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "unclosed raw quote"),
                }),
                new Test("{{'\n}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "unclosed character constant"),
                }),
                new Test("{{3k}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.ERROR, "bad number: 3k"),
                }),
                new Test("{{(3}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        LEFT_PAREN_ITEM,
                        mkItem(ItemType.NUMBER, "3"),
                        mkItem(ItemType.ERROR, "unclosed left paren"),
                }),
                new Test("{{|||||}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        PIPE_ITEM,
                        PIPE_ITEM,
                        PIPE_ITEM,
                        PIPE_ITEM,
                        PIPE_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("hello-{{/*/}}-world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-"),
                        mkItem(ItemType.ERROR, "unclosed comment"),
                }),
                new Test("hello-{{/* */ }}-world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-"),
                        mkItem(ItemType.ERROR, "comment closed leaving delim still open"),
                }),
                new Test("hello-{.}}-world", new Item[]{
                        mkItem(ItemType.TEXT, "hello-{.}}-world"),
                        EOF_ITEM
                }),

                // Additional cases
                new Test("{{/* this is a comment */}}", new Item[]{
                        mkItem(ItemType.COMMENT, "/* this is a comment */"),
                        EOF_ITEM
                }),
                new Test("{{$v : 3}}", new Item[]{
                        LEFT_DELIM_ITEM,
                        mkItem(ItemType.VARIABLE, "$v"),
                        SPACE_ITEM,
                        mkItem(ItemType.ERROR, "expected :="),
                })
        };

        for (Test test : tests) {
            Lexer lexer = new Lexer(test.input, true);
            for (Item item : test.items) {
                Item tgt = lexer.nextItem();
                assertNotNull(tgt, String.format("Input: '%s', Expected item type: '%s'", test.input, item.type()));
                assertEquals(item.type(), tgt.type(), String.format("Input: '%s', Expected item value: '%s', value: '%s'", test.input, item.value(), tgt.value()));
                assertEquals(item.value(), tgt.value(), String.format("Input: '%s'", test.input));
            }
        }
    }

    private static Item mkItem(ItemType type, String value) {
        return new Item(type, value, 0);
    }
}