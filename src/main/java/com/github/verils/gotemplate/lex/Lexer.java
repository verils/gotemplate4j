package com.github.verils.gotemplate.lex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private static final int DECIMAL_SCALE = 10;
    private static final int HEX_SCALE = 16;
    private static final int OCTET_SCALE = 8;
    private static final int BINARY_SCALE = 2;

    private static final String DEFAULT_LEFT_DELIM = "{{";
    private static final String DEFAULT_RIGHT_DELIM = "}}";
    private static final String DEFAULT_LEFT_COMMENT = "/*";
    private static final String DEFAULT_RIGHT_COMMENT = "*/";

    private static final Map<String, ItemType> KEY = new LinkedHashMap<>();
    private static final String DECIMAL_DIGITS = "0123456789_";

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
    private int pos = 0;

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
            pos = n;
            if (pos > start) {
                addItem(ItemType.TEXT);
            }
            updateStart();

            parseLeftDelim();
            return;
        }

        if (input.length() > start) {
            pos = input.length();
            addItem(ItemType.TEXT);
        }

        updateStart();
        addItem(ItemType.EOF);
    }

    private void parseLeftDelim() {
        int n = input.indexOf(leftComment, start);
        boolean hasComment = n >= 0;
        if (hasComment) {
            pos = n;
            updateStart();

            parseComment();

            n = input.indexOf(rightDelim, start);
            if (n < 0) {
                throw new SyntaxException("Unclosed delim");
            }
            pos = n + rightDelim.length();
            updateStart();

            parseText();

            return;
        }

        pos += leftDelim.length();
        addItem(ItemType.LEFT_DELIM);
        updateStart();

        parseInsideAction();
    }

    private void parseComment() {
        int n = input.indexOf(rightComment, start);
        if (n < 0) {
            throw new SyntaxException("Unclosed comment");
        }

        pos = n + rightComment.length();
        addItem(ItemType.COMMENT);
        updateStart();
    }

    private void parseInsideAction() {
        int n = input.indexOf(rightDelim, start);
        if (n < 0) {
            throw new SyntaxException("Unclosed delim");
        }

        if (start == n) {
            parseRightDelim();
            return;
        }

        char ch = nextChar();
        if (Char.isSpace(ch)) {
            parseSpace();
            return;
        } else if (ch == '"') {
            parseQuote();
            return;
        } else if (ch == '`') {
            parseRawQuote();
            return;
        } else if (ch == '.') {
            parseDot();
            return;
        } else if (Char.isValid(ch, "+-") || Char.isNumeric(ch)) {
            resetPos();
            parseNumber();
            return;
        } else if (Char.isAlphabetic(ch)) {
            resetPos();
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

        updateStart();
        parseInsideAction();
    }

    private void parseRightDelim() {
        pos = start + rightDelim.length();
        addItem(ItemType.RIGHT_DELIM);
        updateStart();

        parseText();
    }

    private void parseSpace() {
        addItem(ItemType.SPACE);
        updateStart();

        parseInsideAction();
    }

    private void parseQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();

                if (ch != Char.EOF && ch != Char.NEW_LINE) {
                    continue;
                }
            }

            if (ch == Char.EOF || ch == Char.NEW_LINE) {
                throw new SyntaxException("unclosed quote");
            }

            if (ch == '"') {
                break;
            }
        }

        addItem(ItemType.STRING);
        updateStart();

        parseInsideAction();
    }

    private void parseRawQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == Char.EOF) {
                throw new SyntaxException("unclosed quote");
            }

            if (ch == '`') {
                break;
            }
        }

        addItem(ItemType.STRING);
        updateStart();

        parseInsideAction();
    }

    private void parseDot() {
        char ch = getChar();
        if (ch < '0' || '9' < ch) {
            parseField();
        }
    }

    private void parseField() {
        if (atTerminator()) {
            addItem(ItemType.DOT);
            updateStart();

            parseInsideAction();
        }
    }

    private void parseNumber() {
        lookForNumber();

        char ch = getChar();
        if (Char.isAlphabetic(ch)) {
            throw new SyntaxException("bad number: " + getValue());
        }

        if (Char.isValid(ch, "+-")) {
            lookForNumber();

            addItem(ItemType.COMPLEX);
        } else {
            addItem(ItemType.NUMBER);
        }

        updateStart();

        parseInsideAction();
    }

    private void lookForNumber() {
        goIf("+-");

        String digits = DECIMAL_DIGITS;

        char ch = nextChar();
        if (ch == '0') {
            ch = nextChar();
            if (Char.isValid(ch, "xX")) {
                digits = "0123456789abcdefABCDEF_";
            } else if (Char.isValid(ch, "oO")) {
                digits = "01234567_";
            } else if (Char.isValid(ch, "bB")) {
                digits = "01_";
            }
        }

        ch = goUntilNot(digits);
        if (ch == '.') {
            pos++;
            ch = goUntilNot(digits);
        }

        if (digits.length() == 10 + 1 && Char.isValid(ch, "eE")) {
            pos++;

            goIf("+-");
            ch = goUntilNot(DECIMAL_DIGITS);
        }

        if (digits.length() == 16 + 6 + 1 && Char.isValid(ch, "pP")) {
            pos++;

            goIf("+-");
            ch = goUntilNot(DECIMAL_DIGITS);
        }

        goIf("i");
    }

    private void parseIdentifier() {
        while (true) {
            char ch = getChar();
            if (!Char.isAlphabetic(ch)) {
                break;
            }
            pos++;
        }
        String word = getValue();

        if (KEY.containsKey(word)) {
            addItem(KEY.get(word));
        } else if (word.charAt(0) == '.') {
            addItem(ItemType.FIELD);
        } else if ("true".equals(word) || "false".equals(word)) {
            addItem(ItemType.BOOL);
        } else {
            addItem(ItemType.IDENTIFIER);
        }

        updateStart();

        parseInsideAction();
    }

    private void updateStart() {
        start = pos;
    }

    private void resetPos() {
        pos = start;
    }

    private void goIf(CharSequence want) {
        char ch = getChar();
        if (Char.isValid(ch, want)) {
            pos++;
        }
    }

    private char goUntilNot(String until) {
        while (true) {
            char ch = nextChar();
            if (!Char.isValid(ch, until)) {
                pos--;
                return ch;
            }
        }
    }

    private char nextChar() {
        char ch = input.charAt(pos);
        pos++;
        return ch;
    }

    private char getChar() {
        char ch = nextChar();
        pos--;
        return ch;
    }

    private boolean atTerminator() {
        char ch = getChar();
        if (Char.isSpace(ch) || Char.isValid(ch, Char.EOF + ".,|:()")) {
            return true;
        }

        int n = input.indexOf(rightDelim, pos);
        return n == pos;
    }

    private String getValue() {
        return input.substring(start, pos);
    }

    private void addItem(ItemType type) {
        String value = getValue();
        items.add(new Item(type, value, start));
    }

    public Item nextItem() {
        if (index < items.size()) {
            return items.get(index++);
        }
        return null;
    }

}
