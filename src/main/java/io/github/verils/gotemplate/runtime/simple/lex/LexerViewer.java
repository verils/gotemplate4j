package io.github.verils.gotemplate.runtime.simple.lex;

import io.github.verils.gotemplate.internal.Token;
import io.github.verils.gotemplate.internal.TokenType;

public class LexerViewer {

    private final String input;
    private final Token[] tokens;
    private int index;

    public LexerViewer(String input, Token[] tokens) {
        this.input = input;
        this.tokens = tokens;
    }


    public String getInput() {
        return input;
    }

    /**
     * 获取下一个元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Token lookNextItem() {
        if (index < tokens.length) {
            return tokens[index];
        }
        return null;
    }

    /**
     * 获取下一个元素，并将查找标记后移一位，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Token nextItem() {
        Token token = lookNextItem();
        if (token != null) {
            index++;
        }
        return token;
    }

    /**
     * 获取下一个非空白元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Token lookNextNonSpaceItem() {
        int count = 0;
        while (true) {
            Token token = nextItem();
            count++;
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                index -= count;
                return token;
            }
        }
    }

    /**
     * 获取下一个非空白元素，并将查找标记后移到这个元素后，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Token nextNonSpaceItem() {
        while (true) {
            Token token = nextItem();
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                return token;
            }
        }
    }

    /**
     * 获取上一个元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    public Token prevItem() {
        if (index > 0) {
            return tokens[--index];
        }
        return null;
    }

    /**
     * 获取上一个非空白元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    public Token prevNonSpaceItem() {
        while (true) {
            Token token = prevItem();
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                return token;
            }
        }
    }
}
