package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals("b", render("{{index .Items 1}}", data("Items", Arrays.asList("a", "b", "c"))));
        assertEquals("h", render("{{index .Text 0}}", data("Text", "hello")));
    }

    @Test
    void indexReadsNestedSequencesAndMaps() throws IOException, TemplateException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d")));

        assertEquals("d", render("{{index .Map \"rows\" 1 1}}", data("Map", map)));
        assertEquals("b", render("{{1 | index .Items 0}}",
                data("Items", new String[][]{{"a", "b"}, {"c", "d"}})));
        assertEquals("Bob", render("{{index .Rows 0 \"Name\"}}",
                data("Rows", java.util.Collections.singletonList(TemplateTestSupport.data("Name", "Bob")))));
    }

    @ParameterizedTest
    @MethodSource("integralIndexCases")
    void indexAcceptsIntegralJavaTypes(Object index) throws IOException, TemplateException {
        assertEquals("b", render("{{index .Items .Index}}",
                data("Items", new String[]{"a", "b", "c"}, "Index", index)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> integralIndexCases() {
        return Stream.of(
                arguments((byte) 1),
                arguments((short) 1),
                arguments(1),
                arguments(1L),
                arguments(BigInteger.ONE),
                arguments(new AtomicInteger(1)),
                arguments(new AtomicLong(1))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidIndexCases")
    void indexRejectsNonIntegralAndOutOfRangeIndexes(Object index) throws IOException, TemplateException {
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> render("{{index .Items .Index}}",
                        data("Items", new String[]{"a", "b", "c"}, "Index", index)));

        assertTrue(exception.getMessage().contains("function 'index' failed"));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> invalidIndexCases() {
        return Stream.of(
                arguments((Object) null),
                arguments("1"),
                arguments(1.9),
                arguments('1'),
                arguments(-1),
                arguments(3)
        );
    }

    @Test
    void indexPreservesOptionalValuesBetweenLevels() throws IOException, TemplateException {
        Map<String, Object> row = new HashMap<>();
        row.put("Name", Optional.of("Bob"));
        List<Optional<Map<String, Object>>> rows = java.util.Collections.singletonList(Optional.of(row));

        assertEquals("Bob", render("{{index .Rows 0 \"Name\"}}", data("Rows", Optional.of(rows))));
    }

    @Test
    void indexRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("index", (Object) new String[]{"a"}));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("index", 42, 0));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("index", new String[]{"a"}, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("index", new String[]{"a"}, "0"));
    }

    @ParameterizedTest
    @MethodSource("sliceArities")
    void sliceSupportsZeroToThreeIndexes(String template, Object data, String expected)
            throws IOException, TemplateException {
        assertEquals(expected, render(template, data));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> sliceArities() {
        List<String> items = Arrays.asList("a", "b", "c");
        return Stream.of(
                arguments("{{slice .Text}}", data("Text", "hello"), "hello"),
                arguments("{{slice .Text 1}}", data("Text", "hello"), "ello"),
                arguments("{{slice .Text 1 4}}", data("Text", "hello"), "ell"),
                arguments("{{slice .Items}}", data("Items", items), "[a, b, c]"),
                arguments("{{slice .Items 1}}", data("Items", items), "[b, c]"),
                arguments("{{slice .Items 1 2}}", data("Items", items), "[b]"),
                arguments("{{slice .Items 1 2 3}}", data("Items", items), "[b]")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSliceCases")
    void sliceRejectsInvalidRanges(Object value, Object[] indexes) {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke(
                "slice", concat(value, indexes)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> invalidSliceCases() {
        return Stream.of(
                arguments("hello", new Object[]{-1}),
                arguments("hello", new Object[]{3, 1}),
                arguments("hello", new Object[]{1, 6}),
                arguments("hello", new Object[]{1, 2, 2}),
                arguments(Arrays.asList("a", "b", "c"), new Object[]{-1, 2}),
                arguments(Arrays.asList("a", "b", "c"), new Object[]{2, 1}),
                arguments(Arrays.asList("a", "b", "c"), new Object[]{1, 3, 2}),
                arguments(Arrays.asList("a", "b", "c"), new Object[]{0, 1, 2, 3}),
                arguments(null, new Object[]{}),
                arguments("hello", new Object[]{"1"}),
                arguments("hello", new Object[]{1.0})
        );
    }

    @Test
    void sliceReturnsArrayForArrayInput() {
        Object full = TemplateTestSupport.invoke("slice", (Object) new String[]{"a", "b", "c", "d"});
        Object result = TemplateTestSupport.invoke("slice", new String[]{"a", "b", "c", "d"}, 1, 3);
        Object bounded = TemplateTestSupport.invoke("slice", new String[]{"a", "b", "c", "d"}, 1, 3, 4);

        assertArrayEquals(new String[]{"a", "b", "c", "d"}, (String[]) full);
        assertArrayEquals(new String[]{"b", "c"}, (String[]) result);
        assertArrayEquals(new String[]{"b", "c"}, (String[]) bounded);
    }

    @Test
    void sliceReturnsListForListInput() throws IOException, TemplateException {
        assertEquals("[b, c]", render("{{slice .Items 1 3}}", data("Items", Arrays.asList("a", "b", "c", "d"))));
    }

    @Test
    void sliceRejectsInvalidArgumentCountsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("slice"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("slice", 42, 0, 1));
    }

    @Test
    void sliceWrapsExecutionFailuresWithDiagnostics() {
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> render("{{slice .Text 2 1}}", data("Text", "abc")));

        assertTrue(exception.getMessage().contains("function 'slice' failed"));
        assertTrue(exception.getMessage().contains("invalid slice index"));
    }

    private static Object[] concat(Object value, Object[] indexes) {
        Object[] args = new Object[indexes.length + 1];
        args[0] = value;
        System.arraycopy(indexes, 0, args, 1, indexes.length);
        return args;
    }
}
