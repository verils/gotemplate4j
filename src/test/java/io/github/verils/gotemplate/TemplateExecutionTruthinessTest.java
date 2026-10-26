package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TemplateExecutionTruthinessTest {

    @Test
    void truthinessCoversJavaNumberKinds() throws IOException, TemplateException {
        assertEquals("no", renderIfValue(0L));
        assertEquals("yes", renderIfValue(2L));
        assertEquals("no", renderIfValue(0.0f));
        assertEquals("yes", renderIfValue(1.5f));
        assertEquals("no", renderIfValue(0.0d));
        assertEquals("yes", renderIfValue(2.5d));
        assertEquals("no", renderIfValue((short) 0));
        assertEquals("yes", renderIfValue((short) 1));
        assertEquals("no", renderIfValue((byte) 0));
        assertEquals("yes", renderIfValue((byte) 1));
        assertEquals("no", renderIfValue(BigDecimal.ZERO));
        assertEquals("yes", renderIfValue(new BigDecimal("0.25")));
    }

    @Test
    void truthinessCoversArraysCollectionsMapsAndObjects() throws IOException, TemplateException {
        assertEquals("no", renderIfValue(new String[0]));
        assertEquals("yes", renderIfValue(new String[]{"x"}));
        assertEquals("no", renderIfValue(Collections.emptyList()));
        assertEquals("yes", renderIfValue(Collections.singletonList("x")));
        assertEquals("no", renderIfValue(Collections.emptyMap()));
        assertEquals("yes", renderIfValue(Collections.singletonMap("x", "y")));
        assertEquals("yes", renderIfValue(new Object()));
    }

    @Test
    void pipelineFinalValueForShortCircuitFunctions() throws IOException, TemplateException {
        assertEquals("false", TemplateTestSupport.render("{{.Value | and}}", TemplateTestSupport.data("Value", false)));
        assertEquals("true", TemplateTestSupport.render("{{.Value | and}}", TemplateTestSupport.data("Value", true)));
        assertEquals("true", TemplateTestSupport.render("{{.Value | or}}", TemplateTestSupport.data("Value", true)));
        assertEquals("false", TemplateTestSupport.render("{{.Value | or}}", TemplateTestSupport.data("Value", false)));
    }

    @ParameterizedTest
    @MethodSource("sharedTruthinessCases")
    void logicalConstructsShareTruthiness(Object value, boolean expected)
            throws IOException, TemplateException {
        assertEquals(expected ? "yes" : "no", renderIfValue(value));
        assertEquals(expected ? "yes" : "no",
                render("{{with .Value}}yes{{else}}no{{end}}", value));
        assertEquals(expected ? "yes" : "no",
                render("{{if and .Value true}}yes{{else}}no{{end}}", value));
        assertEquals(expected ? "yes" : "no",
                render("{{if or .Value false}}yes{{else}}no{{end}}", value));
        assertEquals(expected ? "false" : "true", render("{{not .Value}}", value));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> sharedTruthinessCases() {
        return Stream.of(
                arguments(null, false),
                arguments(false, false),
                arguments(true, true),
                arguments("", false),
                arguments(" ", true),
                arguments("0", true),
                arguments(0, false),
                arguments(0L, false),
                arguments(0.0d, false),
                arguments(-0.0d, false),
                arguments(Double.NaN, true),
                arguments(1, true),
                arguments(BigInteger.ZERO, false),
                arguments(BigInteger.ONE, true),
                arguments(BigDecimal.ZERO, false),
                arguments(new BigDecimal("1E-400"), true),
                arguments(new String[0], false),
                arguments(new String[]{"x"}, true),
                arguments(Collections.emptyList(), false),
                arguments(Collections.singletonList("x"), true),
                arguments(Collections.emptyMap(), false),
                arguments(Collections.singletonMap("key", "value"), true),
                arguments(Optional.empty(), false),
                arguments(Optional.of(""), false),
                arguments(Optional.of("value"), true),
                arguments(new Object(), true)
        );
    }

    private String render(String template, Object value) throws IOException, TemplateException {
        return TemplateTestSupport.render(template, TemplateTestSupport.data("Value", value));
    }

    private String renderIfValue(Object value) throws IOException, TemplateException {
        return render("{{if .Value}}yes{{else}}no{{end}}", value);
    }
}
