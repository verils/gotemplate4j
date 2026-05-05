package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FunctionsCollectionTest {

    @ParameterizedTest
    @MethodSource("lengthCases")
    void lenReturnsSizeForSupportedTypes(Object value, Object expected) {
        assertEquals(expected, TemplateTestSupport.invoke("len", value));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> lengthCases() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        return Stream.of(
                arguments("hello", 5),
                arguments("", 0),
                arguments(new String[]{"a", "b", "c"}, 3),
                arguments(new String[]{}, 0),
                arguments(Arrays.asList("x", "y"), 2),
                arguments(new ArrayList<>(), 0),
                arguments(map, 3),
                arguments(null, 0)
        );
    }

    @Test
    void lenRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len", 1, 2));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("len", 42));
    }

    @Test
    void indexReadsMapsArraysAndStrings() throws IOException, TemplateException {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        assertEquals("value", render("{{index .Map \"key\"}}", data("Map", map)));
        assertEquals("b", render("{{index .Items 1}}", data("Items", new String[]{"a", "b", "c"})));
        assertEquals("h", render("{{index .Text 0}}", data("Text", "hello")));
    }

    @ParameterizedTest
    @MethodSource("indexConversionCases")
    void indexConvertsNumericLikeIndexes(Object index, String expected) throws IOException, TemplateException {
        assertEquals(expected, render("{{index .Items .Index}}", data("Items", new String[]{"a", "b", "c"}, "Index", index)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> indexConversionCases() {
        return Stream.of(
                arguments("1", "b"),
                arguments("abc", "a"),
                arguments(null, "a"),
                arguments(1.9, "b")
        );
    }

    @ParameterizedTest
    @MethodSource("indexMissCases")
    void indexReturnsEmptyOutputForMissingValues(String template, Object data) throws IOException, TemplateException {
        assertEquals("", render(template, data));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> indexMissCases() {
        return Stream.of(
                arguments("{{index .Collection \"key\"}}", data("Collection", null)),
                arguments("{{index .Items 10}}", data("Items", new String[]{"a", "b", "c"})),
                arguments("{{index .Text 100}}", data("Text", "hi"))
        );
    }

    @Test
    void templateIndexWithNegativeArrayIndexKeepsCurrentExecutorBehavior() throws IOException, TemplateException {
        assertEquals("b", render("{{index .Items -1}}", data("Items", new String[]{"a", "b", "c"})));
    }

    @Test
    void indexRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("index", (Object) new String[]{"a"}));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("index", 42, 0));
    }

    @ParameterizedTest
    @MethodSource("stringSliceCases")
    void sliceHandlesStringBounds(String template, String expected) throws IOException, TemplateException {
        assertEquals(expected, render(template, data("Text", "hello")));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> stringSliceCases() {
        return Stream.of(
                arguments("{{slice .Text 1 4}}", "ell"),
                arguments("{{slice \"hello\" 1 4}}", "ell"),
                arguments("{{slice .Text 3 1}}", ""),
                arguments("{{slice .Text 0 100}}", "hello")
        );
    }

    @Test
    void directSliceClampsNegativeStringStart() {
        assertEquals("hel", TemplateTestSupport.invoke("slice", "hello", -5, 3));
    }

    @Test
    void sliceReturnsArrayForArrayInput() {
        Object result = TemplateTestSupport.invoke("slice", (Object) new String[]{"a", "b", "c", "d"}, 1, 3);
        assertArrayEquals(new String[]{"b", "c"}, (String[]) result);
    }

    @Test
    void sliceReturnsEmptyArrayWhenArrayStartIsNotBeforeEnd() {
        Object result = TemplateTestSupport.invoke("slice", (Object) new String[]{"a", "b", "c"}, 3, 2);
        assertEquals(0, java.lang.reflect.Array.getLength(result));
    }

    @Test
    void sliceRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("slice", "abc", 0));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("slice", 42, 0, 1));
    }
}
