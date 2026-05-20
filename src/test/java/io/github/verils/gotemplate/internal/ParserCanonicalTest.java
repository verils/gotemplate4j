package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.ParserTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserCanonicalTest {

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
            assertEquals("missing value: command", e.getMessage());
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


}
