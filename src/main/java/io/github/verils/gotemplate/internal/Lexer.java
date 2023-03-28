package io.github.verils.gotemplate.internal;

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


    private static final Map<String, TokenType> KEY_MAP = new LinkedHashMap<>();

    static {
        KEY_MAP.put(".", TokenType.DOT);
        KEY_MAP.put("block", TokenType.BLOCK);
        KEY_MAP.put("define", TokenType.DEFINE);
        KEY_MAP.put("else", TokenType.ELSE);
        KEY_MAP.put("end", TokenType.END);
        KEY_MAP.put("if", TokenType.IF);
        KEY_MAP.put("nil", TokenType.NIL);
        KEY_MAP.put("range", TokenType.RANGE);
        KEY_MAP.put("template", TokenType.TEMPLATE);
        KEY_MAP.put("with", TokenType.WITH);
    }


    private final String input;
    private final boolean keepComments;

    private final String leftDelimiter;
    private final String rightDelimiter;

    private final String leftComment;
    private final String rightComment;


    /* Position markers */

    private int start = 0;
    private int pos = 0;
    private int parenDepth = 0;


    /* Result tokens */

    private final List<Token> tokens = new ArrayList<>(32);


    public Lexer(String input) {
        this(input, false);
    }

    public Lexer(String input, boolean keepComments) {
        this(input, keepComments, DEFAULT_LEFT_DELIM, DEFAULT_RIGHT_DELIM, DEFAULT_LEFT_COMMENT, DEFAULT_RIGHT_COMMENT);
    }

    public Lexer(String input, boolean keepComments, String leftDelimiter, String rightDelimiter, String leftComment, String rightComment) {
        if (input == null) {
            throw new NullPointerException();
        }

        this.input = input;
        this.keepComments = keepComments;
        this.leftDelimiter = leftDelimiter;
        this.rightDelimiter = rightDelimiter;
        this.leftComment = leftComment;
        this.rightComment = rightComment;

        parseInput();
    }

    /**
     * Parse and handle states
     */
    private void parseInput() {
        State state = parseText();
        while (state != null) {
            state = state.run();
        }
    }

    private State parseText() {
        int n = input.indexOf(leftDelimiter, pos);
        boolean hasLeftDelim = n >= 0;
        if (hasLeftDelim) {
            pos = n;

            boolean atLeftTrimMarker = atLeftTrimMarker();
            if (atLeftTrimMarker) {
                pos -= ltrimLength();
            }

            if (pos > start) {
                addItem(TokenType.TEXT);
            }

            pos = n;
            moveStartToPos();

            return this::parseLeftDelim;
        }

        if (input.length() > pos) {
            pos = input.length();
            addItem(TokenType.TEXT);
        }

        moveStartToPos();
        addItem(TokenType.EOF);

        return null;
    }

    private State parseLeftDelim() {
        boolean atLeftTrimMarker = atLeftTrimMarker();
        pos += leftDelimiter.length();

        int parseStart = atLeftTrimMarker ? pos + 2 : pos;
        int n = input.indexOf(leftComment, parseStart);
        boolean hasComment = n >= 0;
        if (hasComment) {
            pos = n;
            moveStartToPos();

            return this::parseComment;
        }

        addItem(TokenType.LEFT_DELIM);

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
            addItem(TokenType.COMMENT);
        }

        if (atRightDelimStatus == AT_RIGHT_DELIM_STATUS_REACHED_WITH_TRIM_MARKER) {
            pos += TRIM_MARKER_LENGTH + rightDelimiter.length();
            pos += rtrimLength();
        }
        if (atRightDelimStatus == AT_RIGHT_DELIM_STATUS_REACHED_WITHOUT_TRIM_MARKER) {
            pos += rightDelimiter.length();
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
        if (ch == CharUtils.EOF) {
            return parseError("unclosed action");
        } else if (CharUtils.isSpace(ch)) {
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
        } else if (CharUtils.isValid(ch, "+-") || CharUtils.isNumeric(ch)) {
            movePosToStart();
            return this::parseNumber;
        } else if (CharUtils.isAlphabetic(ch)) {
            movePosToStart();
            return this::parseIdentifier;
        } else if (ch == '(') {
            addItem(TokenType.LEFT_PAREN);
            parenDepth++;
        } else if (ch == ')') {
            addItem(TokenType.RIGHT_PAREN);
            parenDepth--;
        } else if (CharUtils.isAscii(ch) && CharUtils.isVisible(ch)) {
            addItem(TokenType.CHAR);
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

        pos = start + rightDelimiter.length();
        addItem(TokenType.RIGHT_DELIM);
        moveStartToPos();

        if (atRightTrimMarker) {
            while (CharUtils.isSpace(nextChar())) {
            }
            pos--;
            moveStartToPos();
        }

        return this::parseText;
    }

    private State parseSpace() {
        addItem(TokenType.SPACE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseDeclare() {
        char ch = nextChar();
        if (ch != '=') {
            return () -> parseError("expected :=");
        }

        addItem(TokenType.DECLARE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parsePipe() {
        addItem(TokenType.PIPE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();
                if (!CharUtils.isValid(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                    continue;
                }
            }

            if (CharUtils.isValid(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                return parseError("unterminated quoted string");
            }

            if (ch == '"') {
                break;
            }
        }

        addItem(TokenType.STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseRawQuote() {
        while (true) {
            char ch = nextChar();
            if (ch == CharUtils.EOF) {
                return parseError("unclosed raw quote");
            }

            if (ch == '`') {
                break;
            }
        }

        addItem(TokenType.STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseVariable() {
        if (atWordTerminator()) {
            addItem(TokenType.VARIABLE);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !CharUtils.isAlphabetic(c));
        if (!atWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addItem(TokenType.VARIABLE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseChar() {
        while (true) {
            char ch = nextChar();
            if (ch == '\\') {
                ch = nextChar();

                if (!CharUtils.isValid(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                    continue;
                }
            }

            if (CharUtils.isValid(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                return parseError("unclosed character constant");
            }

            if (ch == '\'') {
                break;
            }
        }

        addItem(TokenType.CHAR_CONSTANT);
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
            addItem(TokenType.DOT);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !CharUtils.isAlphabetic(c));
        if (!atWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addItem(TokenType.FIELD);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseNumber() {
        lookForNumber();

        char ch = getChar();
        if (CharUtils.isAlphabetic(ch)) {
            pos++;
            return parseError("bad number: " + getValue());
        }

        if (CharUtils.isValid(ch, "+-")) {
            lookForNumber();

            addItem(TokenType.COMPLEX);
        } else {
            addItem(TokenType.NUMBER);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseIdentifier() {
        while (true) {
            char ch = nextChar();
            if (ch == CharUtils.EOF) {
                break;
            }
            if (!CharUtils.isAlphabetic(ch)) {
                pos--;
                break;
            }
        }
        String word = getValue();

        if (KEY_MAP.containsKey(word)) {
            addItem(KEY_MAP.get(word));
        } else if (word.charAt(0) == '.') {
            addItem(TokenType.FIELD);
        } else if ("true".equals(word) || "false".equals(word)) {
            addItem(TokenType.BOOL);
        } else {
            addItem(TokenType.IDENTIFIER);
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

        String digits = CharUtils.DECIMAL_DIGITS;

        char ch = nextChar();
        if (ch == '0') {
            ch = nextChar();
            if (CharUtils.isValid(ch, "xX")) {
                digits = CharUtils.HEX_DIGITS;
            } else if (CharUtils.isValid(ch, "oO")) {
                digits = CharUtils.OCTET_DIGITS;
            } else if (CharUtils.isValid(ch, "bB")) {
                digits = CharUtils.BINARY_DIGITS;
            }
        }

        ch = goUntilNot(digits);
        if (ch == '.') {
            pos++;
            ch = goUntilNot(digits);
        }

        if (digits.length() == 10 + 1 && CharUtils.isValid(ch, "eE")) {
            pos++;

            goIf("+-");
            ch = goUntilNot(CharUtils.DECIMAL_DIGITS);
        }

        if (digits.length() == 16 + 6 + 1 && CharUtils.isValid(ch, "pP")) {
            pos++;

            goIf("+-");
            goUntilNot(CharUtils.DECIMAL_DIGITS);
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
        if (CharUtils.isValid(ch, want)) {
            pos++;
        }
    }

    private char goUntilNot(String valid) {
        Predicate<Character> predicate = c -> !CharUtils.isValid(c, valid);
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
            return CharUtils.EOF;
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
        int leftDelimLength = leftDelimiter.length();
        if (pos + leftDelimLength + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }
        return input.indexOf(leftDelimiter, pos) == pos &&
                TRIM_MARKER == input.charAt(pos + leftDelimLength) &&
                CharUtils.isSpace(input.charAt(pos + leftDelimLength + 1));
    }

    private boolean atRightTrimMarker() {
        if (pos + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }
        return CharUtils.isSpace(input.charAt(pos)) && TRIM_MARKER == input.charAt(pos + 1);
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
        return input.indexOf(rightDelimiter, checkPos) == checkPos;
    }

    private boolean atWordTerminator() {
        char ch = getChar();
        if (CharUtils.isSpace(ch) || CharUtils.isValid(ch, CharUtils.EOF + ".,|:()")) {
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
            if (!CharUtils.isSpace(ch)) {
                break;
            }
        }
        return pos - i;
    }

    private int rtrimLength() {
        int i = pos;
        for (; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (!CharUtils.isSpace(ch)) {
                break;
            }
        }
        return i - pos;
    }

    private void addItem(TokenType type) {
        String value = getValue();
        tokens.add(new Token(type, value, start));
    }

    private void addErrorItem(String error) {
        tokens.add(new Token(TokenType.ERROR, error, start));
    }

    public List<Token> getTokens() {
        return tokens;
    }


    private interface State {

        State run();
    }
}
