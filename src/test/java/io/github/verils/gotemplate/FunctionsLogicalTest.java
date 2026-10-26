package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @ParameterizedTest
    @MethodSource("defaultCases")
    void defaultUsesGoTruthinessForDirectAndPipelineCalls(Object value, String expected)
            throws IOException, TemplateException {
        Map<String, Object> data = data("Value", value);

        assertEquals(expected, render("{{default .Value \"fallback\"}}", data));
        assertEquals(expected, render("{{.Value | default \"fallback\"}}", data));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> defaultCases() {
        return Stream.of(
                arguments(null, "fallback"),
                arguments("", "fallback"),
                arguments(0, "fallback"),
                arguments(0L, "fallback"),
                arguments(0.0d, "fallback"),
                arguments(false, "fallback"),
                arguments(new String[0], "fallback"),
                arguments(Collections.emptyList(), "fallback"),
                arguments(Collections.emptyMap(), "fallback"),
                arguments(Optional.empty(), "fallback"),
                arguments("value", "value"),
                arguments(" ", " "),
                arguments(1, "1"),
                arguments(true, "true"),
                arguments(Collections.singletonList("x"), "[x]"),
                arguments(Collections.singletonMap("key", "value"), "{key=value}"),
                arguments(Optional.of("value"), "value")
        );
    }

    @Test
    void defaultReceivesNullAndMissingPipelineValues() throws IOException, TemplateException {
        assertEquals("fallback", render("{{nil | default \"fallback\"}}"));
        assertEquals("fallback", render("{{.Missing | default \"fallback\"}}", data()));
    }

    @Test
    void customDefaultKeepsStandardPipelineArgumentOrder() throws IOException, TemplateException {
        Map<String, Function> functions = new HashMap<>();
        functions.put("default", args -> args.length + "|" + args[0] + "|" + args[1]);
        Template template = new Template("test", functions);
        template.parse("{{.Value | default \"fallback\"}}");

        java.io.StringWriter writer = new java.io.StringWriter();
        template.execute(writer, data("Value", "pipeline-value"));

        assertEquals("2|fallback|pipeline-value", writer.toString());
    }

    @Test
    void defaultRequiresExactlyTwoArguments() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("default"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("default", "fallback"));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("default", "value", "fallback", "extra"));
    }

    @Test
    void notRequiresExactlyOneArgument() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("not"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("not", true, false));
    }
}
