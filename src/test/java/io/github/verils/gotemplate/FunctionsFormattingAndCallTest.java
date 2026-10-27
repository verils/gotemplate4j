package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionsFormattingAndCallTest {

    @Test
    void printUsesGoSpacingRules() throws IOException, TemplateException {
        assertEquals("helloworld", render("{{print \"hello\" \"world\"}}"));
        assertEquals("1 2", render("{{print 1 2}}"));
        assertEquals("x1y2", render("{{print \"x\" 1 \"y\" 2}}"));
        assertEquals("<nil>x", render("{{print nil \"x\"}}"));
        assertEquals("x<nil>", render("{{print \"x\" nil}}"));
        assertEquals("<nil> <nil>", render("{{print nil nil}}"));
        assertEquals("", render("{{print}}"));
    }

    @Test
    void printfFormatsCoreVerbs() throws IOException, TemplateException {
        assertEquals("hello 42", render("{{printf .Format .Name .Age}}",
                data("Format", "%s %d", "Name", "hello", "Age", 42)));
        assertEquals("hello", render("{{printf \"hello\"}}"));
        assertEquals("Alice|42|true|<nil>",
                render("{{printf \"%v|%v|%v|%v\" .Name .Age .Active .Missing}}",
                        data("Name", "Alice", "Age", 42, "Active", true)));
        assertEquals("java.lang.String|java.lang.Integer|<nil>",
                render("{{printf \"%T|%T|%T\" .Name .Age .Missing}}",
                        data("Name", "Alice", "Age", 42)));
    }

    @Test
    void printfFormatsNumbersBooleansAndQuotedStrings() throws IOException, TemplateException {
        assertEquals("true|255|FF|3.14|3.140",
                render("{{printf \"%t|%d|%X|%.2f|%.3f\" .Flag .Int .Int .Float .Float}}",
                        data("Flag", true, "Int", 255, "Float", 3.14)));
        assertEquals("\"a\\n\\\"b\\\\中\\x01\"",
                TemplateTestSupport.invoke("printf", "%q", "a\n\"b\\中\u0001"));
        assertEquals("\"\\u0085\"", TemplateTestSupport.invoke("printf", "%q", "\u0085"));
        assertEquals("\"x\"", TemplateTestSupport.invoke("printf", "%q", 'x'));
        assertEquals("\"\\a\\b\\f\\r\\t\\v\"",
                TemplateTestSupport.invoke("printf", "%q", "\u0007\b\f\r\t\u000B"));
        assertEquals("ff|0xff|ff", TemplateTestSupport.invoke("printf", "%x|%#x|%x",
                new BigInteger("255"), new BigInteger("255"), new AtomicInteger(255)));
        assertEquals("3.50", TemplateTestSupport.invoke("printf", "%.2f", new BigDecimal("3.5")));
    }

    @Test
    void printfSupportsWidthPrecisionFlagsAndLiteralPercent() throws IOException, TemplateException {
        assertEquals("[00042] [hello] [+3.14] [he] [3.14159] 100%",
                render("{{printf \"[%05d] [%-5s] [%+.2f] [%.2s] [%v] 100%%\" .Number .Text .Price .Text .Price}}",
                        data("Number", 42, "Text", "hello", "Price", 3.14159)));
        assertEquals("[  x] [  3.14]", TemplateTestSupport.invoke("printf", "[%3v] [%6.2f]", "x", 3.14159));
    }

    @Test
    void printfUsesPracticalJavaAggregateRepresentations() throws IOException, TemplateException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        assertEquals("[a, b]|{a=1, b=2}",
                render("{{printf \"%v|%v\" .Array .Map}}",
                        data("Array", new String[]{"a", "b"}, "Map", map)));
        assertEquals("[1, 2]", TemplateTestSupport.invoke("printf", "%v", new int[]{1, 2}));
    }

    @Test
    void printRendersAllPrimitiveArrayTypes() {
        assertEquals("[true, false]", TemplateTestSupport.invoke("print", (Object) new boolean[]{true, false}));
        assertEquals("[1, 2]", TemplateTestSupport.invoke("print", (Object) new byte[]{1, 2}));
        assertEquals("[1, 2]", TemplateTestSupport.invoke("print", (Object) new short[]{1, 2}));
        assertEquals("[1, 2]", TemplateTestSupport.invoke("print", (Object) new long[]{1, 2}));
        assertEquals("[1.0, 2.0]", TemplateTestSupport.invoke("print", (Object) new float[]{1, 2}));
        assertEquals("[1.0, 2.0]", TemplateTestSupport.invoke("print", (Object) new double[]{1, 2}));
        assertEquals("[a, b]", TemplateTestSupport.invoke("print", (Object) new char[]{'a', 'b'}));
    }

    @Test
    void printlnAppendsNewline() throws IOException, TemplateException {
        assertEquals("hello\n", render("{{println \"hello\"}}"));
        assertEquals("1 2 3\n", render("{{println 1 2 3}}"));
        assertEquals("\n", render("{{println}}"));
        assertEquals("<nil> x\n", render("{{println nil \"x\"}}"));
    }

    @Test
    void printfRejectsInvalidFormatsArgumentsAndTypes() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", 42));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%z", "x"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%d"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%s", "x", "extra"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%d", "42"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%f", 42));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%t", "true"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%s", 42));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%.f", 1.0));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%*d", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%.2*f", 1.0));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%[1]s", "x"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%0s", "x"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%#d", 1));
        assertThrows(IllegalArgumentException.class,
                () -> TemplateTestSupport.invoke("printf", "%999999999999999999999d", 1));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("printf", "%++d", 1));
    }

    @Test
    void printfErrorsAreWrappedAsTemplateExecutionExceptions() throws IOException, TemplateException {
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> render("{{printf \"%z\" .Value}}", data("Value", "x")));

        assertTrue(exception.getMessage().contains("function 'printf' failed"));
        assertTrue(exception.getMessage().contains("unsupported verb 'z'"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void callInvokesFunctionValues() throws IOException, TemplateException {
        assertEquals("result", render("{{call .Func}}", data("Func", (Function) args -> "result")));
        assertEquals("arg1-arg2", render("{{call .Func \"arg1\" \"arg2\"}}",
                data("Func", (Function) args -> args[0] + "-" + args[1])));
    }

    @Test
    void callRequiresAFunctionValue() {
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("call"));
        assertThrows(IllegalArgumentException.class, () -> TemplateTestSupport.invoke("call", "not a function"));
    }
}
