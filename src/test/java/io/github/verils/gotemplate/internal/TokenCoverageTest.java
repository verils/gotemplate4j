package io.github.verils.gotemplate.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Token class to improve coverage
 */
class TokenCoverageTest {

    @Test
    void testTokenCreation() {
        Token token = new Token(TokenType.TEXT, "hello", 0, 1, 1);
        assertEquals(TokenType.TEXT, token.type());
        assertEquals("hello", token.value());
        assertEquals(0, token.pos());
        assertEquals(1, token.line());
        assertEquals(1, token.column());
    }

    @Test
    void testTokenToStringWithText() {
        Token token = new Token(TokenType.TEXT, "hello", 0, 1, 1);
        assertEquals("hello", token.toString());
    }

    @Test
    void testTokenToStringWithEOF() {
        Token token = new Token(TokenType.EOF, "", 10, 1, 11);
        assertEquals("EOF", token.toString());
    }

    @Test
    void testTokenToStringWithKeyword() {
        Token token = new Token(TokenType.KEYWORD, "if", 5, 1, 6);
        assertEquals("<if>", token.toString());
    }

    @Test
    void testTokenToStringWithError() {
        Token token = new Token(TokenType.ERROR, "error message", 0, 1, 1);
        assertEquals("error message", token.toString());
    }

    @Test
    void testTokenWithDifferentTypes() {
        // Test various token types
        Token numberToken = new Token(TokenType.NUMBER, "42", 0, 1, 1);
        assertEquals(TokenType.NUMBER, numberToken.type());
        
        Token stringToken = new Token(TokenType.STRING, "\"hello\"", 0, 1, 1);
        assertEquals(TokenType.STRING, stringToken.type());
        
        Token identifierToken = new Token(TokenType.IDENTIFIER, "myFunc", 0, 1, 1);
        assertEquals(TokenType.IDENTIFIER, identifierToken.type());
        
        Token pipeToken = new Token(TokenType.PIPE, "|", 0, 1, 1);
        assertEquals(TokenType.PIPE, pipeToken.type());
    }

    @Test
    void testTokenPositionTracking() {
        Token token1 = new Token(TokenType.TEXT, "hello", 0, 1, 1);
        Token token2 = new Token(TokenType.TEXT, "world", 6, 1, 7);
        Token token3 = new Token(TokenType.TEXT, "test", 12, 2, 1);
        
        assertEquals(0, token1.pos());
        assertEquals(6, token2.pos());
        assertEquals(12, token3.pos());
        
        assertEquals(1, token1.line());
        assertEquals(1, token2.line());
        assertEquals(2, token3.line());
        
        assertEquals(1, token1.column());
        assertEquals(7, token2.column());
        assertEquals(1, token3.column());
    }

    @Test
    void testTokenImmutability() {
        Token token = new Token(TokenType.TEXT, "original", 0, 1, 1);
        
        // Verify that token properties cannot be changed
        assertEquals("original", token.value());
        assertEquals(TokenType.TEXT, token.type());
    }
}
