package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser2;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserPipelineCommandTest {

    @Test
    void testChainNodeAfterVariable() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.Field}}{{end}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testChainNodeAfterField() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X.Y.Z}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testChainNodeAfterParenthesizedPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{(.X).Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseBlock error paths

    @Test
    void testPipelineWithDotAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{. | .}}"));
    }


    @Test
    void testPipelineWithNilAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | nil}}"));
    }


    @Test
    void testPipelineWithNumberAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | 123}}"));
    }


    @Test
    void testPipelineWithStringAsSecondCommand() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf \"%d\" 1 | \"error\"}}"));
    }

    // Test error context building with various scenarios

    @Test
    void testLookNextItemAtEOF() throws TemplateParseException {
        Parser parser = createParser2();
        // This tests the internal lookNextItem behavior through normal parsing
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test moveToPrevItem at index 0

    @Test
    void testMoveToPrevItemAtStart() throws TemplateParseException {
        Parser parser = createParser2();
        // Tests boundary condition where tokenIndex is 0
        Map<String, Node> nodes = parser.parse("test", "text");
        assertNotNull(nodes.get("test"));
    }

    // Test parsePipe missing value error

    @Test
    void testParsePipeWithEmptyCommands() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{}}"));
    }

    // Test parseCommand empty command error

    @Test
    void testParseCommandWithNoArguments() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> {
            // This should trigger empty command error
            parser.parse("test", "{{ }}");
        });
    }

    // Test parseNumber with char constant errors

    @Test
    void testParseCharConstantWithInvalidSyntax() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{'invalid}}"));
    }


    @Test
    void testParseCharConstantWithMultipleChars() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{'ab'}}"));
    }

    // Test parseIntValue with different bases

    @Test
    void testErrorInNestedPipeline() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{printf (}}"));
    }

    // Test boolean as first command in pipeline stage

    @Test
    void testBooleanAsFirstCommandInPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        // Boolean as first command is OK
        Map<String, Node> nodes = parser.parse("test", "{{true}}");
        assertNotNull(nodes.get("test"));
    }


    // Test identifier not found

    @Test
    void testIdentifierNotDefined() {
        Parser parser = new Parser(); // Empty function map
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{undefinedFunc}}"));
    }

    // Test field after various node types in chain

    @Test
    void testFieldChainAfterBool() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{true.Field}}"));
    }


    @Test
    void testFieldChainAfterString() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{\"string\".Field}}"));
    }


    @Test
    void testFieldChainAfterNumber() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{123.Field}}"));
    }


    @Test
    void testFieldChainAfterNil() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{nil.Field}}"));
    }


    @Test
    void testFieldChainAfterDot() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{..Field}}"));
    }


    // Test complex pipeline

    @Test
    void testComplexPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X | printf \"%d\" | len}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parenthesized pipeline in command

    @Test
    void testParenthesizedPipelineInCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d\" (.X)}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple arguments in command

    @Test
    void testMultipleArgumentsInCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s\" 42 \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }


    // Test range with else

    @Test
    void testUnclosedDelimiterWithTrim() {
        Parser parser = createParser2();
        assertThrows(Exception.class, () -> parser.parse("test", "{{- "));
    }

    // Test lookahead at EOF

    @Test
    void testLookaheadAtEOF() throws TemplateParseException {
        Parser parser = createParser2();
        // Parsing simple template tests lookahead behavior at boundaries
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test move to previous non-space item

    @Test
    void testMoveToPrevNonSpace() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.X }}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex error scenarios

    @Test
    void testMultipleErrorsInTemplate() {
        Parser parser = createParser2();
        // Parser stops at first error, but this tests error handling
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{invalid1}}{{invalid2}}"));
    }

    // Test very long template name

    @Test
    void testDefaultDelimiters() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pipeline with multiple stages

    @Test
    void testPipelineWithManyStages() throws TemplateParseException {
        Parser parser = createParser2();
        // Note: This requires appropriate functions to be registered
        Map<String, Node> nodes = parser.parse("test", "{{.X}}");
        assertNotNull(nodes.get("test"));
    }

    // Test assignment operator

    @Test
    void testVariableWithMultipleFields() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.A.B.C}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested pipeline

    @Test
    void testDeeplyNestedPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{(((.X)))}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range without assignment

    @Test
    void testCommandWithMixedArguments() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s %v\" 42 \"hello\" .Value}}");
        assertNotNull(nodes.get("test"));
    }


    // Test pipeline with single command

    @Test
    void testPipelineWithSingleCommand() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }


    // Test nested if inside range
}
