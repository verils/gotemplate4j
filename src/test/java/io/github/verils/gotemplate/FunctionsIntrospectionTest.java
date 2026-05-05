package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FunctionsIntrospectionTest {

    @Test
    void typeofReturnsJavaTypeNameOrNil() throws IOException, TemplateException {
        assertTrue(render("{{typeof .Value}}", data("Value", "hello")).contains("String"));
        assertTrue(render("{{typeof .Value}}", data("Value", 42)).contains("Integer"));
        assertTrue(render("{{typeof .Value}}", data("Value", true)).contains("Boolean"));
        assertTrue(render("{{typeof .Value}}", data("Value", new int[]{1, 2, 3})).contains("[I"));
        assertEquals("nil", render("{{typeof .Value}}", data("Value", null)));
    }

    @ParameterizedTest
    @MethodSource("kindCases")
    void kindOfMapsJavaValuesToTemplateKinds(Object value, String expected) throws IOException, TemplateException {
        assertEquals(expected, render("{{kindOf .Value}}", data("Value", value)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> kindCases() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        return Stream.of(
                arguments("hello", "string"),
                arguments(42, "int"),
                arguments(true, "bool"),
                arguments(map, "map"),
                arguments(Arrays.asList(1, 2, 3), "slice"),
                arguments(new int[]{1, 2, 3}, "array"),
                arguments(new Recipient("Test", "gift", true), "struct"),
                arguments(null, "invalid")
        );
    }

    @ParameterizedTest
    @MethodSource("deepEqualCases")
    void deepEqualComparesValuesDeeply(Object left, Object right, String expected) throws IOException, TemplateException {
        assertEquals(expected, render("{{if deepEqual .A .B}}yes{{else}}no{{end}}", data("A", left, "B", right)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> deepEqualCases() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("key", "value");
        Map<String, String> map2 = new HashMap<>();
        map2.put("key", "value");
        List<String> sameList = Arrays.asList("a", "b");

        return Stream.of(
                arguments(new int[]{1, 2, 3}, new int[]{1, 2, 3}, "yes"),
                arguments(new int[]{1, 2, 3}, new int[]{1, 2, 4}, "no"),
                arguments(new int[]{1, 2}, new int[]{1, 2, 3}, "no"),
                arguments(map1, map2, "yes"),
                arguments(sameList, sameList, "yes"),
                arguments(null, null, "yes"),
                arguments(new int[]{1, 2}, null, "no"),
                arguments("string", 42, "no"),
                arguments(new Object[]{new int[]{1, 2}, new int[]{3, 4}},
                        new Object[]{new int[]{1, 2}, new int[]{3, 4}}, "yes")
        );
    }

    @Test
    void introspectionFunctionsValidateArgumentCounts() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("typeof"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("kindOf"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("deepEqual", "only one"));
    }
}
