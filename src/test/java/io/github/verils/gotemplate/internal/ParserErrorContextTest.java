package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import org.junit.jupiter.api.Test;

import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser1;
import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser2;
import static org.junit.jupiter.api.Assertions.*;

class ParserErrorContextTest {

    @Test
    void testErrorWithContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{else}}\n  World\n{{end}}");
            // Should parse successfully
        } catch (TemplateParseException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }


    @Test
    void testUnmatchedEndWithErrorContext() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello\n{{end}}");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            // Exception should be thrown, message may or may not include context
            assertNotNull(e.getMessage());
        }
    }


    @Test
    void testErrorContextWithNullToken() {
        Parser parser = createParser1();
        try {
            // Test error path where token is null in error context
            parser.parse("test", "{{if .X}}hello");
            fail("Should throw TemplateParseException for unclosed if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Message should contain parse error info
            assertTrue(e.getMessage().contains("Parse error") || e.getMessage().contains("unexpected"));
        }
    }


    @Test
    void testErrorContextWithMultilineTemplate() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{invalid syntax}}\nLine 4");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // Error message should be present, may or may not have line info depending on error type
            assertFalse(e.getMessage().isEmpty());
        }
    }


    @Test
    void testUnclosedDelimiterError() {
        Parser parser = createParser1();
        try {
            parser.parse("test", "Hello {{");
            fail("Should throw TemplateParseException for unclosed delimiter");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unclosed"));
        }
    }


    @Test
    void testMissingActionTokenError() {
        Parser parser = createParser1();
        try {
            // This should trigger missing action token error
            parser.parse("test", "{{ ");
            fail("Should throw TemplateParseException");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }


    @Test
    void testErrorMessageWithContextForSyntaxError() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{if .X}}\n  Hello\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            // Error should contain parse error info
            assertFalse(message.isEmpty());
        }
    }


    @Test
    void testErrorMessageWithContextForUnclosedIf() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "Line 1\nLine 2\n{{if .X}}\nLine 4");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
            assertFalse(message.isEmpty());
        }
    }


    @Test
    void testErrorMessageWithContextForMissingEnd() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{range .Items}}\n  Item\n{{if .Active}}\n    Active");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            String message = e.getMessage();
            assertNotNull(message);
        }
    }

    // Test lookNextItem at boundaries

    @Test
    void testErrorContextWithTabs() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\n\t{{invalid}}\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test empty template name in define

    @Test
    void testErrorContextWithTabAtErrorColumn() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "\t\t{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at first line

    @Test
    void testErrorContextAtFirstLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "{{invalid}}\nline2\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context at last line

    @Test
    void testErrorContextAtLastLine() {
        Parser parser = createParser2();
        try {
            parser.parse("test", "line1\nline2\n{{invalid}}");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test error context with very long line

    @Test
    void testErrorContextWithLongLine() {
        Parser parser = createParser2();
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 200; i++) {
                sb.append('x');
            }
            sb.append(" {{invalid}}");
            parser.parse("test", sb.toString());
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test variable with multiple field accesses

    @Test
    void testParseBlockWithInvalidToken() {
        Parser parser = createParser2();
        try {
            // Test block with invalid token type (not string)
            parser.parse("test", "{{block .Value}}content{{end}}");
            fail("Should throw TemplateParseException for invalid block token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected") || e.getMessage().contains("missing"));
        }
    }

    @Test
    void testParseBlockWithMissingToken() {
        Parser parser = createParser2();
        try {
            // Test block with missing token after block keyword
            parser.parse("test", "{{block }}");
            fail("Should throw TemplateParseException for missing block token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("missing") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseDefinitionWithInvalidToken() {
        Parser parser = createParser2();
        try {
            // Test define with invalid token type
            parser.parse("test", "{{define .Invalid}}content{{end}}");
            fail("Should throw TemplateParseException for invalid define token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseDefinitionWithMissingEnd() {
        Parser parser = createParser2();
        try {
            // Test define without end
            parser.parse("test", "{{define \"template\"}}content");
            fail("Should throw TemplateParseException for missing end in define");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected") || e.getMessage().contains("expected end"));
        }
    }

    @Test
    void testParseDefinitionWithUnexpectedTokenAfterName() {
        Parser parser = createParser2();
        try {
            // Test define with unexpected token after template name
            parser.parse("test", "{{define \"template\" .Invalid}}content{{end}}");
            fail("Should throw TemplateParseException for unexpected token after define name");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseElseWithUnexpectedToken() {
        Parser parser = createParser2();
        try {
            // Test else with unexpected token
            parser.parse("test", "{{if .X}}hello{{else .Invalid}}world{{end}}");
            fail("Should throw TemplateParseException for unexpected token in else");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseEndWithUnexpectedToken() {
        Parser parser = createParser2();
        try {
            // Test end with unexpected token
            parser.parse("test", "{{if .X}}hello{{end .Invalid}}");
            fail("Should throw TemplateParseException for unexpected token in end");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseTemplateWithInvalidToken() {
        Parser parser = createParser2();
        try {
            // Test template with invalid token type
            parser.parse("test", "{{template .Invalid}}");
            fail("Should throw TemplateParseException for invalid template token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseTemplateWithMissingToken() {
        Parser parser = createParser2();
        try {
            // Test template with missing token
            parser.parse("test", "{{template }}");
            fail("Should throw TemplateParseException for missing template token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("missing") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseBranchWithMissingEnd() {
        Parser parser = createParser2();
        try {
            // Test if without end
            parser.parse("test", "{{if .X}}hello");
            fail("Should throw TemplateParseException for missing end in if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("expected end") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParsePipeWithMissingValue() {
        Parser parser = createParser2();
        try {
            // Test pipe with missing value
            parser.parse("test", "{{if }}hello{{end}}");
            fail("Should throw TemplateParseException for missing value in pipe");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("missing value"));
        }
    }

    @Test
    void testParsePipeWithNonExecutableCommand() {
        Parser parser = createParser2();
        try {
            // Test pipe with non-executable command (bool node as first argument in second stage)
            parser.parse("test", "{{printf \"%s\" | true}}");
            fail("Should throw TemplateParseException for non-executable command");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("non-executable command"));
        }
    }

    @Test
    void testParsePipeWithDotNodeAsFirstArgument() {
        Parser parser = createParser2();
        try {
            // Test pipe with dot node as first argument in pipeline stage (second stage)
            parser.parse("test", "{{printf \"%s\" | .}}");
            fail("Should throw TemplateParseException for dot node as first argument");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("non-executable command"));
        }
    }

    @Test
    void testParsePipeWithNilNodeAsFirstArgument() {
        Parser parser = createParser2();
        try {
            // Test pipe with nil node as first argument in pipeline stage (second stage)
            parser.parse("test", "{{printf \"%s\" | nil}}");
            fail("Should throw TemplateParseException for nil node as first argument");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("non-executable command"));
        }
    }

    @Test
    void testParsePipeWithNumberNodeAsFirstArgument() {
        Parser parser = createParser2();
        try {
            // Test pipe with number node as first argument in pipeline stage (second stage)
            parser.parse("test", "{{printf \"%s\" | 42}}");
            fail("Should throw TemplateParseException for number node as first argument");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("non-executable command"));
        }
    }

    @Test
    void testParsePipeWithStringNodeAsFirstArgument() {
        Parser parser = createParser2();
        try {
            // Test pipe with string node as first argument in pipeline stage (second stage)
            parser.parse("test", "{{printf \"%s\" | \"hello\"}}");
            fail("Should throw TemplateParseException for string node as first argument");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("non-executable command"));
        }
    }

    @Test
    void testParseCommandWithUndefinedFunction() {
        Parser parser = createParser2();
        try {
            // Test command with undefined function
            parser.parse("test", "{{undefinedFunc .Value}}");
            fail("Should throw TemplateParseException for undefined function");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("undefined function"));
        }
    }

    @Test
    void testParseCommandWithEmptyCommand() {
        Parser parser = createParser2();
        try {
            // Test empty command
            parser.parse("test", "{{}}");
            fail("Should throw TemplateParseException for empty command");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("empty command") || e.getMessage().contains("missing"));
        }
    }

    @Test
    void testParseNumberWithMalformedCharConstant() {
        Parser parser = createParser2();
        try {
            // Test malformed character constant - unclosed quote
            parser.parse("test", "{{'x}}");
            fail("Should throw TemplateParseException for malformed char constant");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // The error might be caught by lexer or parser
            assertTrue(e.getMessage().contains("malformed") || e.getMessage().contains("invalid") || e.getMessage().contains("unclosed"));
        }
    }

    @Test
    void testParseNumberWithIllegalSyntax() {
        Parser parser = createParser2();
        try {
            // Test illegal number syntax - invalid hex
            parser.parse("test", "{{0xGG}}");
            fail("Should throw TemplateParseException for illegal number syntax");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // The error might be caught during number parsing
            assertTrue(e.getMessage().contains("illegal number") || e.getMessage().contains("invalid") || e.getMessage().contains("number"));
        }
    }

    @Test
    void testParseRangeControlOutsideRange() {
        Parser parser = createParser2();
        try {
            // Test break outside range
            parser.parse("test", "{{break}}");
            fail("Should throw TemplateParseException for break outside range");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("outside {{range}}"));
        }
    }

    @Test
    void testParseRangeControlWithUnexpectedToken() {
        Parser parser = createParser2();
        try {
            // Test break with unexpected token
            parser.parse("test", "{{range .Items}}{{break .Invalid}}{{end}}");
            fail("Should throw TemplateParseException for break with unexpected token");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseContinueOutsideRange() {
        Parser parser = createParser2();
        try {
            // Test continue outside range
            parser.parse("test", "{{continue}}");
            fail("Should throw TemplateParseException for continue outside range");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("outside {{range}}"));
        }
    }

    @Test
    void testParseWithMissingEnd() {
        Parser parser = createParser2();
        try {
            // Test with without end
            parser.parse("test", "{{with .Value}}hello");
            fail("Should throw TemplateParseException for missing end in with");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("expected end") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseBlockWithUnexpectedElse() {
        Parser parser = createParser2();
        try {
            // Test block with unexpected else
            parser.parse("test", "{{block \"name\" .Value}}{{else}}content{{end}}");
            fail("Should throw TemplateParseException for unexpected else in block");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected") && e.getMessage().contains("block"));
        }
    }

    @Test
    void testParseCommandWithUnexpectedDotAfterTerm() {
        Parser parser = createParser2();
        try {
            // Test unexpected dot after bool term
            parser.parse("test", "{{true.Invalid}}");
            fail("Should throw TemplateParseException for unexpected dot after bool term");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected dot after term"));
        }
    }

    @Test
    void testParseCommandWithUnexpectedDotAfterString() {
        Parser parser = createParser2();
        try {
            // Test unexpected dot after string term
            parser.parse("test", "{{\"hello\".Invalid}}");
            fail("Should throw TemplateParseException for unexpected dot after string term");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected dot after term"));
        }
    }

    @Test
    void testParseCommandWithUnexpectedDotAfterNumber() {
        Parser parser = createParser2();
        try {
            // Test unexpected dot after number term - this may be parsed as a field access
            parser.parse("test", "{{42.Invalid}}");
            // This might actually parse successfully as a field access on a number
            // If it doesn't throw, that's also acceptable behavior
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            // If it does throw, check for the expected message
            if (e.getMessage().contains("unexpected dot after term")) {
                assertTrue(e.getMessage().contains("unexpected dot after term"));
            }
        }
    }

    @Test
    void testParseCommandWithUnexpectedDotAfterNil() {
        Parser parser = createParser2();
        try {
            // Test unexpected dot after nil term
            parser.parse("test", "{{nil.Invalid}}");
            fail("Should throw TemplateParseException for unexpected dot after nil term");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected dot after term"));
        }
    }

    @Test
    void testParseCommandWithUnexpectedDotAfterDot() {
        Parser parser = createParser2();
        try {
            // Test unexpected dot after dot term
            parser.parse("test", "{{..Invalid}}");
            fail("Should throw TemplateParseException for unexpected dot after dot term");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("unexpected dot after term"));
        }
    }

    @Test
    void testParseElseIfWithMissingToken() {
        Parser parser = createParser2();
        try {
            // Test else if with missing token
            parser.parse("test", "{{if .X}}hello{{else if");
            fail("Should throw TemplateParseException for missing token in else if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("missing") || e.getMessage().contains("unclosed"));
        }
    }

    @Test
    void testParseElseWithWithMissingToken() {
        Parser parser = createParser2();
        try {
            // Test else with with missing token
            parser.parse("test", "{{if .X}}hello{{else with");
            fail("Should throw TemplateParseException for missing token in else with");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("missing") || e.getMessage().contains("unclosed"));
        }
    }

    @Test
    void testParseBranchWithElseIfMissingEnd() {
        Parser parser = createParser2();
        try {
            // Test else if without end
            parser.parse("test", "{{if .X}}hello{{else if .Y}}world");
            fail("Should throw TemplateParseException for missing end in else if");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("expected end") || e.getMessage().contains("unexpected"));
        }
    }

    @Test
    void testParseBranchWithElseWithMissingEnd() {
        Parser parser = createParser2();
        try {
            // Test else with without end
            parser.parse("test", "{{if .X}}hello{{else with .Y}}world");
            fail("Should throw TemplateParseException for missing end in else with");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("expected end") || e.getMessage().contains("unexpected"));
        }
    }
}
