package com.github.verils.gotemplate.lex;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LexerTest {

    private static final Item EOF_ITEM = new Item(ItemType.EOF, "", 0);
    private static final Item LEFT_DELIM_ITEM = new Item(ItemType.LEFT_DELIM, "{{", 0);
    private static final Item RIGHT_DELIM_ITEM = new Item(ItemType.RIGHT_DELIM, "}}", 0);
    public static final Item SPACE_ITEM = new Item(ItemType.SPACE, " ", 0);

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
                new Test("", new Item[]{new Item(ItemType.EOF, "", 0)}),
                new Test(" \t\n", new Item[]{
                        new Item(ItemType.TEXT, " \t\n", 0),
                        EOF_ITEM
                }),
                new Test("now is the time", new Item[]{
                        new Item(ItemType.TEXT, "now is the time", 0),
                        EOF_ITEM
                }),
                new Test("{{,@% }}", new Item[]{
                        LEFT_DELIM_ITEM,
                        new Item(ItemType.CHAR, ",", 0),
                        new Item(ItemType.CHAR, "@", 0),
                        new Item(ItemType.CHAR, "%", 0),
                        SPACE_ITEM,
                        RIGHT_DELIM_ITEM,
                        EOF_ITEM
                }),
                new Test("hello-{{/* this is a comment */}}-world", new Item[]{
                        new Item(ItemType.TEXT, "hello-", 0),
                        new Item(ItemType.COMMENT, "/* this is a comment */", 0),
                        new Item(ItemType.TEXT, "-world", 0),
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
}