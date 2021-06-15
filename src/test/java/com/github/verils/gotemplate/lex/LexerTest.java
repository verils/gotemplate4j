package com.github.verils.gotemplate.lex;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {


    private static final Item DOT_ITEM = mkItem(ItemType.DOT, ".");
    private static final Item BLOCK_ITEM = mkItem(ItemType.BLOCK, "block");
    private static final Item EOF_ITEM = mkItem(ItemType.EOF, "");
    private static final Item FOR_ITEM = mkItem(ItemType.IDENTIFIER, "for");

    private static final Item LEFT_DELIM_ITEM = mkItem(ItemType.LEFT_DELIM, "{{");
    private static final Item RIGHT_DELIM_ITEM = mkItem(ItemType.RIGHT_DELIM, "}}");

    private static final Item LEFT_PAREN_ITEM = mkItem(ItemType.LEFT_PAREN, "(");
    private static final Item RIGHT_PAREN_ITEM = mkItem(ItemType.RIGHT_PAREN, ")");

    public static final Item SPACE_ITEM = mkItem(ItemType.SPACE, " ");

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
                })
        };

        for (Test test : tests) {
            Lexer lexer = new Lexer(test.input);
            for (Item item : test.items) {
                Item tgt = lexer.nextItem();
                assertNotNull(tgt, String.format("Input: '%s', Expected item type: '%s'", test.input, item.getType()));
                assertEquals(item.getType(), tgt.getType(), String.format("Input: '%s', Expected item value: '%s', value: '%s'", test.input, item.getValue(), tgt.getValue()));
                assertEquals(item.getValue(), tgt.getValue(), String.format("Input: '%s'", test.input));
            }
        }
    }

    private static Item mkItem(ItemType type, String value) {
        return new Item(type, value, 0);
    }
}