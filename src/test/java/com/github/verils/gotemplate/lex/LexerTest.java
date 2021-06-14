package com.github.verils.gotemplate.lex;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {

    private static final Item EOF_ITEM = makeItem(ItemType.EOF, "");
    private static final Item LEFT_DELIM_ITEM = makeItem(ItemType.LEFT_DELIM, "{{");
    private static final Item RIGHT_DELIM_ITEM = makeItem(ItemType.RIGHT_DELIM, "}}");
    public static final Item SPACE_ITEM = makeItem(ItemType.SPACE, " ");

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
                new Test("", new Item[]{makeItem(ItemType.EOF, "")}),
                new Test(" \t\n", new Item[]{
                        makeItem(ItemType.TEXT, " \t\n"),
                        EOF_ITEM
                }),
                new Test("now is the time", new Item[]{
                        makeItem(ItemType.TEXT, "now is the time"),
                        EOF_ITEM
                }),
                new Test("{{,@% }}", new Item[]{
                        LEFT_DELIM_ITEM,
                        makeItem(ItemType.CHAR, ","),
                        makeItem(ItemType.CHAR, "@"),
                        makeItem(ItemType.CHAR, "%"),
                        SPACE_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("hello-{{/* this is a comment */}}-world", new Item[]{
                        makeItem(ItemType.TEXT, "hello-"),
                        makeItem(ItemType.COMMENT, "/* this is a comment */"),
                        makeItem(ItemType.TEXT, "-world"),
                        EOF_ITEM
                })
        };

        for (Test test : tests) {
            Lexer lexer = new Lexer(test.input);
            for (Item item : test.items) {
                Item tgt = lexer.nextItem();
                assertNotNull(tgt, String.format("Input: '%s', Expected item type: '%s'", test.input, item.getType()));
                assertEquals(item.getType(), tgt.getType(), "Input: '" + test.input + "'");
            }
        }
    }

    private static Item makeItem(ItemType type, String value) {
        return new Item(type, value, 0);
    }
}