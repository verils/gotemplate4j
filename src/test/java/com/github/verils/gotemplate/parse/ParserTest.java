package com.github.verils.gotemplate.parse;

import lombok.Data;
import lombok.var;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testNumber() {
        @Data
        class Test {
            private final String input;

            private final String result;

            private final boolean error;
            private final String errorMessage;
        }

        var tests = new Test[]{
                new Test("", "", false, null),
                new Test("{{/*\n\n\n*/}}", "", false, null),
                new Test(" \t\n", " \t\n", false, null),
                new Test("some text", "some text", false, null),
                new Test("{{}}", "{{}}", true, "missing value for command"),
                new Test("{{.X}}", "{{.X}}", false, null)
        };


        for (Test test : tests) {
            Node node = null;
            Exception ex = null;

            try {
                node = new Parser().parse(test.input);
            } catch (Exception e) {
                ex = e;
            }

            boolean hasError = ex != null;
            if (!hasError) {
                assertNotNull(node);
                assertTrue(node instanceof ListNode);
                assertEquals(test.getResult(), node.toString());
            } else {
                assertEquals(test.errorMessage, ex.getMessage());
            }
        }
    }
}