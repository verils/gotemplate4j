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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FunctionsEscapingTest {

    @ParameterizedTest
    @MethodSource("htmlCases")
    void htmlEscapesSpecialCharacters(String input, String expected) throws IOException, TemplateException {
        assertEquals(expected, render("{{html .Text}}", data("Text", input)));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> htmlCases() {
        return Stream.of(
                arguments("<script>alert('xss')</script>", "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"),
                arguments("&<>\"'", "&amp;&lt;&gt;&quot;&#39;"),
                arguments("a&b", "a&amp;b")
        );
    }

    @Test
    void htmlReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{html .Text}}", data("Text", null)));
    }

    @Test
    void jsEscapesJavascriptSensitiveCharacters() {
        String result = String.valueOf(TemplateTestSupport.invoke("js", "Hello\nWorld\t\"quoted\""));

        assertTrue(result.contains("\\n"));
        assertTrue(result.contains("\\t"));
        assertTrue(result.contains("\\\""));
    }

    @Test
    void jsEscapesBackslashesQuotesControlCharsAndUnicode() {
        assertEquals("back\\\\slash", TemplateTestSupport.invoke("js", "back\\slash"));
        assertEquals("\\u4e2d\\u6587", TemplateTestSupport.invoke("js", "中文"));

        String result = String.valueOf(TemplateTestSupport.invoke("js", "line1\r\nline2\ttab"));
        assertTrue(result.contains("\\r"));
        assertTrue(result.contains("\\n"));
        assertTrue(result.contains("\\t"));
    }

    @Test
    void jsReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{js .Text}}", data("Text", null)));
    }

    @Test
    void urlqueryEncodesUsingUtf8() throws IOException, TemplateException {
        assertEquals("hello+world%26test%3D1", render("{{urlquery .Text}}", data("Text", "hello world&test=1")));
        assertEquals("a%3Db%26c%3Dd", render("{{urlquery .Text}}", data("Text", "a=b&c=d")));
    }

    @Test
    void urlqueryReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{urlquery .Text}}", data("Text", null)));
    }

    @ParameterizedTest
    @MethodSource("wrongArityCases")
    void escapingFunctionsRequireOneArgument(String function, Object[] args) {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke(function, args));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> wrongArityCases() {
        return Stream.of(
                arguments("html", new Object[]{}),
                arguments("html", new Object[]{1, 2}),
                arguments("js", new Object[]{}),
                arguments("urlquery", new Object[]{})
        );
    }
}
