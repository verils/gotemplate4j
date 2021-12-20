package io.github.verils.gotemplate.lex;

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

    private static final char TRIM_MARKER = '-';
    private static final int TRIM_MARKER_LENGTH = 2;

    private static final int AT_RIGHT_DELIM_STATUS_UNREACHED = 0;
    private static final int AT_RIGHT_DELIM_STATUS_REACHED_WITHOUT_TRIM_MARKER = 1;
    private static final int AT_RIGHT_DELIM_STATUS_REACHED_WITH_TRIM_MARKER = 2;


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
    private final boolean keepComments;

    private final String leftDelim;
    private final String rightDelim;

    private final String leftComment;
    private final String rightComment;

    /* 解析过程种的标记位 */
    private int start = 0;
    private int pos = 0;
    private int parenDepth = 0;

    private final List<Item> items = new ArrayList<>(32);


    public Lexer(String input) {
        this(input, false);
    }

    public Lexer(String input, boolean keepComments) {
        if (input == null) {
            throw new NullPointerException();
        }

        this.input = input;
        this.keepComments = keepComments;

        this.leftDelim = DEFAULT_LEFT_DELIM;
        this.rightDelim = DEFAULT_RIGHT_DELIM;

        this.leftComment = DEFAULT_LEFT_COMMENT;
        this.rightComment = DEFAULT_RIGHT_COMMENT;

        parse();
    }

    private void parse() {
        State state = parseText();
        while (state != null) {
            state = state.exec();
        }
    }

    private State parseText() {
        int n = input.indexOf(leftDelim, pos);
        boolean hasLeftDelim = n >= 0;
        if (hasLeftDelim) {
            pos = n;

            boolean atLeftTrimMarker = atLeftTrimMarker();
            if (atLeftTrimMarker) {
                pos -= ltrimLength();
            }

            if (pos > start) {
                addItem(ItemType.TEXT);
            }

            pos = n;
            moveStartToPos();

            return this::parseLeftDelim;
        }

        if (input.length() > pos) {
            pos = input.length();
            addItem(ItemType.TEXT);
        }

        moveStartToPos();
        addItem(ItemType.EOF);

        return null;
    }

    private State parseLeftDelim() {
        boolean atLeftTrimMarker = atLeftTrimMarker();
        pos += leftDelim.length();

        int parseStart = atLeftTrimMarker ? pos + 2 : pos;
        int n = input.indexOf(leftComment, parseStart);
        boolean hasComment = n >= 0;
        if (hasComment) {
            pos = n;
            moveStartToPos();

            return this::parseComment;
        }

        addItem(ItemType.LEFT_DELIM);

        pos = parseStart;
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseComment() {
        int parseStart = pos + leftComment.length();
        int n = input.indexOf(rightComment, parseStart);
        if (n < 0) {
            return parseError("unclosed comment");
        }

        pos = n + rightComment.length();

        int atRightDelimStatus = atRightDelimStatus();
        if (atRightDelimStatus == AT_RIGHT_DELIM_STATUS_UNREACHED) {
            return parseError("comment closed leaving delim still open");
        }

        if (keepComments) {
            addItem(ItemType.COMMENT);
        }

        if (atRightDelimStatus == AT_RIGHT_DELIM_STATUS_REACHED_WITH_TRIM_MARKER) {
            pos += TRIM_MARKER_LENGTH + rightDelim.length();
            pos += rtrimLength();
        }
        if (atRightDelimStatus == AT_RIGHT_DELIM_STATUS_REACHED_WITHOUT_TRIM_MARKER) {
            pos += rightDelim.length();
        }
        moveStartToPos();

        return this::parseText;
    }

    private State parseInsideAction() {
        int atRightDelimStatus = atRightDelimStatus();
        if (atRightDelimStatus != AT_RIGHT_DELIM_STATUS_UNREACHED) {
            if (parenDepth != 0) {
                return parseError("unclosed left paren");
            }

            return this::parseRightDelim;
        }

        char ch = nextChar();
        if (ch == Char.EOF) {
            return parseError("unclosed action");
        } else if (Char.isSpace(ch)) {
            return this::parseSpace;
        } else if (ch == ':') {
            return this::parseDeclare;
        } else if (ch == '|') {
            return this::parsePipe;
        } else if (ch == '"') {
            return this::parseQuote;
        } else if (ch == '`') {
            return this::parseRawQuote;
        } else if (ch == '$') {
            return this::parseVariable;
        } else if (ch == '\'') {
            return this::parseChar;
        } else if (ch == '.') {
            return this::parseDot;
        } else if (Char.isValid(ch, "+-") || Char.isNumeric(ch)) {
            movePosToStart();
            return this::parseNumber;
        } else if (Char.isAlphabetic(ch)) {
            movePosToStart();
            return this::parseIdentifier;
        } else if (ch == '(') {
            addItem(ItemType.LEFT_PAREN);
            parenDepth++;
        } else if (ch == ')') {
            addItem(ItemType.RIGHT_PAREN);
            parenDepth--;
        } else if (Char.isAscii(ch) && Char.isVisible(ch)) {
            addItem(ItemType.CHAR);
        } else {
            return parseError("bad character in action: " + ch);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private State parseRightDelim() {
        boolean atRightTrimMarker = atRightTrimMarker();
        if (atRightTrimMarker) {
            pos += 2;
            moveStartToPos();
        }

        pos = start + rightDelim.length();
        addItem(ItemType.RIGHT_DELIM);
        moveStartToPos();

        if (atRightTrimMarker) {
            while (Char.isSpace(nextChar())) {
            }
            pos--;
            moveStartToPos();
        }

        return this::parseText;
    }

    private State parseSpace() {
        addItem(ItemType.SPACE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseDeclare() {
        char ch = nextChar();
        if (ch != '=') {
            return () -> parseError("expected :=");
        }

        addItem(ItemType.DECLARE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parsePipe() {
        addItem(ItemType.PIPE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();
                if (!Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                    continue;
                }
            }

            if (Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                return parseError("unterminated quoted string");
            }

            if (ch == '"') {
                break;
            }
        }

        addItem(ItemType.STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseRawQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == Char.EOF) {
                return parseError("unclosed raw quote");
            }

            if (ch == '`') {
                break;
            }
        }

        addItem(ItemType.STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseVariable() {
        if (atWordTerminator()) {
            addItem(ItemType.VARIABLE);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !Char.isAlphabetic(c));
        if (!atWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addItem(ItemType.VARIABLE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseChar() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();

                if (!Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                    continue;
                }
            }

            if (Char.isValid(ch, Char.EOF, Char.NEW_LINE)) {
                return parseError("unclosed character constant");
            }

            if (ch == '\'') {
                break;
            }
        }

        addItem(ItemType.CHAR_CONSTANT);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseDot() {
        char ch = getChar();
        if (ch < '0' || '9' < ch) {
            return this::parseField;
        }

        return this::parseIdentifier;
    }

    private State parseField() {
        if (atWordTerminator()) {
            addItem(ItemType.DOT);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !Char.isAlphabetic(c));
        if (!atWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addItem(ItemType.FIELD);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseNumber() {
        lookForNumber();

        char ch = getChar();
        if (Char.isAlphabetic(ch)) {
            pos++;
            return parseError("bad number: " + getValue());
        }

        if (Char.isValid(ch, "+-")) {
            lookForNumber();

            addItem(ItemType.COMPLEX);
        } else {
            addItem(ItemType.NUMBER);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseIdentifier() {
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

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseError(String error) {
        addErrorItem(error);
        return null;
    }

    private void lookForNumber() {
        goIf("+-");

        String digits = Char.DECIMAL_DIGITS;

        char ch = nextChar();
        if (ch == '0') {
            ch = nextChar();
            if (Char.isValid(ch, "xX")) {
                digits = Char.HEX_DIGITS;
            } else if (Char.isValid(ch, "oO")) {
                digits = Char.OCTET_DIGITS;
            } else if (Char.isValid(ch, "bB")) {
                digits = Char.BINARY_DIGITS;
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
            ch = goUntilNot(Char.DECIMAL_DIGITS);
        }

        if (digits.length() == 16 + 6 + 1 && Char.isValid(ch, "pP")) {
            pos++;

            goIf("+-");
            goUntilNot(Char.DECIMAL_DIGITS);
        }

        goIf("i");
    }

    private void moveStartToPos() {
        start = pos;
    }

    private void movePosToStart() {
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
        int leftDelimLength = leftDelim.length();
        if (pos + leftDelimLength + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }
        return input.indexOf(leftDelim, pos) == pos &&
                TRIM_MARKER == input.charAt(pos + leftDelimLength) &&
                Char.isSpace(input.charAt(pos + leftDelimLength + 1));
    }

    private boolean atRightTrimMarker() {
        if (pos + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }
        return Char.isSpace(input.charAt(pos)) && TRIM_MARKER == input.charAt(pos + 1);
    }

    private int atRightDelimStatus() {
        if (atRightTrimMarker() && atRightDelim(pos + TRIM_MARKER_LENGTH)) {
            return AT_RIGHT_DELIM_STATUS_REACHED_WITH_TRIM_MARKER;
        }
        if (atRightDelim(pos)) {
            return AT_RIGHT_DELIM_STATUS_REACHED_WITHOUT_TRIM_MARKER;
        }
        return AT_RIGHT_DELIM_STATUS_UNREACHED;
    }

    private boolean atRightDelim(int checkPos) {
        return input.indexOf(rightDelim, checkPos) == checkPos;
    }

    private boolean atWordTerminator() {
        char ch = getChar();
        if (Char.isSpace(ch) || Char.isValid(ch, Char.EOF + ".,|:()")) {
            return true;
        }
        return atRightDelimStatus() != AT_RIGHT_DELIM_STATUS_UNREACHED;
    }

    private String getValue() {
        return input.substring(start, pos);
    }

    private int ltrimLength() {
        int i = pos;
        for (; i > 0; i--) {
            char ch = input.charAt(i - 1);
            if (!Char.isSpace(ch)) {
                break;
            }
        }
        return pos - i;
    }

    private int rtrimLength() {
        int i = pos;
        for (; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (!Char.isSpace(ch)) {
                break;
            }
        }
        return i - pos;
    }

    private void addItem(ItemType type) {
        String value = getValue();
        items.add(new Item(type, value, start));
    }

    private void addErrorItem(String error) {
        items.add(new Item(ItemType.ERROR, error, start));
    }

    public LexerViewer getViewer() {
        return new LexerViewer(input, items.toArray(new Item[0]));
    }


    private interface State {

        State exec();
    }
}
