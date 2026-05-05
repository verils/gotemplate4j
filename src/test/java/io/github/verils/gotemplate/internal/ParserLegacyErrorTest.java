package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
class ParserLegacyErrorTest extends ParserTestSupport {

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

}
