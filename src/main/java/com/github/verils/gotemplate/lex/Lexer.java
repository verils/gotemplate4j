package com.github.verils.gotemplate.lex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private static final Map<String, ItemType> KEY = new LinkedHashMap<>();

    static {
        KEY.put(".", ItemType.DOT);
        KEY.put("block", ItemType.BLOCK);
        KEY.put("define", ItemType.DEFINE);
        KEY.put("else", ItemType.ELSE);
        KEY.put("end", ItemType.END);
        KEY.put("if", ItemType.IF);
        KEY.put("nil", ItemType.NIL);
        KEY.put("range", ItemType.RANGE);
        KEY.put("template", ItemType.TEMPLATE);
        KEY.put("with", ItemType.WITH);
    }

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

        end = start + 1;

        char ch = input.charAt(start);
        if (Char.isSpace(ch)) {
            parseSpace();
            return;
        } else if (ch == '"') {
            parseQuote();
            return;
        } else if (ch == '+' || ch == '-' || Char.isNumeric(ch)) {
            parseNumber();
            return;
        } else if (ch == '.') {
            parseDot();
            return;
        } else if (Char.isAlphabetic(ch)) {
            parseIdentifier();
            return;
        } else if (ch == '(') {
            addItem(ItemType.LEFT_PAREN);
            parenDepth++;
        } else if (ch == ')') {
            addItem(ItemType.RIGHT_PAREN);
            parenDepth--;
        } else {
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

        parseAction();
    }

    private void parseQuote() {
        end = start + 1;
        while (true) {
            char ch = input.charAt(end);
            end++;
            if (ch == '"') {
                break;
            }
        }

        addItem(ItemType.STRING);
        start = end;

        parseAction();
    }

    private void parseNumber() {
        addItem(ItemType.NUMBER);
        start = end;

        parseAction();
    }

    private void parseDot() {
        char ch = input.charAt(end);
        if (ch < '0' || '9' < ch) {
            parseField();
        }
    }

    private void parseField() {
        if (atTerminator()) {
            addItem(ItemType.DOT);
            start = end;

            parseAction();
        }
    }

    private void parseIdentifier() {
        StringBuilder sb = new StringBuilder();

        end = start;
        while (true) {
            char ch = input.charAt(end);
            if (!Char.isAlphabetic(ch)) {
                break;
            }

            sb.append(ch);
            end++;
        }

        String word = sb.toString();

        if (KEY.containsKey(word)) {
            addItem(KEY.get(word));
        } else if (word.charAt(0) == '.') {
            addItem(ItemType.FIELD);
        } else if ("true".equals(word) || "false".equals(word)) {
            addItem(ItemType.BOOL);
        } else {
            addItem(ItemType.IDENTIFIER);
        }

        start = end;

        parseAction();
    }

    private boolean atTerminator() {
        char ch = input.charAt(end);
        if (Char.isSpace(ch) ||
                ch == '\0' ||
                ch == '.' ||
                ch == ',' ||
                ch == '|' ||
                ch == ':' ||
                ch == '(' ||
                ch == ')') {
            return true;
        }

        int n = input.indexOf(rightDelim, end);
        return n == end;
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

}
