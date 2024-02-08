package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.internal.lang.CharUtils;

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


    /**
     * Current position of the input
     */
    private int pos = 0;

    /**
     * Start position of current token
     */
    private int start = 0;


    private int parenDepth = 0;


    /**
     * The count of newline have met + 1
     */
    private int line = 1;

    /**
     * Start line of current token
     */
    private int startLine = line;

    private int column = 0;

    private int lineStart = 0;


    /* Result tokens */

    private final List<Token> tokens = new ArrayList<>(8);


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

        parse();
    }

    /**
     * Parse and handle states
     */
    private void parse() {
        State state = parseText();
        while (state != null) {
            state = state.run();
        }
    }

    private State parseText() {
        moveStartToPos();

        int leftDelimPos = input.indexOf(leftDelimiter, pos);
        if (leftDelimPos == -1) {
            if (input.length() > pos) {
                int startLine = line;

                int eolPos;
                while ((eolPos = input.indexOf(CharUtils.NEW_LINE, pos)) != -1) {
                    line++;
                    pos = eolPos + 1;
                }

                String text = input.substring(start);
                addToken(TokenType.TEXT, text, start, startLine);
            }

            pos = input.length();
            addToken(TokenType.EOF, "", input.length(), line);

            return null;
        } else {
            pos = leftDelimPos;

            int eotPos = pos;

            boolean posAtLeftDelimWithTrimMarker = isPosAtLeftDelimWithTrimMarker();
            if (posAtLeftDelimWithTrimMarker) {
                for (; eotPos >= start; eotPos--) {
                    char ch = input.charAt(eotPos - 1);

                    if (CharUtils.isNewline(ch)) {
                        line++;
                    }

                    if (!CharUtils.isSpace(ch)) {
                        break;
                    }
                }
            }

            if (eotPos > start) {
                String text = input.substring(start, eotPos);
                addToken(TokenType.TEXT, text);
            }

            moveStartToPos();

            return this::parseLeftDelim;
        }
    }

    private State parseLeftDelim() {
        boolean atLeftTrimMarker = isPosAtLeftDelimWithTrimMarker();
        pos += leftDelimiter.length();

        int parseStart = atLeftTrimMarker ? pos + 2 : pos;
        int n = input.indexOf(leftComment, parseStart);
        boolean hasComment = n >= 0;
        if (hasComment) {
            pos = n;
            moveStartToPos();

            return this::parseComment;
        }

        addToken(TokenType.LEFT_DELIM);

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

        boolean posAtRightDelim = isPosAtRightDelim();
        if (!posAtRightDelim) {
            return parseError("comment closed leaving delim still open");
        }

        if (keepComments) {
            addToken(TokenType.COMMENT);
        }

        if (isPosAtRightDelimWithTrimMarker()) {
            pos += TRIM_MARKER_LENGTH + rightDelimiter.length();
            int sotPos = pos;
            for (; sotPos < input.length(); sotPos++) {
                char ch = input.charAt(sotPos);
                if (!CharUtils.isSpace(ch)) {
                    break;
                }
            }
            pos += sotPos - pos;
        }
        if (isPosAtRightDelimWithoutTrimMarker()) {
            pos += rightDelimiter.length();
        }
        moveStartToPos();

        return this::parseText;
    }

    private State parseInsideAction() {
        boolean posAtRightDelim = isPosAtRightDelim();
        if (posAtRightDelim) {
            if (parenDepth != 0) {
                return parseError("unclosed left paren");
            }

            return this::parseRightDelim;
        }

        char ch = getCurrentCharAndGoToNext();
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
        } else if (CharUtils.isAnyOf(ch, "+-") || CharUtils.isNumeric(ch)) {
            movePosToStart();
            return this::parseNumber;
        } else if (CharUtils.isAlphabetic(ch)) {
            movePosToStart();
            return this::parseIdentifier;
        } else if (ch == '(') {
            parenDepth++;
            addToken(TokenType.LEFT_PAREN);
        } else if (ch == ')') {
            parenDepth--;
            if (parenDepth < 0) {
                return parseError("unexpected right paren");
            }
            addToken(TokenType.RIGHT_PAREN);
        } else if (CharUtils.isAscii(ch) && CharUtils.isVisible(ch)) {
            addToken(TokenType.CHAR);
        } else {
            return parseError("bad character in action: " + ch);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseRightDelim() {
        boolean posAtRightDelimWithTrimMarker = isPosAtRightDelimWithTrimMarker();
        if (posAtRightDelimWithTrimMarker) {
            pos += 2;
            moveStartToPos();
        }

        pos = start + rightDelimiter.length();
        addToken(TokenType.RIGHT_DELIM);
        moveStartToPos();

        if (posAtRightDelimWithTrimMarker) {
            char ch = getCurrentChar();
            if (ch != CharUtils.EOF) {
                goToNoneSpace();
                moveStartToPos();
            }
        }

        return this::parseText;
    }

    private State parseSpace() {
        addToken(TokenType.SPACE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseDeclare() {
        char ch = getCurrentCharAndGoToNext();
        if (ch != '=') {
            return () -> parseError("expected :=");
        }

        addToken(TokenType.DECLARE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parsePipe() {
        addToken(TokenType.PIPE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseQuote() {
        while (true) {
            char ch = getCurrentCharAndGoToNext();
            if (ch == '\\') {
                ch = getCurrentCharAndGoToNext();
                if (!CharUtils.isAnyOf(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                    continue;
                }
            }

            if (CharUtils.isAnyOf(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                return parseError("unterminated quoted string");
            }

            if (ch == '"') {
                break;
            }
        }

        addToken(TokenType.STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseRawQuote() {
        while (true) {
            char ch = getCurrentCharAndGoToNext();
            if (ch == CharUtils.EOF) {
                return parseError("unclosed raw quote");
            }

            if (ch == '`') {
                break;
            }
        }

        addToken(TokenType.RAW_STRING);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseVariable() {
        if (isPosAtWordTerminator()) {
            addToken(TokenType.VARIABLE);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !CharUtils.isAlphabetic(c));
        if (!isPosAtWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addToken(TokenType.VARIABLE);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseChar() {
        while (true) {
            char ch = getCurrentCharAndGoToNext();
            if (ch == '\\') {
                ch = getCurrentCharAndGoToNext();

                if (!CharUtils.isAnyOf(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                    continue;
                }
            }

            if (CharUtils.isAnyOf(ch, CharUtils.EOF, CharUtils.NEW_LINE)) {
                return parseError("unclosed character constant");
            }

            if (ch == '\'') {
                break;
            }
        }

        addToken(TokenType.CHAR_CONSTANT);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseDot() {
        char ch = getCurrentChar();
        if (ch < '0' || '9' < ch) {
            return this::parseField;
        }

        return this::parseIdentifier;
    }

    private State parseField() {
        if (isPosAtWordTerminator()) {
            addToken(TokenType.DOT);
            moveStartToPos();

            return this::parseInsideAction;
        }

        char ch = goUntil(c -> !CharUtils.isAlphabetic(c));
        if (!isPosAtWordTerminator()) {
            return () -> parseError("bad character: " + ch);
        }

        addToken(TokenType.FIELD);
        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseNumber() {
        lookForNumber();

        char ch = getCurrentChar();
        if (CharUtils.isAlphabetic(ch)) {
            pos++;
            return parseError("bad number: " + getText());
        }

        if (CharUtils.isAnyOf(ch, "+-")) {
            lookForNumber();

            addToken(TokenType.COMPLEX);
        } else {
            addToken(TokenType.NUMBER);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseIdentifier() {
        while (true) {
            char ch = getCurrentCharAndGoToNext();
            if (ch == CharUtils.EOF) {
                break;
            }
            if (!CharUtils.isAlphabetic(ch)) {
                pos--;
                break;
            }
        }
        String word = getText();

        if (KEY_MAP.containsKey(word)) {
            addToken(KEY_MAP.get(word));
        } else if (word.charAt(0) == '.') {
            addToken(TokenType.FIELD);
        } else if ("true".equals(word) || "false".equals(word)) {
            addToken(TokenType.BOOL);
        } else {
            addToken(TokenType.IDENTIFIER);
        }

        moveStartToPos();

        return this::parseInsideAction;
    }

    private State parseError(String error) {
        addToken(TokenType.ERROR, error);
        return null;
    }

    private void lookForNumber() {
        goUntilNot("+-");

        String digits = CharUtils.DECIMAL_DIGITS;

        char ch = getCurrentCharAndGoToNext();
        if (ch == '0') {
            ch = getCurrentCharAndGoToNext();
            if (CharUtils.isAnyOf(ch, "xX")) {
                digits = CharUtils.HEX_DIGITS;
            } else if (CharUtils.isAnyOf(ch, "oO")) {
                digits = CharUtils.OCTET_DIGITS;
            } else if (CharUtils.isAnyOf(ch, "bB")) {
                digits = CharUtils.BINARY_DIGITS;
            }
        }

        ch = goUntilNot(digits);
        if (ch == '.') {
            pos++;
            ch = goUntilNot(digits);
        }

        if (digits.length() == 10 + 1 && CharUtils.isAnyOf(ch, "eE")) {
            pos++;

            goUntilNot("+-");
            ch = goUntilNot(CharUtils.DECIMAL_DIGITS);
        }

        if (digits.length() == 16 + 6 + 1 && CharUtils.isAnyOf(ch, "pP")) {
            pos++;

            goUntilNot("+-");
            goUntilNot(CharUtils.DECIMAL_DIGITS);
        }

        goUntilNot("i");
    }

    private void moveStartToPos() {
        start = pos;
        startLine = line;
    }

    private void movePosToStart() {
        pos = start;
    }

    private void goToNoneSpace() {
        while (true) {
            char ch = getCurrentChar();

            if (CharUtils.isNewline(ch)) {
                line++;
            }

            if (!CharUtils.isSpace(ch)) {
                return;
            }
            pos++;
        }
    }

    private char goUntilNot(CharSequence chars) {
        return goUntil(ch -> !CharUtils.isAnyOf(ch, chars));
    }

    private char goUntil(Predicate<Character> predicate) {
        while (true) {
            char ch = getCurrentChar();
            if (predicate.test(ch)) {
                return ch;
            }
            pos++;
        }
    }

    private char getCurrentCharAndGoToNext() {
        if (pos < input.length()) {
            char ch = input.charAt(pos);
            pos++;
            return ch;
        } else {
            return CharUtils.EOF;
        }
    }

    private char getCurrentChar() {
        return pos < input.length() ? input.charAt(pos) : CharUtils.EOF;
    }

    private boolean isPosAtLeftDelimWithTrimMarker() {
        int leftDelimLength = leftDelimiter.length();
        if (pos + leftDelimLength + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }

        if (input.indexOf(leftDelimiter, pos) != pos) {
            return false;
        }

        if (TRIM_MARKER != input.charAt(pos + leftDelimLength)) {
            return false;
        }

        return CharUtils.isSpace(input.charAt(pos + leftDelimLength + 1));
    }

    private boolean isPosAtRightDelim() {
        return isPosAtRightDelimWithTrimMarker() || isPosAtRightDelimWithoutTrimMarker();
    }

    private boolean isPosAtRightDelimWithTrimMarker() {
        if (pos + TRIM_MARKER_LENGTH > input.length()) {
            return false;
        }

        if (!CharUtils.isSpace(input.charAt(pos))) {
            return false;
        }

        if (TRIM_MARKER != input.charAt(pos + 1)) {
            return false;
        }

        return isPosAtRightDelim(pos + TRIM_MARKER_LENGTH);
    }

    private boolean isPosAtRightDelimWithoutTrimMarker() {
        return isPosAtRightDelim(pos);
    }

    private boolean isPosAtRightDelim(int pos) {
        return input.indexOf(rightDelimiter, pos) == pos;
    }

    private boolean isPosAtWordTerminator() {
        char ch = getCurrentChar();
        if (CharUtils.isSpace(ch) || CharUtils.isAnyOf(ch, CharUtils.EOF + ".,|:()")) {
            return true;
        }
        return isPosAtRightDelim();
    }

    private String getText() {
        return input.substring(start, pos);
    }

    private int getColumn() {
        return start - lineStart + 1;
    }

    private void addToken(TokenType type) {
        addToken(type, getText(), start, line);
    }

    private void addToken(TokenType type, String value) {
        addToken(type, value, start, line);
    }

    private void addToken(TokenType type, String text, int start, int startLine) {
        Token token = new Token(type, text, start, startLine, getColumn());
        tokens.add(token);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public String getLeftDelimiter() {
        return leftDelimiter;
    }

    public String getRightDelimiter() {
        return rightDelimiter;
    }

    private interface State {

        State run();
    }
}
