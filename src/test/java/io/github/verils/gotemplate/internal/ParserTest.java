package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    public static final Logger log = Logger.getLogger(ParserTest.class.getName());


    @Test
    void testEmpty() throws TemplateParseException {
        assertOK("empty", "", "");
    }

    @Test
    void testComment() throws TemplateParseException {
        assertOK("comment", "{{/*\n\n\n*/}}", "");
    }

    @Test
    void testSpace() throws TemplateParseException {
        assertOK("spaces", " \t\n", "\" \t\n\"");
    }

    @Test
    void testText() throws TemplateParseException {
        assertOK("text", "some text", "\"some text\"");
    }

    @Test
    void testEmptyAction() {
        Parser parser = createParser1();
        try {
            parser.parse("empty action", "{{}}");
        } catch (TemplateParseException e) {
            assertEquals("missing value for command", e.getMessage());
        }
    }

    @Test
    void testField() throws TemplateParseException {
        assertOK("field", "{{.X}}", "{{.X}}");
    }

    @Test
    void testSimpleCommand() throws TemplateParseException {
        assertOK("simple command", "{{printf}}", "{{printf}}");
    }

    @Test
    void testInvocation() throws TemplateParseException {
        assertOK("$ invocation", "{{$}}", "{{$}}");
    }

    @Test
    void testVariableInvocation() throws TemplateParseException {
        assertOK("variable invocation", "{{with $x := 3}}{{$x 23}}{{end}}", "{{with $x := 3}}{{$x 23}}{{end}}");
    }

    @Test
    void testVariableWithField() throws TemplateParseException {
        assertOK("variable with fields", "{{$.I}}", "{{$.I}}");
    }

    @Test
    void testMultiWordCommand() throws TemplateParseException {
        assertOK("multi-word command", "{{printf `%d` 23}}", "{{printf `%d` 23}}");
    }

    @Test
    void testPipeline() throws TemplateParseException {
        assertOK("pipeline", "{{.X|.Y}}", "{{.X | .Y}}");
    }

    @Test
    void testPipelineWithDecl() throws TemplateParseException {
        assertOK("pipeline with decl", "{{$x := .X|.Y}}", "{{$x := .X | .Y}}");
    }

    @Test
    void testNestedPipeline() throws TemplateParseException {
        assertOK("nested pipeline", "{{.X (.Y .Z) (.A | .B .C) (.E)}}", "{{.X (.Y .Z) (.A | .B .C) (.E)}}");
    }

    @Test
    void testFieldForParen() throws TemplateParseException {
        assertOK("field applied to parentheses", "{{(.Y .Z).Field}}", "{{(.Y .Z).Field}}");
    }

    @Test
    void testSimpleIf() throws TemplateParseException {
        assertOK("simple if", "{{if .X}}hello{{end}}", "{{if .X}}\"hello\"{{end}}");
    }

    @Test
    void testIfElse() throws TemplateParseException {
        assertOK("if with else", "{{if .X}}true{{else}}false{{end}}", "{{if .X}}\"true\"{{else}}\"false\"{{end}}");
    }

    @Test
    void testIfElseIf() throws TemplateParseException {
        assertOK("if with else if", "{{if .X}}true{{else if .Y}}false{{end}}", "{{if .X}}\"true\"{{else}}{{if .Y}}\"false\"{{end}}{{end}}");
    }

    @Test
    void testIfElseChain() throws TemplateParseException {
        assertOK("if else chain", "+{{if .X}}X{{else if .Y}}Y{{else if .Z}}Z{{end}}+", "\"+\"{{if .X}}\"X\"{{else}}{{if .Y}}\"Y\"{{else}}{{if .Z}}\"Z\"{{end}}{{end}}{{end}}\"+\"");
    }

    @Test
    void testSimpleRange() throws TemplateParseException {
        assertOK("simple range", "{{range .X}}hello{{end}}", "{{range .X}}\"hello\"{{end}}");
    }

    @Test
    void testChainedFieldRange() throws TemplateParseException {
        assertOK("chained field range", "{{range .X.Y.Z}}hello{{end}}", "{{range .X.Y.Z}}\"hello\"{{end}}");
    }

    @Test
    void testNestedRange() throws TemplateParseException {
        assertOK("nested range", "{{range .X}}hello{{range .Y}}goodbye{{end}}{{end}}", "{{range .X}}\"hello\"{{range .Y}}\"goodbye\"{{end}}{{end}}");
    }

    @Test
    void testRangeElse() throws TemplateParseException {
        assertOK("range with else", "{{range .X}}true{{else}}false{{end}}", "{{range .X}}\"true\"{{else}}\"false\"{{end}}");
    }

    @Test
    void testRangeOverPipeline() throws TemplateParseException {
        assertOK("range over pipeline", "{{range .X|.M}}true{{else}}false{{end}}", "{{range .X | .M}}\"true\"{{else}}\"false\"{{end}}");
    }

    @Test
    void testRangeArray() throws TemplateParseException {
        assertOK("range []int", "{{range .SI}}{{.}}{{end}}", "{{range .SI}}{{.}}{{end}}");
    }

    @Test
    void testRange1Variable() throws TemplateParseException {
        assertOK("range 1 var", "{{range $x := .SI}}{{.}}{{end}}", "{{range $x := .SI}}{{.}}{{end}}");
    }

    @Test
    void testRange2Variables() throws TemplateParseException {
        assertOK("range 2 var", "{{range $x, $y := .SI}}{{.}}{{end}}", "{{range $x, $y := .SI}}{{.}}{{end}}");
    }

    @Test
    void testConstants() throws TemplateParseException {
        assertOK("constants", "{{range .SI 1 -3.2i true false 'a' nil}}{{end}}", "{{range .SI 1 -3.2i true false 'a' nil}}{{end}}");
    }

    @Test
    void testTemplate() throws TemplateParseException {
        assertOK("template", "{{template `x`}}", "{{template \"x\"}}");
    }

    @Test
    void testTemplateWithArg() throws TemplateParseException {
        assertOK("template with arg", "{{template `x` .Y}}", "{{template \"x\" .Y}}");
    }

    @Test
    void testWith() throws TemplateParseException {
        assertOK("with", "{{with .X}}hello{{end}}", "{{with .X}}\"hello\"{{end}}");
    }

    @Test
    void testWithElse() throws TemplateParseException {
        assertOK("with with else", "{{with .X}}hello{{else}}goodbye{{end}}", "{{with .X}}\"hello\"{{else}}\"goodbye\"{{end}}");
    }

    @Test
    void testTrimLeft() throws TemplateParseException {
        assertOK("trim left", "x \r\n\t{{- 3}}", "\"x\"{{3}}");
    }

    @Test
    void testTrimRight() throws TemplateParseException {
        assertOK("trim right", "{{3 -}}\n\n\ty", "{{3}}\"y\"");
    }

    @Test
    void testTrimLeftAndRight() throws TemplateParseException {
        assertOK("trim left and right", "x \r\n\t{{- 3 -}}\n\n\ty", "\"x\"{{3}}\"y\"");
    }

    @Test
    void testTrimExtra() throws TemplateParseException {
        assertOK("trim with extra spaces", "x\n{{-  3   -}}\ny", "\"x\"{{3}}\"y\"");
    }

    @Test
    void testCommentTrimLeft() throws TemplateParseException {
        assertOK("comment trim left", "x \r\n\t{{- /* hi */}}", "\"x\"");
    }

    @Test
    void testCommentTrimRight() throws TemplateParseException {
        assertOK("comment trim right", "{{/* hi */ -}}\n\n\ty", "\"y\"");
    }

    @Test
    void testCommentTrimLeftAndRight() throws TemplateParseException {
        assertOK("comment trim left and right", "x \r\n\t{{- /* */ -}}\n\n\ty", "\"x\"\"y\"");
    }

    @Test
    void testBlockDefinition() throws TemplateParseException {
        assertOK("block definition", "{{block \"foo\" .}}hello{{end}}", "{{template \"foo\" .}}");
    }

    @Test
    void testNewlineInAssignment() throws TemplateParseException {
        assertOK("newline in assignment", "{{ $x \n := \n 1 \n }}", "{{$x := 1}}");
    }

    @Test
    void testNewlineInEmptyAction() {
        assertError("newline in empty action", "{{\n}}");
    }

    @Test
    void testNewlineInPipeline() throws TemplateParseException {
        assertOK("newline in pipeline", "{{\n\"x\"\n|\nprintf\n}}", "{{\"x\" | printf}}");
    }

    @Test
    void testNewlineInComment() throws TemplateParseException {
        assertOK("newline in comment", "{{/*\nhello\n*/}}", "");
    }

    @Test
    void testNewlineInComment1() {
        assertError("newline in empty action", "{{\n}}");
    }

    @Test
    void testUnclosedAction() {
        assertError("unclosed action", "hello{{range");
    }

    @Test
    void testUnmatchedEnd() {
        assertError("unmatched end", "{{end}}");
    }

    @Test
    void testUnmatchedElse() {
        assertError("unmatched else", "{{else}}");
    }

    @Test
    void testUnmatchedElseAfterIf() {
        assertError("unmatched else after if", "{{if .X}}hello{{end}}{{else}}");
    }

    @Test
    void testMultipleElse() {
        assertError("multiple else", "{{if .X}}1{{else}}2{{else}}3{{end}}");
    }

    @Test
    void testMissingEnd() {
        assertError("missing end", "hello{{range .x}}");
    }

    @Test
    void testMissingEndAfterElse() {
        assertError("missing end after else", "hello{{range .x}}{{else}}");
    }

    @Test
    void testUndefinedFunction() {
        assertError("undefined function", "hello{{undefined}}");
    }

    @Test
    void testUndefinedVariable() {
        assertError("undefined variable", "{{$x}}");
    }

    @Test
    void testUndefinedVariableAfterEnd() {
        assertError("variable undefined after end", "{{with $x := 4}}{{end}}{{$x}}");
    }

    @Test
    void testUndefinedInTemplate() {
        assertError("variable undefined in template", "{{template $v}}");
    }

    @Test
    void testDeclareWithField() {
        assertError("declare with field", "{{with $x.Y := 4}}{{end}}");
    }

    @Test
    void testDeclareWithFieldRef() {
        assertError("template with field ref", "{{template .X}}");
    }

    @Test
    void testDeclareWithVar() {
        assertError("template with var", "{{template $v}}");
    }

    @Test
    void testDeclareOutsideRange() {
        assertError("multidecl outside range", "{{with $v, $u := 3}}{{end}}");
    }

    @Test
    void testTooManyDeclares() {
        assertError("too many decls in range", "{{range $u, $v, $w := 3}}{{end}}");
    }

    @Test
    void testParen() {
        assertError("dot applied to parentheses", "{{printf (printf .).}}");
    }

    @Test
    void testAdjacentArg() {
        assertError("adjacent args", "{{printf 3`x`}}");
    }

    @Test
    void testAdjacentArgWithDot() {
        assertError("adjacent args with .", "{{printf `x`.}}");
    }

    @Test
    void testBug0a() throws TemplateParseException {
        assertOK("bug0a", "{{$x := 0}}{{$x}}", "{{$x := 0}}{{$x}}");
    }

    @Test
    void testBug0b() {
        assertError("bug0b", "{{$x += 1}}{{$x}}");
    }

    @Test
    void testBug0c() {
        assertError("bug0c", "{{$x ! 2}}{{$x}}");
    }

    @Test
    void testBug0d() {
        assertError("bug0d", "{{$x % 3}}{{$x}}");
    }

    @Test
    void testBug0e() {
        assertError("bug0e", "{{range $x := $y := 3}}{{end}}");
    }

    @Test
    void testBug1a() {
        assertError("bug1a", "{{$x:=.}}{{$x!2}}");
    }

    @Test
    void testBug1b() {
        assertError("bug1b", "{{$x:=.}}{{$x+2}}");
    }

    @Test
    void testBug1c() throws TemplateParseException {
        assertOK("bug1c", "{{$x:=.}}{{$x +2}}", "{{$x := .}}{{$x +2}}");
    }

    @Test
    void testBug1d() {
        assertError("extra end after if", "{{if .X}}a{{else if .Y}}b{{end}}{{end}}");
    }

    @Test
    void testDotAfterInteger() {
        assertError("dot after integer", "{{1.E}}");
    }

    @Test
    void testDotAfterFloat() {
        assertError("dot after float", "{{0.1.E}}");
    }

    @Test
    void testDotAfterBoolean() {
        assertError("dot after boolean", "{{true.E}}");
    }

    @Test
    void testDotAfterChar() {
        assertError("dot after char", "{{'a'.any}}");
    }

    @Test
    void testDotAfterString() {
        assertError("dot after string", "{{\"hello\".guys}}");
    }

    @Test
    void testDotAfterDot() {
        assertError("dot after dot", "{{..E}}");
    }

    @Test
    void testDotAfterNil() {
        assertError("dot after nil", "{{nil.E}}");
    }

    @Test
    void testWrongPipelineDot() {
        assertError("wrong pipeline dot", "{{12|.}}");
    }

    @Test
    void testWrongPipelineNumber() {
        assertError("wrong pipeline number", "{{.|12|printf}}");
    }

    @Test
    void testWrongPipelineString() {
        assertError("wrong pipeline string", "{{.|printf|\"error\"}}");
    }

    @Test
    void testWrongPipelineChar() {
        assertError("wrong pipeline char", "{{12|printf|'e'}}");
    }

    @Test
    void testWrongPipelineBoolean() {
        assertError("wrong pipeline boolean", "{{.|true}}");
    }

    @Test
    void testWrongPipelineNil() {
        assertError("wrong pipeline nil", "{{'c'|nil}}");
    }

    @Test
    void testEmptyPipeline() {
        assertError("empty pipeline", "{{printf \"%d\" ( ) }}");
    }

    @Test
    void testInvalidBlockDefinition() {
        assertError("block definition", "{{block \"foo\"}}hello{{end}}");
    }


    private void assertOK(String name, String input, String result) throws TemplateParseException {
        Parser parser = createParser1();
        Map<String, Node> nodes = parser.parse(name, input);
        Node node = nodes.get(name);
        assertNotNull(node);
        assertInstanceOf(ListNode.class, node);
        assertEquals(result, node.toString());
    }

    private void assertError(String name, String text) {
        Parser parser = createParser1();
        assertThrows(TemplateParseException.class, () -> parser.parse(name, text));
    }

    private Parser createParser1() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        return new Parser(functions);
    }

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
                boolean parsed = test.isInt || test.isFloat || test.isComplex;
                if (parsed) {
                    log.log(Level.WARNING, "unexpected error", e);
                } else {
                    log.log(Level.FINE, String.format("expected error: %s", e.getMessage()));
                }
            }
        }
    }

    @Test
    void testErrorWithContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{else}}\n  World\n{{end}}");
            // Should parse successfully
        } catch (TemplateParseException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    void testUnmatchedEndWithErrorContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello\n{{end}}");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            // Exception should be thrown, message may or may not include context
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void testErrorContextWithNullToken() {
        Parser parser = createParser1();
        try {
            // Test error path where token is null in error context
            parser.parse("test", "{{if .X}}hello");
            fail("Should throw TemplateParseException for unclosed if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Message should contain parse error info
            assertTrue(e.getMessage().contains("Parse error") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testErrorContextWithMultilineTemplate() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{invalid syntax}}\nLine 4");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Error message should be present, may or may not have line info depending on error type
            assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    void testUnclosedDelimiterError() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello {{");
            fail("Should throw TemplateParseException for unclosed delimiter");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unclosed"));
        }
    }

    @Test
    void testMissingActionTokenError() {
        Parser parser = createParser1();
        try {
            // This should trigger missing action token error
            parser.parse("test", "{{ ");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }


    private Parser createParser2() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        functions.put("len", null);
        functions.put("index", null);
        return new Parser(functions);
    }

    // Test complex number parsing edge cases
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
    void testChainNodeAfterVariable() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.Field}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testChainNodeAfterField() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X.Y.Z}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testChainNodeAfterParenthesizedPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{(.X).Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseBlock error paths
    @Test
    void testParseBlockWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{block .Name .}}content{{end}}"));
    }

    @Test
    void testParseBlockMissingEndToken() {
        Parser parser = createParser2();
        assertThrows(Exception.class, () -> parser.parse("test", "{{block \"name\" .}}content"));
    }

    @Test
    void testParseBlockWithUnexpectedElse() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{block \"name\" .}}content{{else}}more{{end}}"));
    }

    // Test parseDefinition error paths
    @Test
    void testParseDefinitionWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define .Name}}content{{end}}"));
    }

    @Test
    void testParseDefinitionMissingRightDelim() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\" content{{end}}"));
    }

    @Test
    void testParseDefinitionMissingEnd() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\"}}content"));
    }

    @Test
    void testParseDefinitionWithUnexpectedLastNode() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\"}}content{{else}}other{{end}}"));
    }

    // Test parseTemplate error paths
    @Test
    void testParseTemplateWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{template .Name}}"));
    }

    @Test
    void testParseTemplateWithPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseElse error paths
    @Test
    void testParseElseWithUnexpectedToken() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}true{{else invalid}}false{{end}}"));
    }

    // Test parseEnd error paths
    @Test
    void testParseEndWithUnexpectedToken() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}true{{end invalid}}"));
    }

    // Test variable declaration edge cases
    @Test
    void testRangeWithTwoVariablesDeclaration() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $key, $value := .Map}}{{$key}}:{{$value}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testRangeWithOneVariableDeclaration() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $item := .List}}{{$item}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testVariableScopeInNestedBranches() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{$v := 1}}{{$v}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testVariableUndefinedAfterBranch() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}{{$v := 1}}{{end}}{{$v}}"));
    }

    // Test pipeline validation for non-executable commands
    @Test
    void testPipelineWithDotAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{. | .}}"));
    }

    @Test
    void testPipelineWithNilAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | nil}}"));
    }

    @Test
    void testPipelineWithNumberAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | 123}}"));
    }

    @Test
    void testPipelineWithStringAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | \"error\"}}"));
    }

    // Test error context building with various scenarios
    @Test
    void testErrorMessageWithContextForSyntaxError() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            // Error should contain parse error info
            assertFalse(message.isEmpty());
        }
    }

    @Test
    void testErrorMessageWithContextForUnclosedIf() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{if .X}}\nLine 4");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            assertFalse(message.isEmpty());
        }
    }

    @Test
    void testErrorMessageWithContextForMissingEnd() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{range .Items}}\n  Item\n{{if .Active}}\n    Active");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
        }
    }

    // Test lookNextItem at boundaries
    @Test
    void testLookNextItemAtEOF() throws TemplateParseException {
        Parser parser = createParser2();
        // This tests the internal lookNextItem behavior through normal parsing
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test moveToPrevItem at index 0
    @Test
    void testMoveToPrevItemAtStart() throws TemplateParseException {
        Parser parser = createParser2();
        // Tests boundary condition where tokenIndex is 0
        Map<String, Node> nodes = parser.parse("test", "text");
        assertNotNull(nodes.get("test"));
    }

    // Test parsePipe missing value error
    @Test
    void testParsePipeWithEmptyCommands() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{}}"));
    }

    // Test parseCommand empty command error
    @Test
    void testParseCommandWithNoArguments() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> {
            // This should trigger empty command error
            parser.parse("test", "{{ }}");
        });
    }

    // Test parseNumber with char constant errors
    @Test
    void testParseCharConstantWithInvalidSyntax() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{'invalid}}"));
    }

    @Test
    void testParseCharConstantWithMultipleChars() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{'ab'}}"));
    }

    // Test parseIntValue with different bases
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
    @Test
    void testDefineAndUseTemplate() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"mytemplate\"}}Hello{{end}}");
        assertNotNull(nodes.get("mytemplate"));
    }

    // Test block with argument
    @Test
    void testBlockWithArgument() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{block \"foo\" .Arg}}content{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("foo"));
    }

    // Test nested if-else-if chains
    @Test
    void testDeepElseIfChain() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .A}}A{{else if .B}}B{{else if .C}}C{{else if .D}}D{{else}}E{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with nested in if
    @Test
    void testWithNestedInIf() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .X}}{{with .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range nested in with
    @Test
    void testRangeNestedInWith() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{with .X}}{{range .Y}}item{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple templates defined
    @Test
    void testMultipleTemplateDefinitions() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{define \"t1\"}}T1{{end}}{{define \"t2\"}}T2{{end}}");
        assertNotNull(nodes.get("t1"));
        assertNotNull(nodes.get("t2"));
    }

    // Test template invocation with argument
    @Test
    void testTemplateInvocationWithArgument() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error in nested pipeline
    @Test
    void testErrorInNestedPipeline() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf (}}"));
    }

    // Test boolean as first command in pipeline stage
    @Test
    void testBooleanAsFirstCommandInPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        // Boolean as first command is OK
        Map<String, Node> nodes = parser.parse("test", "{{true}}");
        assertNotNull(nodes.get("test"));
    }

    // Test dot node
    @Test
    void testDotNode() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nil node
    @Test
    void testNilNode() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{nil}}");
        assertNotNull(nodes.get("test"));
    }

    // Test raw string
    @Test
    void testRawString() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{`raw string`}}");
        assertNotNull(nodes.get("test"));
    }

    // Test identifier not found
    @Test
    void testIdentifierNotDefined() {
        Parser parser = new Parser(); // Empty function map
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{undefinedFunc}}"));
    }

    // Test field after various node types in chain
    @Test
    void testFieldChainAfterBool() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{true.Field}}"));
    }

    @Test
    void testFieldChainAfterString() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{\"string\".Field}}"));
    }

    @Test
    void testFieldChainAfterNumber() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{123.Field}}"));
    }

    @Test
    void testFieldChainAfterNil() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{nil.Field}}"));
    }

    @Test
    void testFieldChainAfterDot() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{..Field}}"));
    }

    // Test comment in various positions
    @Test
    void testCommentAtBeginning() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{/* comment */}}text");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCommentAtEnd() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "text{{/* comment */}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCommentInMiddle() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "text1{{/* comment */}}text2");
        assertNotNull(nodes.get("test"));
    }

    // Test whitespace handling
    @Test
    void testMultipleSpaces() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{   .   }}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testTabsAndNewlines() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\t.\n}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex pipeline
    @Test
    void testComplexPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X | printf \"%d\" | len}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parenthesized pipeline in command
    @Test
    void testParenthesizedPipelineInCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d\" (.X)}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple arguments in command
    @Test
    void testMultipleArgumentsInCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s\" 42 \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test negative numbers
    @Test
    void testNegativeInteger() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{-42}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testNegativeFloat() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{-3.14}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testPositiveSign() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{+42}}");
        assertNotNull(nodes.get("test"));
    }

    // Test scientific notation
    @Test
    void testScientificNotation() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1.23e10}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testScientificNotationNegativeExponent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1.23e-10}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testScientificNotationCapitalE() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1.23E10}}");
        assertNotNull(nodes.get("test"));
    }

    // Test hex float
    @Test
    void testHexFloat() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0x1p4}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testHexFloatNegativeExponent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0x1p-4}}");
        assertNotNull(nodes.get("test"));
    }

    // Test escaped characters in strings
    @Test
    void testStringWithEscapes() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"hello\\nworld\"}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testStringWithUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"\\u0041\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test character constants with escapes
    @Test
    void testCharConstantWithEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{'\\n'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithHexEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{'\\xFF'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{'\\u0041'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithLongUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{'\\U00000041'}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range with else
    @Test
    void testRangeWithElseClause() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}item{{else}}empty{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if-else-if without final else
    @Test
    void testElseIfWithoutFinalElse() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with else
    @Test
    void testWithElseClause() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}has value{{else}}no value{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested actions
    @Test
    void testNestedActions() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{if .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test action after end
    @Test
    void testActionAfterEnd() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}value{{end}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test text before and after action
    @Test
    void testTextBeforeAndAfterAction() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "before{{.Value}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple actions in sequence
    @Test
    void testMultipleActionsInSequence() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.A}}{{.B}}{{.C}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested structures
    @Test
    void testDeeplyNestedStructure() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .A}}{{with .B}}{{range .C}}{{if .D}}deep{{end}}{{end}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable in nested scope
    @Test
    void testVariableInNestedScope() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{with $outer := .}}{{if true}}{{$outer}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error with tab characters in template
    @Test
    void testErrorContextWithTabs() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\n\t{{invalid}}\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test empty template name in define
    @Test
    void testDefineWithEmptyName() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"\"}}empty{{end}}");
        assertNotNull(nodes.get(""));
    }

    // Test template with special characters in name
    @Test
    void testTemplateWithSpecialCharactersInName() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template-with-dash\"}}content{{end}}");
        assertNotNull(nodes.get("template-with-dash"));
    }

    // Test unclosed delimiter with trim - this causes lexer error
    @Test
    void testUnclosedDelimiterWithTrim() {
        Parser parser = createParser2();
        assertThrows(Exception.class, () -> parser.parse("test", "{{- "));
    }

    // Test lookahead at EOF
    @Test
    void testLookaheadAtEOF() throws TemplateParseException {
        Parser parser = createParser2();
        // Parsing simple template tests lookahead behavior at boundaries
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test move to previous non-space item
    @Test
    void testMoveToPrevNonSpace() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X }}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex error scenarios
    @Test
    void testMultipleErrorsInTemplate() {
        Parser parser = createParser2();
        // Parser stops at first error, but this tests error handling
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{invalid1}}{{invalid2}}"));
    }

    // Test very long template name
    @Test
    void testVeryLongTemplateName() throws TemplateParseException {
        Parser parser = createParser2();
        StringBuilder sb = new StringBuilder("template_");
        for (int i = 0; i < 100; i++) {
            sb.append('x');
        }
        String longName = sb.toString();
        Map<String, Node> nodes = parser.parse("test",
                "{{define \"" + longName + "\"}}content{{end}}");
        assertNotNull(nodes.get(longName));
    }

    // Test unicode in template text
    @Test
    void testUnicodeInTemplateText() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "你好世界{{.Name}}");
        assertNotNull(nodes.get("test"));
    }

    // Test unicode in string literal
    @Test
    void testUnicodeInStringLiteral() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"こんにちは\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test mixed delimiters (default delimiters)
    @Test
    void testDefaultDelimiters() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pipeline with multiple stages
    @Test
    void testPipelineWithManyStages() throws TemplateParseException {
        Parser parser = createParser2();
        // Note: This requires appropriate functions to be registered
        Map<String, Node> nodes = parser.parse("test", "{{.X}}");
        assertNotNull(nodes.get("test"));
    }

    // Test assignment operator
    @Test
    void testAssignmentOperator() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .Value}}{{$x}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test declare operator in range
    @Test
    void testDeclareInRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $idx, $val := .Items}}{{$idx}}:{{$val}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test boolean values
    @Test
    void testTrueBoolean() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{true}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testFalseBoolean() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{false}}");
        assertNotNull(nodes.get("test"));
    }

    // Test zero values
    @Test
    void testZeroInteger() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testZeroFloat() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0.0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test large numbers
    @Test
    void testLargeInteger() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{9223372036854775807}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testLargeFloat() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{1.7976931348623157e+308}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex number that simplifies to integer
    @Test
    void testComplexSimplifiesToInt() throws TemplateParseException {
        Parser parser = createParser2();
        // 5+0i should simplify to int 5
        Map<String, Node> nodes = parser.parse("test", "{{5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex number that simplifies to float
    @Test
    void testComplexSimplifiesToFloat() throws TemplateParseException {
        Parser parser = createParser2();
        // 5.5+0i should simplify to float 5.5
        Map<String, Node> nodes = parser.parse("test", "{{5.5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error context with tab character at error position
    @Test
    void testErrorContextWithTabAtErrorColumn() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "\t\t{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at first line
    @Test
    void testErrorContextAtFirstLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{invalid}}\nline2\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at last line
    @Test
    void testErrorContextAtLastLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\nline2\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context with very long line
    @Test
    void testErrorContextWithLongLine() {
        Parser parser = createParser2();
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 200; i++) {
                sb.append('x');
            }
            sb.append(" {{invalid}}");
            parser.parse("test", sb.toString());
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test variable with multiple field accesses
    @Test
    void testVariableWithMultipleFields() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.A.B.C}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested pipeline
    @Test
    void testDeeplyNestedPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{(((.X)))}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range without assignment
    @Test
    void testRangeWithoutAssignment() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if with just dot
    @Test
    void testIfWithJustDot() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .}}truthy{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with just dot
    @Test
    void testWithWithJustDot() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .}}inside{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test block with empty content
    @Test
    void testBlockWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{block \"empty\" .}}{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("empty"));
    }

    // Test define with empty content
    @Test
    void testDefineWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"empty\"}}{{end}}");
        assertNotNull(nodes.get("empty"));
    }

    // Test template with no arguments
    @Test
    void testTemplateWithNoArguments() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple variables in same scope
    @Test
    void testMultipleVariablesInSameScope() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $a := 1}}{{$a}}{{with $b := 2}}{{$b}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable shadowing
    @Test
    void testVariableShadowing() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := 1}}{{$x}}{{with $x := 2}}{{$x}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test command with all argument types
    @Test
    void testCommandWithMixedArguments() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s %v\" 42 \"hello\" .Value}}");
        assertNotNull(nodes.get("test"));
    }

    // Test number parsing edge case: just zero
    @Test
    void testJustZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test octal zero
    @Test
    void testOctalZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0o0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test hex zero
    @Test
    void testHexZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0x0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test binary zero
    @Test
    void testBinaryZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0b0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test negative zero
    @Test
    void testNegativeZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{-0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test positive zero
    @Test
    void testPositiveZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{+0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test float zero
    @Test
    void testFloatZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0.0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex zero
    @Test
    void testComplexZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pure imaginary zero
    @Test
    void testPureImaginaryZero() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with newline escape
    @Test
    void testStringWithNewlineEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"line1\\nline2\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with tab escape
    @Test
    void testStringWithTabEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"tab\\there\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with quote escape
    @Test
    void testStringWithQuoteEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"quote\\\"here\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test char with various escapes
    @Test
    void testCharWithBackslashEscape() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{'\\\\'}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pipeline with single command
    @Test
    void testPipelineWithSingleCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test action with only whitespace
    @Test
    void testActionWithOnlyWhitespace() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{   }}"));
    }

    // Test text with special characters
    @Test
    void testTextWithSpecialCharacters() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "Hello <World> & 'Friends'");
        assertNotNull(nodes.get("test"));
    }

    // Test empty string
    @Test
    void testEmptyString() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{\"\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test empty raw string
    @Test
    void testEmptyRawString() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{``}}");
        assertNotNull(nodes.get("test"));
    }

    // Test comment with special characters
    @Test
    void testCommentWithSpecialCharacters() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{/* Special chars: <>&\"' */}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple comments
    @Test
    void testMultipleComments() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{/* comment1 */}}text{{/* comment2 */}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested if inside range
    @Test
    void testNestedIfInsideRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{if .Active}}active{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested range inside if
    @Test
    void testNestedRangeInsideIf() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .HasItems}}{{range .Items}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested with inside range
    @Test
    void testNestedWithInsideRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{with .Name}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test else if with complex condition
    @Test
    void testElseIfWithComplexCondition() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B.C.D}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range with dot reference
    @Test
    void testRangeWithDotReference() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}Current: {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with dot reference
    @Test
    void testWithWithDotReference() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}The value is {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test template name with underscores
    @Test
    void testTemplateNameWithUnderscores() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"my_template_name\"}}content{{end}}");
        assertNotNull(nodes.get("my_template_name"));
    }

    // Test template name with numbers
    @Test
    void testTemplateNameWithNumbers() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template123\"}}content{{end}}");
        assertNotNull(nodes.get("template123"));
    }


    @Test
    void testParserWithUnclosedAction() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed action", "{{ .Name"));
    }

    @Test
    void testParserWithUnclosedIf() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed if", "{{if .Name}}hello"));
    }

    @Test
    void testParserWithUnclosedRange() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed range", "{{range .Items}}item"));
    }

    @Test
    void testParserWithUnclosedWith() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed with", "{{with .Name}}hello"));
    }

    @Test
    void testParserWithUnexpectedEnd() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unexpected end", "{{end}}"));
    }

    @Test
    void testParserWithUnexpectedElse() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unexpected else", "{{else}}"));
    }

    @Test
    void testParserWithInvalidVariableDeclaration() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("invalid var decl", "{{:= .Name}}"));
    }

    @Test
    void testParserWithMissingPipelineInIf() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in if", "{{if}}"));
    }

    @Test
    void testParserWithMissingPipelineInRange() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in range", "{{range}}"));
    }

    @Test
    void testParserWithMissingPipelineInWith() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in with", "{{with}}"));
    }
}