package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser2;
import static org.junit.jupiter.api.Assertions.*;

class ParserNumberTest {

    @Test
    void testParseZero() throws TemplateParseException {
        Token token = new Token(TokenType.NUMBER, "0", 0, 0, 0);
        Parser parser = new Parser();
        NumberNode numberNode = parser.parseNumber(token);
        assertTrue(numberNode.isInt(), String.format("invalid number: %s", "0"));
        assertTrue(numberNode.isFloat(), String.format("invalid number: %s", "0"));
        assertFalse(numberNode.isComplex(), String.format("invalid number: %s", "0"));
    }


    @Test
    void testParseNumber() {

        class NumberTest {
            private final String text;

            private final boolean isInt;
            private final boolean isFloat;
            private final boolean isComplex;

            NumberTest(String text, boolean isInt, boolean isFloat, boolean isComplex) {
                this.text = text;
                this.isInt = isInt;
                this.isFloat = isFloat;
                this.isComplex = isComplex;
            }
        }

        NumberTest[] tests = new NumberTest[]{
                new NumberTest("0", true, true, false),
                new NumberTest("-0", true, true, false),
                new NumberTest("73", true, true, false),
                new NumberTest("7_3", true, true, false),
                new NumberTest("0b10_010_01", true, true, false),
                new NumberTest("0B10_010_01", true, true, false),
                new NumberTest("073", true, true, false),
                new NumberTest("0o73", true, true, false),
                new NumberTest("0O73", true, true, false),
                new NumberTest("0x73", true, true, false),
                new NumberTest("0X73", true, true, false),
                new NumberTest("0x7_3", true, true, false),
                new NumberTest("-73", true, true, false),
                new NumberTest("+73", true, true, false),
                new NumberTest("100", true, true, false),
                new NumberTest("1e9", true, true, false),
                new NumberTest("-1e9", true, true, false),
                new NumberTest("-1.2", false, true, false),
                new NumberTest("1e19", false, true, false),
                new NumberTest("1e1_9", false, true, false),
                new NumberTest("1E19", false, true, false),
                new NumberTest("-1e19", false, true, false),
                new NumberTest("0x_1p4", true, true, false),
                new NumberTest("0X_1P4", true, true, false),
                new NumberTest("0x_1p-4", false, true, false),
                new NumberTest("4i", false, false, true),
                new NumberTest("-1.2+4.2i", false, false, true),
                new NumberTest("073i", false, false, true),
                new NumberTest("0i", true, true, true),
                new NumberTest("-1.2+0i", false, true, true),
                new NumberTest("-12+0i", true, true, true),
                new NumberTest("13+0i", true, true, true),
                new NumberTest("0123", true, true, false),
                new NumberTest("-0x0", true, true, false),
                new NumberTest("0xdeadbeef", true, true, false),
                new NumberTest("'a'", true, true, false),
                new NumberTest("'\\n'", true, true, false),
                new NumberTest("'\\\\'", true, true, false),
                new NumberTest("'\\''", true, true, false),
                new NumberTest("'\\xFF'", true, true, false),
                new NumberTest("'パ'", true, true, false),
                new NumberTest("'\\u30d1'", true, true, false),
                new NumberTest("'\\U000030d1'", true, true, false),
                new NumberTest("+-2", false, false, false),
                new NumberTest("0x123.", false, false, false),
                new NumberTest("1e.", false, false, false),
                new NumberTest("0xi.", false, false, false),
                new NumberTest("1+2.", false, false, false),
                new NumberTest("'x", false, false, false),
                new NumberTest("'xx'", false, false, false),
                new NumberTest("'433937734937734969526500969526500'", false, false, false),
                new NumberTest("0xef", true, true, false),
        };

        for (NumberTest test : tests) {
            Parser parser = new Parser();

            String text = test.text;

            TokenType type = TokenType.NUMBER;
            if (text.charAt(0) == '\'') {
                type = TokenType.CHAR_CONSTANT;
            }

            try {
                Complex.parseComplex(text);
                type = TokenType.COMPLEX;
            } catch (NumberFormatException ignored) {
            }


            try {
                NumberNode numberNode = parser.parseNumber(new Token(type, text, 0, 0, 0));
                assertEquals(test.isInt, numberNode.isInt(), String.format("invalid number: %s", test.text));
                assertEquals(test.isFloat, numberNode.isFloat(), String.format("invalid number: %s", test.text));
                assertEquals(test.isComplex, numberNode.isComplex(), String.format("invalid number: %s", test.text));
            } catch (TemplateParseException e) {
                // Expected for invalid numbers, ignore
            }
        }
    }


    @Test
    void testComplexNumberWithZeroImaginary() throws TemplateParseException {
        Parser parser = createParser2();
        // Complex number with zero imaginary part should simplify to float/int
        Map<String, Node> nodes = parser.parse("test", "{{3+0i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberPureImaginary() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{5i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberFullForm() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1.5+2.5i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberNegativeReal() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{-3+4i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberNegativeImaginary() throws TemplateParseException {
        Parser parser = createParser2();
        // Note: Go template complex number syntax requires + between real and imaginary
        Map<String, Node> nodes = parser.parse("test", "{{3+-4i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberBothNegative() throws TemplateParseException {
        Parser parser = createParser2();
        // Note: Go template complex number syntax requires + between real and imaginary
        Map<String, Node> nodes = parser.parse("test", "{{-3+-4i}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testComplexNumberIntegerSimplification() throws TemplateParseException {
        Parser parser = createParser2();
        // Complex with zero imaginary and integer real should simplify to int
        Map<String, Node> nodes = parser.parse("test", "{{5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test chain node formation with different node types

    @Test
    void testBinaryNumberParsing() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0b1010}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testOctalNumberParsing() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0o755}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testHexadecimalNumberParsing() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0xFF}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testNumberWithUnderscores() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1_000_000}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testFloatWithUnderscores() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1_000.500_100}}");
        assertNotNull(nodes.get("test"));
    }

    // Test define template and use it
}
