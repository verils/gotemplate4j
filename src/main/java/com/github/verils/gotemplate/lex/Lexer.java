package com.github.verils.gotemplate.lex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Lexer {

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
        int n = input.indexOf(leftDelim, pos);
        boolean hasLeftDelim = n >= 0;
        if (hasLeftDelim) {
            pos = n;

            boolean atLeftTrimMarker = atLeftTrimMarker();
            if (atLeftTrimMarker) {
                String text = getValue();
                int rtrimLength = rtrimLength(text);
                pos -= rtrimLength;
            }

            if (pos > start) {
                addItem(ItemType.TEXT);
            }

            pos = n;
            updateStart();

            parseLeftDelim();
            return;
        }

        if (input.length() > pos) {
            pos = input.length();
            addItem(ItemType.TEXT);
        }

        updateStart();
        addItem(ItemType.EOF);
    }

    private void parseLeftDelim() {
        boolean atLeftTrimMarker = atLeftTrimMarker();
        pos += leftDelim.length();

        int parseStart = atLeftTrimMarker ? pos + 2 : pos;
        int n = input.indexOf(leftComment, parseStart);
        boolean hasComment = n >= 0;
        if (hasComment) {
            pos = n;
            updateStart();

            parseComment();
            return;
        }

        addItem(ItemType.LEFT_DELIM);

        pos = parseStart;
        updateStart();

        parseInsideAction();
    }

    private void parseComment() {
        int parseStart = pos + leftComment.length();
        int n = input.indexOf(rightComment, parseStart);
        if (n < 0) {
            addErrorItem("unclosed comment");
            return;
        }

        pos = n + rightComment.length();

        boolean atRightTrimMarker = atRightTrimMarker();
        boolean atRightDelim = atRightDelim();
        if (!atRightTrimMarker && !atRightDelim) {
            addErrorItem("comment closed leaving delim still open");
            return;
        }

        addItem(ItemType.COMMENT);

        if (atRightTrimMarker) {
            pos += 2 + rightDelim.length();
        }
        if (atRightDelim) {
            pos += rightDelim.length();
        }

        updateStart();

        parseText();
    }

    private void parseInsideAction() {
        boolean atRightTrimMarker = atRightTrimMarker();
        boolean atRightDelim = atRightDelim();
        if (atRightTrimMarker || atRightDelim) {
            if (parenDepth != 0) {
                addErrorItem("unclosed left paren");
                return;
            }

            parseRightDelim();
            return;
        }

        char ch = nextChar();
        if (ch == Char.EOF) {
            addErrorItem("unclosed action");
            return;
        } else if (Char.isSpace(ch)) {
            parseSpace();
            return;
        } else if (ch == ':') {
            parseDeclare();
            return;
        } else if (ch == '|') {
            parsePipe();
            return;
        } else if (ch == '"') {
            parseQuote();
            return;
        } else if (ch == '`') {
            parseRawQuote();
            return;
        } else if (ch == '$') {
            parseVariable();
            return;
        } else if (ch == '\'') {
            parseChar();
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
        } else if (Char.isAscii(ch) && Char.isVisible(ch)) {
            addItem(ItemType.CHAR);
        } else {
            addErrorItem("bad character in action: " + ch);
            return;
        }

        updateStart();

        parseInsideAction();
    }

    private void parseRightDelim() {
        boolean atRightTrimMarker = atRightTrimMarker();
        if (atRightTrimMarker) {
            pos += 2;
            updateStart();
        }

        pos = start + rightDelim.length();
        addItem(ItemType.RIGHT_DELIM);
        updateStart();

        if (atRightTrimMarker) {
            while (Char.isSpace(nextChar())) {
            }
            pos--;
            updateStart();
        }

        parseText();
    }

    private void parseSpace() {
        addItem(ItemType.SPACE);
        updateStart();

        parseInsideAction();
    }

    private void parseDeclare() {
        char ch = nextChar();
        if (ch != '=') {
            throw new SyntaxException("expected :=");
        }

        addItem(ItemType.DECLARE);
        updateStart();

        parseInsideAction();
    }

    private void parsePipe() {
        addItem(ItemType.PIPE);
        updateStart();

        parseInsideAction();
    }

    private void parseQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();
                if (!Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                    continue;
                }
            }

            if (Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                addErrorItem("unclosed quote");
                return;
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
                addErrorItem("unclosed raw quote");
                return;
            }

            if (ch == '`') {
                break;
            }
        }

        addItem(ItemType.STRING);
        updateStart();

        parseInsideAction();
    }

    private void parseVariable() {
        if (atWordTerminator()) {
            addItem(ItemType.VARIABLE);
            updateStart();

            parseInsideAction();
            return;
        }

        char ch = goUntil(c -> !Char.isAlphabetic(c));
        if (!atWordTerminator()) {
            throw new SyntaxException("bad character: " + ch);
        }

        addItem(ItemType.VARIABLE);
        updateStart();

        parseInsideAction();
    }

    private void parseChar() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();

                if (!Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                    continue;
                }
            }

            if (Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                addErrorItem("unclosed character constant");
                return;
            }

            if (ch == '\'') {
                break;
            }
        }

        addItem(ItemType.CHAR_CONSTANT);
        updateStart();

        parseInsideAction();
    }

    private void parseDot() {
        char ch = getChar();
        if (ch < '0' || '9' < ch) {
            parseField();
            return;
        }

        parseIdentifier();
    }

    private void parseField() {
        if (atWordTerminator()) {
            addItem(ItemType.DOT);
            updateStart();

            parseInsideAction();
            return;
        }

        char ch = goUntil(c -> !Char.isAlphabetic(c));
        if (!atWordTerminator()) {
            throw new SyntaxException("bad character: " + ch);
        }

        addItem(ItemType.FIELD);
        updateStart();

        parseInsideAction();
    }

    private void parseNumber() {
        lookForNumber();

        char ch = getChar();
        if (Char.isAlphabetic(ch)) {
            pos++;
            addErrorItem("bad number: " + getValue());
            return;
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
            goUntilNot(DECIMAL_DIGITS);
        }

        goIf("i");
    }

    private void parseIdentifier() {
        while (true) {
            char ch = nextChar();
            if (ch == Char.EOF) {
                break;
            }
            if (!Char.isAlphabetic(ch)) {
                pos--;
                break;
            }
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

    private char goUntilNot(String valid) {
        Predicate<Character> predicate = c -> !Char.isValid(c, valid);
        return goUntil(predicate);
    }

    private char goUntil(Predicate<Character> predicate) {
        while (true) {
            char ch = nextChar();
            if (predicate.test(ch)) {
                pos--;
                return ch;
            }
        }
    }

    private char nextChar() {
        if (pos >= input.length()) {
            return Char.EOF;
        }

        char ch = input.charAt(pos);
        pos++;
        return ch;
    }

    private char getChar() {
        char ch = nextChar();
        pos--;
        return ch;
    }

    private boolean atLeftTrimMarker() {
        int n = pos + leftDelim.length() + 2;
        if (n > input.length()) {
            return false;
        }
        return (leftDelim + "- ").equals(input.substring(pos, n));
    }

    private boolean atRightTrimMarker() {
        int n = pos + leftDelim.length() + 2;
        if (n > input.length()) {
            return false;
        }
        return (" -" + rightDelim).equals(input.substring(pos, n));
    }

    private boolean atRightDelim() {
        int n = input.indexOf(rightDelim, pos);
        return n == pos;
    }

    private boolean atWordTerminator() {
        char ch = getChar();
        if (Char.isSpace(ch) || Char.isValid(ch, Char.EOF + ".,|:()")) {
            return true;
        }
        return atRightDelim();
    }

    private String getValue() {
        return input.substring(start, pos);
    }

    private int rtrimLength(String text) {
        int len = 0;
        for (int i = text.length() - 1; i >= 0; i--) {
            char ch = text.charAt(i);
            if (!Char.isSpace(ch)) {
                break;
            }
            len++;
        }
        return len;
    }

    private void addItem(ItemType type) {
        String value = getValue();
        items.add(new Item(type, value, start));
    }

    private void addErrorItem(String error) {
        items.add(new Item(ItemType.ERROR, error, start));
    }

    public Item nextItem() {
        if (index < items.size()) {
            return items.get(index++);
        }
        return null;
    }

}
