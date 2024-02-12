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
        Parser parser = createParser();
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
    void testDotAfterInteger() throws TemplateParseException {
        assertError("dot after integer", "{{1.E}}");
    }

    @Test
    void testDotAfterFloat() throws TemplateParseException {
        assertError("dot after float", "{{0.1.E}}");
    }

    @Test
    void testDotAfterBoolean() throws TemplateParseException {
        assertError("dot after boolean", "{{true.E}}");
    }

    @Test
    void testDotAfterChar() throws TemplateParseException {
        assertError("dot after char", "{{'a'.any}}");
    }

    @Test
    void testDotAfterString() throws TemplateParseException {
        assertError("dot after string", "{{\"hello\".guys}}");
    }

    @Test
    void testDotAfterDot() throws TemplateParseException {
        assertError("dot after dot", "{{..E}}");
    }

    @Test
    void testDotAfterNil() throws TemplateParseException {
        assertError("dot after nil", "{{nil.E}}");
    }

    @Test
    void testWrongPipelineDot() throws TemplateParseException {
        assertError("wrong pipeline dot", "{{12|.}}");
    }

    @Test
    void testWrongPipelineNumber() throws TemplateParseException {
        assertError("wrong pipeline number", "{{.|12|printf}}");
    }

    @Test
    void testWrongPipelineString() throws TemplateParseException {
        assertError("wrong pipeline string", "{{.|printf|\"error\"}}");
    }

    @Test
    void testWrongPipelineChar() throws TemplateParseException {
        assertError("wrong pipeline char", "{{12|printf|'e'}}");
    }

    @Test
    void testWrongPipelineBoolean() throws TemplateParseException {
        assertError("wrong pipeline boolean", "{{.|true}}");
    }

    @Test
    void testWrongPipelineNil() throws TemplateParseException {
        assertError("wrong pipeline nil", "{{'c'|nil}}");
    }

    @Test
    void testEmptyPipeline() throws TemplateParseException {
        assertError("empty pipeline", "{{printf \"%d\" ( ) }}");
    }

    @Test
    void testInvalidBlockDefinition() throws TemplateParseException {
        assertError("block definition", "{{block \"foo\"}}hello{{end}}");
    }


    private void assertOK(String name, String input, String result) throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse(name, input);
        Node node = nodes.get(name);
        assertNotNull(node);
        assertInstanceOf(ListNode.class, node);
        assertEquals(result, node.toString());
    }

    private void assertError(String name, String text) {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> parser.parse(name, text));
    }

    private Parser createParser() {
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
            NumberNode numberNode = new NumberNode(text);

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
                numberNode = parser.parseNumber(new Token(type, text, 0, 0, 0));
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

}