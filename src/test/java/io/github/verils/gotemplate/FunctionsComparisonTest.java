package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FunctionsComparisonTest {

    @ParameterizedTest
    @MethodSource("comparisonTemplates")
    void comparisonFunctionsRenderExpectedBranch(String template, String expected) throws IOException, TemplateException {
        assertEquals(expected, render(template));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> comparisonTemplates() {
        return Stream.of(
                arguments("{{if eq 1 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if eq \"hello\" \"hello\"}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if eq 1 2}}yes{{else}}no{{end}}", "no"),
                arguments("{{if eq 1 \"1\"}}yes{{else}}no{{end}}", "no"),
                arguments("{{if eq 1 1 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ne 1 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ne 1 1}}yes{{else}}no{{end}}", "no"),
                arguments("{{if ne 1 2 3}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if lt 1 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if lt 2 1}}yes{{else}}no{{end}}", "no"),
                arguments("{{if lt 2 2}}yes{{else}}no{{end}}", "no"),
                arguments("{{if lt \"abc\" \"abd\"}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if lt 1.5 2.5}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if le 2 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if le 1 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if gt 2 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if gt 2 2}}yes{{else}}no{{end}}", "no"),
                arguments("{{if gt \"b\" \"a\"}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ge 2 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ge 3 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ge 1 2}}yes{{else}}no{{end}}", "no")
        );
    }

    @ParameterizedTest
    @MethodSource("chainedComparisons")
    void chainedComparisonsCompareAdjacentValues(String template, String expected) throws IOException, TemplateException {
        assertEquals(expected, render(template));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> chainedComparisons() {
        return Stream.of(
                arguments("{{if lt 1 2 3 4}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if lt 1 3 2 4}}yes{{else}}no{{end}}", "no"),
                arguments("{{if le 1 2 2 3}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if gt 4 3 2 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ge 4 3 3 2}}yes{{else}}no{{end}}", "yes")
        );
    }

    @Test
    void comparisonUsesRuntimeValues() throws IOException, TemplateException {
        assertEquals("yes", render("{{if eq .A .B}}yes{{else}}no{{end}}", data("A", null, "B", null)));
        assertEquals("no", render("{{if eq .A .B}}yes{{else}}no{{end}}", data("A", "value", "B", null)));
        assertEquals("no", render("{{if ne .A .B}}yes{{else}}no{{end}}", data("A", null, "B", null)));
    }

    @ParameterizedTest
    @MethodSource("tooFewArguments")
    void comparisonFunctionsRequireAtLeastTwoArguments(String function) {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke(function, 1));
    }

    private static Stream<String> tooFewArguments() {
        return Stream.of("eq", "ne", "lt", "le", "gt", "ge");
    }

    @Test
    void orderedComparisonsRejectIncompatibleTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("lt", 1, "a"));
    }
}
