package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void test() {
        class Test {
            private final String name;

            private final String input;
            private final String result;

            private final boolean error;
            private final String errorMessage;

            public Test(String name, String input, String result, boolean error, String errorMessage) {
                this.name = name;
                this.input = input;
                this.result = result;
                this.error = error;
                this.errorMessage = errorMessage;
            }
        }

        Test[] tests = {
                new Test("empty", "", "", false, null),
                new Test("comment", "{{/*\n\n\n*/}}", "", false, null),
                new Test("spaces", " \t\n", "\" \t\n\"", false, null),
                new Test("text", "some text", "\"some text\"", false, null),
                new Test("empty action", "{{}}", "{{}}", true, "missing value for command"),
                new Test("field", "{{.X}}", "{{.X}}", false, null),
                new Test("simple command", "{{printf}}", "{{printf}}", false, null),
                new Test("$ invocation", "{{$}}", "{{$}}", false, null),
                new Test("variable invocation", "{{with $x := 3}}{{$x 23}}{{end}}", "{{with $x := 3}}{{$x 23}}{{end}}", false, null),
                new Test("variable with fields", "{{$.I}}", "{{$.I}}", false, null),
                new Test("multi-word command", "{{printf `%d` 23}}", "{{printf `%d` 23}}", false, null),
                new Test("pipeline", "{{.X|.Y}}", "{{.X | .Y}}", false, null),
                new Test("pipeline with decl", "{{$x := .X|.Y}}", "{{$x := .X | .Y}}", false, null),
                new Test("nested pipeline", "{{.X (.Y .Z) (.A | .B .C) (.E)}}", "{{.X (.Y .Z) (.A | .B .C) (.E)}}", false, null),
                new Test("field applied to parentheses", "{{(.Y .Z).Field}}", "{{(.Y .Z).Field}}", false, null),
                new Test("simple if", "{{if .X}}hello{{end}}", "{{if .X}}\"hello\"{{end}}", false, null),
                new Test("if with else", "{{if .X}}true{{else}}false{{end}}", "{{if .X}}\"true\"{{else}}\"false\"{{end}}", false, null),
                new Test("if with else if", "{{if .X}}true{{else if .Y}}false{{end}}", "{{if .X}}\"true\"{{else}}{{if .Y}}\"false\"{{end}}{{end}}", false, null),
                new Test("if else chain", "+{{if .X}}X{{else if .Y}}Y{{else if .Z}}Z{{end}}+", "\"+\"{{if .X}}\"X\"{{else}}{{if .Y}}\"Y\"{{else}}{{if .Z}}\"Z\"{{end}}{{end}}{{end}}\"+\"", false, null),
                new Test("simple range", "{{range .X}}hello{{end}}", "{{range .X}}\"hello\"{{end}}", false, null),
                new Test("chained field range", "{{range .X.Y.Z}}hello{{end}}", "{{range .X.Y.Z}}\"hello\"{{end}}", false, null),
                new Test("nested range", "{{range .X}}hello{{range .Y}}goodbye{{end}}{{end}}", "{{range .X}}\"hello\"{{range .Y}}\"goodbye\"{{end}}{{end}}", false, null),
                new Test("range with else", "{{range .X}}true{{else}}false{{end}}", "{{range .X}}\"true\"{{else}}\"false\"{{end}}", false, null),
                new Test("range over pipeline", "{{range .X|.M}}true{{else}}false{{end}}", "{{range .X | .M}}\"true\"{{else}}\"false\"{{end}}", false, null),
                new Test("range []int", "{{range .SI}}{{.}}{{end}}", "{{range .SI}}{{.}}{{end}}", false, null),
                new Test("range 1 var", "{{range $x := .SI}}{{.}}{{end}}", "{{range $x := .SI}}{{.}}{{end}}", false, null),
                new Test("range 2 var", "{{range $x, $y := .SI}}{{.}}{{end}}", "{{range $x, $y := .SI}}{{.}}{{end}}", false, null),
                new Test("constants", "{{range .SI 1 -3.2i true false 'a' nil}}{{end}}", "{{range .SI 1 -3.2i true false 'a' nil}}{{end}}", false, null),
                new Test("template", "{{template `x`}}", "{{template \"x\"}}", false, null),
                new Test("template with arg", "{{template `x` .Y}}", "{{template \"x\" .Y}}", false, null),
                new Test("with", "{{with .X}}hello{{end}}", "{{with .X}}\"hello\"{{end}}", false, null),
                new Test("with with else", "{{with .X}}hello{{else}}goodbye{{end}}", "{{with .X}}\"hello\"{{else}}\"goodbye\"{{end}}", false, null),

                new Test("trim left", "x \r\n\t{{- 3}}", "\"x\"{{3}}", false, null),
                new Test("trim right", "{{3 -}}\n\n\ty", "{{3}}\"y\"", false, null),
                new Test("trim left and right", "x \r\n\t{{- 3 -}}\n\n\ty", "\"x\"{{3}}\"y\"", false, null),
                new Test("trim with extra spaces", "x\n{{-  3   -}}\ny", "\"x\"{{3}}\"y\"", false, null),
                new Test("comment trim left", "x \r\n\t{{- /* hi */}}", "\"x\"", false, null),
                new Test("comment trim right", "{{/* hi */ -}}\n\n\ty", "\"y\"", false, null),
                new Test("comment trim left and right", "x \r\n\t{{- /* */ -}}\n\n\ty", "\"x\"\"y\"", false, null),
                new Test("block definition", "{{block \"foo\" .}}hello{{end}}", "{{template \"foo\" .}}", false, null),

                new Test("newline in assignment", "{{ $x \n := \n 1 \n }}", "{{$x := 1}}", false, null),
                new Test("newline in empty action", "{{\n}}", "{{\n}}", true, null),
                new Test("newline in pipeline", "{{\n\"x\"\n|\nprintf\n}}", "{{\"x\" | printf}}", false, null),
                new Test("newline in comment", "{{/*\nhello\n*/}}", "", false, null),
                new Test("newline in comment", "{{-\n/*\nhello\n*/\n-}}", "", false, null),

                new Test("unclosed action", "hello{{range", "", true, null),
                new Test("unmatched end", "{{end}}", "", true, null),
                new Test("unmatched else", "{{else}}", "", true, null),
                new Test("unmatched else after if", "{{if .X}}hello{{end}}{{else}}", "", true, null),
                new Test("multiple else", "{{if .X}}1{{else}}2{{else}}3{{end}}", "", true, null),
                new Test("missing end", "hello{{range .x}}", "", true, null),
                new Test("missing end after else", "hello{{range .x}}{{else}}", "", true, null),
                new Test("undefined function", "hello{{undefined}}", "", true, null),
                new Test("undefined variable", "{{$x}}", "", true, null),
                new Test("variable undefined after end", "{{with $x := 4}}{{end}}{{$x}}", "", true, null),
                new Test("variable undefined in template", "{{template $v}}", "", true, null),
                new Test("declare with field", "{{with $x.Y := 4}}{{end}}", "", true, null),
                new Test("template with field ref", "{{template .X}}", "", true, null),
                new Test("template with var", "{{template $v}}", "", true, null),
                new Test("invalid punctuation", "{{printf 3, 4}}", "", true, null),
                new Test("multidecl outside range", "{{with $v, $u := 3}}{{end}}", "", true, null),
                new Test("too many decls in range", "{{range $u, $v, $w := 3}}{{end}}", "", true, null),
                new Test("dot applied to parentheses", "{{printf (printf .).}}", "", true, null),
                new Test("adjacent args", "{{printf 3`x`}}", "", true, null),
                new Test("adjacent args with .", "{{printf `x`.}}", "", true, null),
                new Test("extra end after if", "{{if .X}}a{{else if .Y}}b{{end}}{{end}}", "", true, null),

                new Test("bug0a", "{{$x := 0}}{{$x}}", "", true, null),
                new Test("bug0b", "{{$x += 1}}{{$x}}", "", true, null),
                new Test("bug0c", "{{$x ! 2}}{{$x}}", "", true, null),
                new Test("bug0d", "{{$x % 3}}{{$x}}", "", true, null),

                new Test("bug0e", "{{range $x := $y := 3}}{{end}}", "", true, null),

                new Test("bug1a", "{{$x:=.}}{{$x!2}}", "", true, null),
                new Test("bug1b", "{{$x:=.}}{{$x+2}}", "", true, null),
                new Test("bug1c", "{{$x:=.}}{{$x +2}}", "{{$x := .}}{{$x +2}}", false, null),

                new Test("dot after integer", "{{1.E}}", "", true, null),
                new Test("dot after float", "{{0.1.E}}", "", true, null),
                new Test("dot after boolean", "{{true.E}}", "", true, null),
                new Test("dot after char", "{{'a'.any}}", "", true, null),
                new Test("dot after string", "{{\"hello\".guys}}", "", true, null),
                new Test("dot after dot", "{{..E}}", "", true, null),
                new Test("dot after nil", "{{nil.E}}", "", true, null),

                new Test("wrong pipeline dot", "{{12|.}}", "", true, null),
                new Test("wrong pipeline number", "{{.|12|printf}}", "", true, null),
                new Test("wrong pipeline string", "{{.|printf|\"error\"}}", "", true, null),
                new Test("wrong pipeline char", "{{12|printf|'e'}}", "", true, null),
                new Test("wrong pipeline boolean", "{{.|true}}", "", true, null),
                new Test("wrong pipeline nil", "{{'c'|nil}}", "", true, null),
                new Test("empty pipeline", "{{printf \"%d\" ( ) }}", "", true, null),

                new Test("block definition", "{{block \"foo\"}}hello{{end}}", "", true, null),
        };


        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);


        for (Test test : tests) {
            try {
                Parser parser = new Parser(functions);
                parser.parse(test.name, test.input);

                Node node = parser.getRootNode(test.name);
                assertNotNull(node);
                assertTrue(node instanceof ListNode);
                assertEquals(test.result, node.toString(), String.format("%s: expected %s got %s", test.name, test.result, node));
            } catch (Exception e) {
                if (!test.error) {
                    System.out.printf("%s: got error: %s%n", test.name, e.getMessage());
                    e.printStackTrace();
                }
                assertTrue(test.error);
            }
        }
    }

}