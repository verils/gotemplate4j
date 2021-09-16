package com.github.verils.gotemplate.parse;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void test() {
        @Data
        class Test {
            private final String name;

            private final String input;
            private final String result;

            private final boolean error;
            private final String errorMessage;
        }

        var tests = new Test[]{
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
                new Test("range 2 var", "{{range $x, $y := .SI}}{{.}}{{end}}", "{{range $x, $y := .SI}}{{.}}{{end}}", false, null)
        };


        Map<String, Object> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);


        for (Test test : tests) {
            try {
                Parser parser = new Parser(test.input, functions);
                Node node = parser.getRoot();
                assertNotNull(node);
                assertTrue(node instanceof ListNode);
                assertEquals(test.getResult(), node.toString(), String.format("%s: expected %s got %s", test.name, test.result, node));
            } catch (Exception e) {
                e.printStackTrace();
                assertEquals(test.errorMessage, String.format("Got error for input '%s' caused by: %s", test.input, e.getMessage()));
            }
        }
    }
}