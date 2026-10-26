package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
                arguments("&<>\"'", "&amp;&lt;&gt;&#34;&#39;"),
                arguments("a&b", "a&amp;b"),
                arguments("a\0b", "a\uFFFDb"),
                arguments("中文☺", "中文☺")
        );
    }

    @Test
    void htmlReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{html .Text}}", data("Text", null)));
    }

    @ParameterizedTest
    @MethodSource("jsCases")
    void jsEscapesLikeGoJSEscapeString(String input, String expected) {
        assertEquals(expected, TemplateTestSupport.invoke("js", input));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> jsCases() {
        return Stream.of(
                arguments("plain text", "plain text"),
                arguments("It'd be nice.", "It\\'d be nice."),
                arguments("Go \"jump\" \\", "Go \\\"jump\\\" \\\\"),
                arguments("</script><script>alert(1)</script>",
                        "\\u003C/script\\u003E\\u003Cscript\\u003Ealert(1)\\u003C/script\\u003E"),
                arguments("<>&=", "\\u003C\\u003E\\u0026\\u003D"),
                arguments("Hello\nWorld\t\"quoted\"\r",
                        "Hello\\u000AWorld\\u0009\\\"quoted\\\"\\u000D"),
                arguments("中文☺", "中文☺"),
                arguments("a" + Character.toString(0xFFFE) + "b", "a\\uFFFEb"),
                arguments(Character.toString(0xD800), "\\uD800")
        );
    }

    @Test
    void jsEscapesScriptClosingSequenceInRenderedTemplate() throws IOException, TemplateException {
        assertEquals("\\u003C/script\\u003E",
                render("{{js .Text}}", data("Text", "</script>")));
    }

    @Test
    void jsReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{js .Text}}", data("Text", null)));
    }

    @ParameterizedTest
    @MethodSource("urlQueryCases")
    void urlqueryEncodesLikeGoQueryEscape(String input, String expected) {
        assertEquals(expected, TemplateTestSupport.invoke("urlquery", input));
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> urlQueryCases() {
        return Stream.of(
                arguments("", ""),
                arguments("abc", "abc"),
                arguments("one two", "one+two"),
                arguments("10%", "10%25"),
                arguments("a=b&c=d", "a%3Db%26c%3Dd"),
                arguments("a~*", "a~%2A"),
                arguments("中文", "%E4%B8%AD%E6%96%87"),
                arguments(" ?&=#+%!<>#\"{}|\\^[]`☺\t:/@$'()*,;",
                        "+%3F%26%3D%23%2B%25%21%3C%3E%23%22%7B%7D%7C%5C%5E%5B%5D%60"
                                + "%E2%98%BA%09%3A%2F%40%24%27%28%29%2A%2C%3B")
        );
    }

    @Test
    void urlqueryReturnsEmptyStringForNull() throws IOException, TemplateException {
        assertEquals("", render("{{urlquery .Text}}", data("Text", null)));
    }

    @Test
    void escapingFunctionsAcceptZeroOrMultipleArguments() throws IOException, TemplateException {
        assertEquals("", TemplateTestSupport.invoke("html"));
        assertEquals("", TemplateTestSupport.invoke("js"));
        assertEquals("", TemplateTestSupport.invoke("urlquery"));
        assertEquals("", render("{{html}}"));
        assertEquals("", render("{{js}}"));
        assertEquals("", render("{{urlquery}}"));

        assertEquals("1 2", TemplateTestSupport.invoke("html", 1, 2));
        assertEquals("1&lt;2", TemplateTestSupport.invoke("html", 1, "<", 2));
        assertEquals("ab\\u003C", TemplateTestSupport.invoke("js", "ab", "<"));
        assertEquals("a+b", TemplateTestSupport.invoke("urlquery", "a", " ", "b"));
    }

    @Test
    void escapingFunctionsTreatNullAsEmptyString() {
        assertEquals("", TemplateTestSupport.invoke("html", (Object) null));
        assertEquals("", TemplateTestSupport.invoke("js", (Object) null));
        assertEquals("", TemplateTestSupport.invoke("urlquery", (Object) null));
    }
}
