package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                arguments("{{if eq 1 1 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if eq 1 1 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if eq 1 2 1}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if eq 1 2 3}}yes{{else}}no{{end}}", "no"),
                arguments("{{if eq 1 1 \"1\"}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ne 1 2}}yes{{else}}no{{end}}", "yes"),
                arguments("{{if ne 1 1}}yes{{else}}no{{end}}", "no"),
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
    void orderedComparisonsRequireExactlyTwoArguments() {
        for (String function : Arrays.asList("ne", "lt", "le", "gt", "ge")) {
            assertThrows(IllegalArgumentException.class,
                    () -> TemplateTestSupport.invoke(function, 1, 2, 3));
        }
    }

    @ParameterizedTest
    @MethodSource("incompatibleComparisonTemplates")
    void incompatibleTypesRaiseTemplateExecutionException(String template) {
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> render(template));
        assertTrue(exception.getMessage().contains("incompatible types for comparison"));
    }

    private static Stream<String> incompatibleComparisonTemplates() {
        return Stream.of(
                "{{eq 1 \"1\"}}",
                "{{eq 2 2.0}}",
                "{{eq true 1}}",
                "{{lt \"a\" 1}}"
        );
    }

    @Test
    void orderedComparisonsRejectUnorderedTypes() {
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> render("{{lt true false}}"));
        assertTrue(exception.getMessage().contains("invalid type for ordered comparison"));

        exception = assertThrows(TemplateExecutionException.class,
                () -> render("{{le nil 0}}"));
        assertTrue(exception.getMessage().contains("invalid type for ordered comparison"));
    }

    @Test
    void integerComparisonsPreservePrecisionAcrossWrapperTypes() {
        assertFalse((Boolean) TemplateTestSupport.invoke("eq",
                9007199254740992L, 9007199254740993L));
        assertTrue((Boolean) TemplateTestSupport.invoke("eq",
                Integer.valueOf(7), Long.valueOf(7)));
        assertTrue((Boolean) TemplateTestSupport.invoke("eq",
                Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE)));
        assertTrue((Boolean) TemplateTestSupport.invoke("eq",
                new AtomicLong(42), Integer.valueOf(42)));
        assertTrue((Boolean) TemplateTestSupport.invoke("gt",
                BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), Long.MAX_VALUE));
    }

    @Test
    void decimalComparisonsPreserveTypeGroupAndPrecision() {
        assertTrue((Boolean) TemplateTestSupport.invoke("eq",
                Float.valueOf(0.5f), Double.valueOf(0.5d)));
        assertTrue((Boolean) TemplateTestSupport.invoke("eq",
                new BigDecimal("0.10000000000000000000"), new BigDecimal("0.1")));
        assertTrue((Boolean) TemplateTestSupport.invoke("lt",
                new BigDecimal("0.10000000000000000001"), new BigDecimal("0.10000000000000000002")));

        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("eq", 1, 1.0d));
    }

    @Test
    void decimalComparisonPreservesGoNaNAndInfinityBehavior() {
        assertFalse((Boolean) TemplateTestSupport.invoke("eq", Double.NaN, Double.NaN));
        assertFalse((Boolean) TemplateTestSupport.invoke("lt", Double.NaN, 1.0d));
        assertFalse((Boolean) TemplateTestSupport.invoke("le", Double.NaN, 1.0d));
        assertTrue((Boolean) TemplateTestSupport.invoke("gt", Double.NaN, 1.0d));
        assertTrue((Boolean) TemplateTestSupport.invoke("ge", Double.NaN, 1.0d));

        assertTrue((Boolean) TemplateTestSupport.invoke("lt",
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse((Boolean) TemplateTestSupport.invoke("eq",
                Double.POSITIVE_INFINITY, new BigDecimal("1E10000")));
    }

    @Test
    void aggregateValuesRequireDeepEqual() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("eq",
                        Arrays.asList("x"), Arrays.asList("x")));
        assertTrue(exception.getMessage().contains("non-comparable value"));

        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("eq",
                        Collections.singletonMap("x", "y"), Collections.singletonMap("x", "y")));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("eq",
                        new String[]{"x"}, new String[]{"x"}));
    }

    @Test
    void sameTypeObjectsUseEqualsButCrossTypeObjectsAreIncompatible() {
        Status active = Status.ACTIVE;
        assertTrue((Boolean) TemplateTestSupport.invoke("eq", active, Status.ACTIVE));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("eq", active, "ACTIVE"));
    }

    private enum Status {
        ACTIVE
    }
}
