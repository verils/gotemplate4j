package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FunctionsLogicalTest {

    @ParameterizedTest
    @MethodSource("logicalTemplates")
    void logicalFunctionsUseGoTemplateTruthiness(String template, String expected) throws IOException, TemplateException {
        assertEquals(expected, render(template));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> logicalTemplates() {
        return Stream.of(
                arguments("{{if and true true}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if and true false}}yes{{else}}no{{end}}", "no"),
                arguments("{{and false true}}", "false"),
                arguments("{{and 1 2 3}}", "3"),
                arguments("{{and false false false}}", "false"),
                arguments("{{and}}", "<no value>"),
                arguments("{{if or false true}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if or false false}}yes{{else}}no{{end}}", "no"),
                arguments("{{or true false}}", "true"),
                arguments("{{or 0 0 5}}", "5"),
                arguments("{{or 0 \"\" false}}", "false"),
                arguments("{{or}}", "<no value>"),
                arguments("{{if not false}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if not 1}}yes{{else}}no{{end}}", "no")
        );
    }

    @Test
    void notTreatsNullEmptyStringAndZeroAsFalsy() throws IOException, TemplateException {
        String template = "{{if not .Value}}yes{{else}}no{{end}}";

        assertEquals("yes", render(template, data("Value", null)));
        assertEquals("yes", render(template, data("Value", "")));
        assertEquals("yes", render(template, data("Value", 0)));
    }

    @Test
    void defaultReturnsFallbackForFalsyValues() throws IOException, TemplateException {
        assertEquals("Anonymous", render("{{default .Value \"Anonymous\"}}", data("Value", null)));
        assertEquals("Anonymous", render("{{default .Value \"Anonymous\"}}", data("Value", "")));
        assertEquals("10", render("{{default .Value 10}}", data("Value", 0)));
        assertEquals("true", render("{{default .Value true}}", data("Value", false)));
    }

    @Test
    void defaultKeepsTruthyValues() throws IOException, TemplateException {
        assertEquals("Alice", render("{{default .Value \"Anonymous\"}}", data("Value", "Alice")));
        assertEquals("7", render("{{default .Value 10}}", data("Value", 7)));
    }

    @Test
    void defaultRequiresExactlyTwoArguments() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("default", "fallback"));
    }
}
