package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests Go text/template root variable semantics for $.
 */
class RootVariableCompatibilityTest {

    @Test
    void rootActionCanPrintInitialData() throws IOException, TemplateException {
        assertEquals("root-value", render("{{$}}", "root-value"));
    }

    @Test
    void rootFieldCanAccessInitialData() throws IOException, TemplateException {
        assertEquals("root", render("{{$.Name}}", data("Name", "root")));
    }

    @Test
    void rootRemainsStableInsideWith() throws IOException, TemplateException {
        Object root = data(
                "Name", "root",
                "Sub", data("Name", "with")
        );

        assertEquals("root/with", render("{{with .Sub}}{{$.Name}}/{{.Name}}{{end}}", root));
    }

    @Test
    void rootRemainsStableInsideRange() throws IOException, TemplateException {
        Object root = data(
                "Name", "root",
                "Items", Arrays.asList("a", "b")
        );

        assertEquals("root:a;root:b;", render("{{range .Items}}{{$.Name}}:{{.}};{{end}}", root));
    }

    @Test
    void rootRemainsStableInsideNestedRangeAndWith() throws IOException, TemplateException {
        Object root = data(
                "Name", "root",
                "Groups", Arrays.asList(
                        data("Detail", data("Name", "one")),
                        data("Detail", data("Name", "two"))
                )
        );

        assertEquals(
                "root/one;root/two;",
                render("{{range .Groups}}{{with .Detail}}{{$.Name}}/{{.Name}};{{end}}{{end}}", root)
        );
    }

    @Test
    void templateInvocationBindsRootToPipelineValue() throws IOException, TemplateException {
        Object root = data(
                "Name", "caller-root",
                "Sub", data("Name", "child-root")
        );

        assertEquals(
                "child-root/child-root",
                render("{{define \"child\"}}{{$.Name}}/{{.Name}}{{end}}{{template \"child\" .Sub}}", root)
        );
    }

    @Test
    void nestedTemplateInvocationBindsRootToEachPipelineValue() throws IOException, TemplateException {
        Object root = data(
                "Name", "caller-root",
                "Sub", data(
                        "Name", "child-root",
                        "Inner", data("Name", "grandchild-root")
                )
        );

        assertEquals(
                "child=child-root grand=grandchild-root/grandchild-root",
                render(
                        "{{define \"grand\"}}grand={{$.Name}}/{{.Name}}{{end}}" +
                                "{{define \"child\"}}child={{$.Name}} {{template \"grand\" .Inner}}{{end}}" +
                                "{{template \"child\" .Sub}}",
                        root
                )
        );
    }
}
