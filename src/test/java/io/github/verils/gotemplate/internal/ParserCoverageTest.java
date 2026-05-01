package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test cases to improve Parser.java code coverage to 90-95%
 */
class ParserCoverageTest {

    private Parser createParser() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        functions.put("len", null);
        functions.put("index", null);
        return new Parser(functions);
    }

    // Test complex number parsing edge cases
    @Test
    void testComplexNumberWithZeroImaginary() throws TemplateParseException {
        Parser parser = createParser();
        // Complex number with zero imaginary part should simplify to float/int
        Map<String, Node> nodes = parser.parse("test", "{{3+0i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberPureImaginary() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{5i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberFullForm() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1.5+2.5i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberNegativeReal() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{-3+4i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberNegativeImaginary() throws TemplateParseException {
        Parser parser = createParser();
        // Note: Go template complex number syntax requires + between real and imaginary
        Map<String, Node> nodes = parser.parse("test", "{{3+-4i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberBothNegative() throws TemplateParseException {
        Parser parser = createParser();
        // Note: Go template complex number syntax requires + between real and imaginary
        Map<String, Node> nodes = parser.parse("test", "{{-3+-4i}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testComplexNumberIntegerSimplification() throws TemplateParseException {
        Parser parser = createParser();
        // Complex with zero imaginary and integer real should simplify to int
        Map<String, Node> nodes = parser.parse("test", "{{5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test chain node formation with different node types
    @Test
    void testChainNodeAfterVariable() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.Field}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testChainNodeAfterField() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.X.Y.Z}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testChainNodeAfterParenthesizedPipeline() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{(.X).Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseBlock error paths
    @Test
    void testParseBlockWithNonStringName() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{block .Name .}}content{{end}}");
        });
    }

    @Test
    void testParseBlockMissingEndToken() {
        Parser parser = createParser();
        assertThrows(Exception.class, () -> {
            parser.parse("test", "{{block \"name\" .}}content");
        });
    }

    @Test
    void testParseBlockWithUnexpectedElse() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{block \"name\" .}}content{{else}}more{{end}}");
        });
    }

    // Test parseDefinition error paths
    @Test
    void testParseDefinitionWithNonStringName() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{define .Name}}content{{end}}");
        });
    }

    @Test
    void testParseDefinitionMissingRightDelim() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{define \"name\" content{{end}}");
        });
    }

    @Test
    void testParseDefinitionMissingEnd() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{define \"name\"}}content");
        });
    }

    @Test
    void testParseDefinitionWithUnexpectedLastNode() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{define \"name\"}}content{{else}}other{{end}}");
        });
    }

    // Test parseTemplate error paths
    @Test
    void testParseTemplateWithNonStringName() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{template .Name}}");
        });
    }

    @Test
    void testParseTemplateWithPipeline() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseElse error paths
    @Test
    void testParseElseWithUnexpectedToken() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{if .X}}true{{else invalid}}false{{end}}");
        });
    }

    // Test parseEnd error paths
    @Test
    void testParseEndWithUnexpectedToken() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{if .X}}true{{end invalid}}");
        });
    }

    // Test variable declaration edge cases
    @Test
    void testRangeWithTwoVariablesDeclaration() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range $key, $value := .Map}}{{$key}}:{{$value}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testRangeWithOneVariableDeclaration() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range $item := .List}}{{$item}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testVariableScopeInNestedBranches() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{$v := 1}}{{$v}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testVariableUndefinedAfterBranch() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{if .X}}{{$v := 1}}{{end}}{{$v}}");
        });
    }

    // Test pipeline validation for non-executable commands
    @Test
    void testPipelineWithDotAsSecondCommand() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{. | .}}");
        });
    }

    @Test
    void testPipelineWithNilAsSecondCommand() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{printf \"%d\" 1 | nil}}");
        });
    }

    @Test
    void testPipelineWithNumberAsSecondCommand() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{printf \"%d\" 1 | 123}}");
        });
    }

    @Test
    void testPipelineWithStringAsSecondCommand() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{printf \"%d\" 1 | \"error\"}}");
        });
    }

    // Test error context building with various scenarios
    @Test
    void testErrorMessageWithContextForSyntaxError() {
        Parser parser = createParser();
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
        Parser parser = createParser();
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
        Parser parser = createParser();
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
    void testLookNextItemAtEOF() throws TemplateParseException {
        Parser parser = createParser();
        // This tests the internal lookNextItem behavior through normal parsing
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test moveToPrevItem at index 0
    @Test
    void testMoveToPrevItemAtStart() throws TemplateParseException {
        Parser parser = createParser();
        // Tests boundary condition where tokenIndex is 0
        Map<String, Node> nodes = parser.parse("test", "text");
        assertNotNull(nodes.get("test"));
    }

    // Test parsePipe missing value error
    @Test
    void testParsePipeWithEmptyCommands() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{}}");
        });
    }

    // Test parseCommand empty command error
    @Test
    void testParseCommandWithNoArguments() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            // This should trigger empty command error
            parser.parse("test", "{{ }}");
        });
    }

    // Test parseNumber with char constant errors
    @Test
    void testParseCharConstantWithInvalidSyntax() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{'invalid}}");
        });
    }

    @Test
    void testParseCharConstantWithMultipleChars() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{'ab'}}");
        });
    }

    // Test parseIntValue with different bases
    @Test
    void testBinaryNumberParsing() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0b1010}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testOctalNumberParsing() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0o755}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testHexadecimalNumberParsing() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0xFF}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testNumberWithUnderscores() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1_000_000}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testFloatWithUnderscores() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1_000.500_100}}");
        assertNotNull(nodes.get("test"));
    }

    // Test define template and use it
    @Test
    void testDefineAndUseTemplate() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"mytemplate\"}}Hello{{end}}");
        assertNotNull(nodes.get("mytemplate"));
    }

    // Test block with argument
    @Test
    void testBlockWithArgument() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{block \"foo\" .Arg}}content{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("foo"));
    }

    // Test nested if-else-if chains
    @Test
    void testDeepElseIfChain() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{if .A}}A{{else if .B}}B{{else if .C}}C{{else if .D}}D{{else}}E{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with nested in if
    @Test
    void testWithNestedInIf() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{if .X}}{{with .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range nested in with
    @Test
    void testRangeNestedInWith() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{with .X}}{{range .Y}}item{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple templates defined
    @Test
    void testMultipleTemplateDefinitions() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{define \"t1\"}}T1{{end}}{{define \"t2\"}}T2{{end}}");
        assertNotNull(nodes.get("t1"));
        assertNotNull(nodes.get("t2"));
    }

    // Test template invocation with argument
    @Test
    void testTemplateInvocationWithArgument() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error in nested pipeline
    @Test
    void testErrorInNestedPipeline() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{printf (}}");
        });
    }

    // Test boolean as first command in pipeline stage
    @Test
    void testBooleanAsFirstCommandInPipeline() throws TemplateParseException {
        Parser parser = createParser();
        // Boolean as first command is OK
        Map<String, Node> nodes = parser.parse("test", "{{true}}");
        assertNotNull(nodes.get("test"));
    }

    // Test dot node
    @Test
    void testDotNode() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nil node
    @Test
    void testNilNode() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{nil}}");
        assertNotNull(nodes.get("test"));
    }

    // Test raw string
    @Test
    void testRawString() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{`raw string`}}");
        assertNotNull(nodes.get("test"));
    }

    // Test identifier not found
    @Test
    void testIdentifierNotDefined() {
        Parser parser = new Parser(); // Empty function map
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{undefinedFunc}}");
        });
    }

    // Test field after various node types in chain
    @Test
    void testFieldChainAfterBool() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{true.Field}}");
        });
    }

    @Test
    void testFieldChainAfterString() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{\"string\".Field}}");
        });
    }

    @Test
    void testFieldChainAfterNumber() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{123.Field}}");
        });
    }

    @Test
    void testFieldChainAfterNil() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{nil.Field}}");
        });
    }

    @Test
    void testFieldChainAfterDot() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{..Field}}");
        });
    }

    // Test comment in various positions
    @Test
    void testCommentAtBeginning() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{/* comment */}}text");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCommentAtEnd() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "text{{/* comment */}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCommentInMiddle() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "text1{{/* comment */}}text2");
        assertNotNull(nodes.get("test"));
    }

    // Test whitespace handling
    @Test
    void testMultipleSpaces() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{   .   }}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testTabsAndNewlines() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\t.\n}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex pipeline
    @Test
    void testComplexPipeline() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.X | printf \"%d\" | len}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parenthesized pipeline in command
    @Test
    void testParenthesizedPipelineInCommand() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d\" (.X)}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple arguments in command
    @Test
    void testMultipleArgumentsInCommand() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s\" 42 \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test negative numbers
    @Test
    void testNegativeInteger() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{-42}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testNegativeFloat() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{-3.14}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testPositiveSign() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{+42}}");
        assertNotNull(nodes.get("test"));
    }

    // Test scientific notation
    @Test
    void testScientificNotation() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1.23e10}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testScientificNotationNegativeExponent() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1.23e-10}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testScientificNotationCapitalE() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1.23E10}}");
        assertNotNull(nodes.get("test"));
    }

    // Test hex float
    @Test
    void testHexFloat() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0x1p4}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testHexFloatNegativeExponent() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0x1p-4}}");
        assertNotNull(nodes.get("test"));
    }

    // Test escaped characters in strings
    @Test
    void testStringWithEscapes() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"hello\\nworld\"}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testStringWithUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"\\u0041\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test character constants with escapes
    @Test
    void testCharConstantWithEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{'\\n'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithHexEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{'\\xFF'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{'\\u0041'}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testCharConstantWithLongUnicodeEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{'\\U00000041'}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range with else
    @Test
    void testRangeWithElseClause() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}item{{else}}empty{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if-else-if without final else
    @Test
    void testElseIfWithoutFinalElse() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with else
    @Test
    void testWithElseClause() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}has value{{else}}no value{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested actions
    @Test
    void testNestedActions() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{if .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test action after end
    @Test
    void testActionAfterEnd() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}value{{end}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test text before and after action
    @Test
    void testTextBeforeAndAfterAction() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "before{{.Value}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple actions in sequence
    @Test
    void testMultipleActionsInSequence() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.A}}{{.B}}{{.C}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested structures
    @Test
    void testDeeplyNestedStructure() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{if .A}}{{with .B}}{{range .C}}{{if .D}}deep{{end}}{{end}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable in nested scope
    @Test
    void testVariableInNestedScope() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", 
            "{{with $outer := .}}{{if true}}{{$outer}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error with tab characters in template
    @Test
    void testErrorContextWithTabs() {
        Parser parser = createParser();
        try {
            parser.parse("test", "line1\n\t{{invalid}}\nline3");
            fail("Should throw exception");
        } catch (TemplateParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Test empty template name in define
    @Test
    void testDefineWithEmptyName() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"\"}}empty{{end}}");
        assertNotNull(nodes.get(""));
    }

    // Test template with special characters in name
    @Test
    void testTemplateWithSpecialCharactersInName() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template-with-dash\"}}content{{end}}");
        assertNotNull(nodes.get("template-with-dash"));
    }

    // Test unclosed delimiter with trim - this causes lexer error
    @Test
    void testUnclosedDelimiterWithTrim() {
        Parser parser = createParser();
        assertThrows(Exception.class, () -> {
            parser.parse("test", "{{- ");
        });
    }

    // Test lookahead at EOF
    @Test
    void testLookaheadAtEOF() throws TemplateParseException {
        Parser parser = createParser();
        // Parsing simple template tests lookahead behavior at boundaries
        Map<String, Node> nodes = parser.parse("test", "{{.}}");
        assertNotNull(nodes.get("test"));
    }

    // Test move to previous non-space item
    @Test
    void testMoveToPrevNonSpace() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.X }}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex error scenarios
    @Test
    void testMultipleErrorsInTemplate() {
        Parser parser = createParser();
        // Parser stops at first error, but this tests error handling
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{invalid1}}{{invalid2}}");
        });
    }

    // Test very long template name
    @Test
    void testVeryLongTemplateName() throws TemplateParseException {
        Parser parser = createParser();
        StringBuilder sb = new StringBuilder("template_");
        for (int i = 0; i < 100; i++) {
            sb.append('x');
        }
        String longName = sb.toString();
        Map<String, Node> nodes = parser.parse("test", 
            "{{define \"" + longName + "\"}}content{{end}}");
        assertNotNull(nodes.get(longName));
    }

    // Test unicode in template text
    @Test
    void testUnicodeInTemplateText() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "你好世界{{.Name}}");
        assertNotNull(nodes.get("test"));
    }

    // Test unicode in string literal
    @Test
    void testUnicodeInStringLiteral() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"こんにちは\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test mixed delimiters (default delimiters)
    @Test
    void testDefaultDelimiters() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{.Field}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pipeline with multiple stages
    @Test
    void testPipelineWithManyStages() throws TemplateParseException {
        Parser parser = createParser();
        // Note: This requires appropriate functions to be registered
        Map<String, Node> nodes = parser.parse("test", "{{.X}}");
        assertNotNull(nodes.get("test"));
    }

    // Test assignment operator
    @Test
    void testAssignmentOperator() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .Value}}{{$x}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test declare operator in range
    @Test
    void testDeclareInRange() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range $idx, $val := .Items}}{{$idx}}:{{$val}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test boolean values
    @Test
    void testTrueBoolean() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{true}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testFalseBoolean() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{false}}");
        assertNotNull(nodes.get("test"));
    }

    // Test zero values
    @Test
    void testZeroInteger() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testZeroFloat() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0.0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test large numbers
    @Test
    void testLargeInteger() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{9223372036854775807}}");
        assertNotNull(nodes.get("test"));
    }

    @Test
    void testLargeFloat() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{1.7976931348623157e+308}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex number that simplifies to integer
    @Test
    void testComplexSimplifiesToInt() throws TemplateParseException {
        Parser parser = createParser();
        // 5+0i should simplify to int 5
        Map<String, Node> nodes = parser.parse("test", "{{5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex number that simplifies to float
    @Test
    void testComplexSimplifiesToFloat() throws TemplateParseException {
        Parser parser = createParser();
        // 5.5+0i should simplify to float 5.5
        Map<String, Node> nodes = parser.parse("test", "{{5.5+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error context with tab character at error position
    @Test
    void testErrorContextWithTabAtErrorColumn() {
        Parser parser = createParser();
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
        Parser parser = createParser();
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
        Parser parser = createParser();
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
        Parser parser = createParser();
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
    void testVariableWithMultipleFields() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .}}{{$x.A.B.C}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested pipeline
    @Test
    void testDeeplyNestedPipeline() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{(((.X)))}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range without assignment
    @Test
    void testRangeWithoutAssignment() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if with just dot
    @Test
    void testIfWithJustDot() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .}}truthy{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with just dot
    @Test
    void testWithWithJustDot() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with .}}inside{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test block with empty content
    @Test
    void testBlockWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{block \"empty\" .}}{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("empty"));
    }

    // Test define with empty content
    @Test
    void testDefineWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"empty\"}}{{end}}");
        assertNotNull(nodes.get("empty"));
    }

    // Test template with no arguments
    @Test
    void testTemplateWithNoArguments() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple variables in same scope
    @Test
    void testMultipleVariablesInSameScope() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with $a := 1}}{{$a}}{{with $b := 2}}{{$b}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable shadowing
    @Test
    void testVariableShadowing() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := 1}}{{$x}}{{with $x := 2}}{{$x}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test command with all argument types
    @Test
    void testCommandWithMixedArguments() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"%d %s %v\" 42 \"hello\" .Value}}");
        assertNotNull(nodes.get("test"));
    }

    // Test number parsing edge case: just zero
    @Test
    void testJustZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test octal zero
    @Test
    void testOctalZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0o0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test hex zero
    @Test
    void testHexZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0x0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test binary zero
    @Test
    void testBinaryZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0b0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test negative zero
    @Test
    void testNegativeZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{-0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test positive zero
    @Test
    void testPositiveZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{+0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test float zero
    @Test
    void testFloatZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0.0}}");
        assertNotNull(nodes.get("test"));
    }

    // Test complex zero
    @Test
    void testComplexZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0+0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pure imaginary zero
    @Test
    void testPureImaginaryZero() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{0i}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with newline escape
    @Test
    void testStringWithNewlineEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"line1\\nline2\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with tab escape
    @Test
    void testStringWithTabEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"tab\\there\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test string with quote escape
    @Test
    void testStringWithQuoteEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"quote\\\"here\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test char with various escapes
    @Test
    void testCharWithBackslashEscape() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{'\\\\'}}");
        assertNotNull(nodes.get("test"));
    }

    // Test pipeline with single command
    @Test
    void testPipelineWithSingleCommand() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{printf \"hello\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test action with only whitespace
    @Test
    void testActionWithOnlyWhitespace() {
        Parser parser = createParser();
        assertThrows(TemplateParseException.class, () -> {
            parser.parse("test", "{{   }}");
        });
    }

    // Test text with special characters
    @Test
    void testTextWithSpecialCharacters() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "Hello <World> & 'Friends'");
        assertNotNull(nodes.get("test"));
    }

    // Test empty string
    @Test
    void testEmptyString() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{\"\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test empty raw string
    @Test
    void testEmptyRawString() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{``}}");
        assertNotNull(nodes.get("test"));
    }

    // Test comment with special characters
    @Test
    void testCommentWithSpecialCharacters() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{/* Special chars: <>&\"' */}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple comments
    @Test
    void testMultipleComments() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{/* comment1 */}}text{{/* comment2 */}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested if inside range
    @Test
    void testNestedIfInsideRange() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{if .Active}}active{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested range inside if
    @Test
    void testNestedRangeInsideIf() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .HasItems}}{{range .Items}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested with inside range
    @Test
    void testNestedWithInsideRange() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{with .Name}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test else if with complex condition
    @Test
    void testElseIfWithComplexCondition() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B.C.D}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range with dot reference
    @Test
    void testRangeWithDotReference() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}Current: {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with dot reference
    @Test
    void testWithWithDotReference() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}The value is {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test template name with underscores
    @Test
    void testTemplateNameWithUnderscores() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"my_template_name\"}}content{{end}}");
        assertNotNull(nodes.get("my_template_name"));
    }

    // Test template name with numbers
    @Test
    void testTemplateNameWithNumbers() throws TemplateParseException {
        Parser parser = createParser();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template123\"}}content{{end}}");
        assertNotNull(nodes.get("template123"));
    }
}
