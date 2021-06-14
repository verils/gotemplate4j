package com.github.verils.gotemplate.lex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lexer {

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private final String input;

    private final String leftDelim;
    private final String rightDelim;

    private final String leftComment;
    private final String rightComment;

    /* 解析过程种的标记位 */

    private int start = 0;
    private int end = 0;

    private int parenDepth = 0;


    private final List<Item> items = new ArrayList<>(32);
    private int index;

    public Lexer(String input) {
        if (input == null) {
            throw new NullPointerException();
        }

        this.input = input;

        this.leftDelim = DEFAULT_LEFT_DELIM;
        this.rightDelim = DEFAULT_RIGHT_DELIM;

        this.leftComment = DEFAULT_LEFT_COMMENT;
        this.rightComment = DEFAULT_RIGHT_COMMENT;

        parseText();
    }

    private void parseText() {
        int n = input.indexOf(leftDelim, start);
        boolean hasLeftDelim = n >= 0;
        if (hasLeftDelim) {
            end = n;
            if (end > start) {
                addItem(ItemType.TEXT);
            }
            start = end;

            parseLeftDelim();
            return;
        }

        if (input.length() > start) {
            end = input.length();
            addItem(ItemType.TEXT);
        }

        start = end;
        addItem(ItemType.EOF);
    }

    private void parseLeftDelim() {
        int n = input.indexOf(leftComment, start);
        boolean hasComment = n >= 0;
        if (hasComment) {
            end = n;
            start = end;

            parseComment();

            n = input.indexOf(rightDelim, start);
            if (n < 0) {
                throw new SyntaxException("Unclosed delim");
            }
            end = n + rightDelim.length();
            start = end;

            parseText();

            return;
        }

        end += leftDelim.length();
        addItem(ItemType.LEFT_DELIM);
        start = end;

        parseAction();
    }

    private void parseComment() {
        int n = input.indexOf(rightComment, start);
        if (n < 0) {
            throw new SyntaxException("Unclosed comment");
        }

        end = n + rightComment.length();
        addItem(ItemType.COMMENT);
        start = end;
    }

    private void parseAction() {
        int n = input.indexOf(rightDelim, start);
        if (n < 0) {
            throw new SyntaxException("Unclosed delim");
        }

        if (start == n) {
            parseRightDelim();
            return;
        }

        char ch = input.charAt(start);
        end++;

        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                parseSpace();
                break;
            case '+':
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                addItem(ItemType.NUMBER);
                break;
            case '(':
                addItem(ItemType.LEFT_PAREN);
                parenDepth++;
                break;
            case ')':
                addItem(ItemType.RIGHT_PAREN);
                parenDepth--;
                break;
            default:
                addItem(ItemType.CHAR);
        }

        start = end;

        parseAction();
    }

    private void parseRightDelim() {
        end = start + rightDelim.length();
        addItem(ItemType.RIGHT_DELIM);
        start = end;

        parseText();
    }

    private void parseSpace() {
        addItem(ItemType.SPACE);
        start = end;
    }

    private void addItem(ItemType type) {
        String value = input.substring(start, end);
        items.add(new Item(type, value, start));
    }

    public Item nextItem() {
        if (index < items.size()) {
            return items.get(index++);
        }
        return null;
    }

    public List<Item> getItems() {
        return Collections.emptyList();
    }
}
