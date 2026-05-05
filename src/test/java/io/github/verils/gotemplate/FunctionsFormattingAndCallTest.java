package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.github.verils.gotemplate.TemplateTestSupport.data;
import static io.github.verils.gotemplate.TemplateTestSupport.render;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionsFormattingAndCallTest {

    @Test
    void printConcatenatesArgumentsWithSpaces() throws IOException, TemplateException {
        assertEquals("hello world", render("{{print \"hello\" \"world\"}}"));
        assertEquals("", render("{{print}}"));
    }

    @Test
    void printfFormatsArguments() throws IOException, TemplateException {
        assertEquals("hello 42", render("{{printf .Format .Name .Age}}",
                data("Format", "%s %d", "Name", "hello", "Age", 42)));
        assertEquals("hello", render("{{printf \"hello\"}}"));
    }

    @Test
    void printlnAppendsNewline() throws IOException, TemplateException {
        assertEquals("hello\n", render("{{println \"hello\"}}"));
        assertEquals("1 2 3\n", render("{{println 1 2 3}}"));
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
