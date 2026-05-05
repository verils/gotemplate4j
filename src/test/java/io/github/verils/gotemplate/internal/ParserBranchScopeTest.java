package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
class ParserBranchScopeTest extends ParserTestSupport {

    @Test
    void testRangeWithTwoVariablesDeclaration() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $key, $value := .Map}}{{$key}}:{{$value}}{{end}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testRangeWithOneVariableDeclaration() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $item := .List}}{{$item}}{{end}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testVariableScopeInNestedBranches() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{$v := 1}}{{$v}}{{end}}");
        assertNotNull(nodes.get("test"));
    }


    @Test
    void testVariableUndefinedAfterBranch() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}{{$v := 1}}{{end}}{{$v}}"));
    }

    // Test pipeline validation for non-executable commands

    @Test
    void testDeepElseIfChain() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .A}}A{{else if .B}}B{{else if .C}}C{{else if .D}}D{{else}}E{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with nested in if

    @Test
    void testWithNestedInIf() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .X}}{{with .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range nested in with

    @Test
    void testRangeNestedInWith() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{with .X}}{{range .Y}}item{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple templates defined

    @Test
    void testRangeWithElseClause() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}item{{else}}empty{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if-else-if without final else

    @Test
    void testElseIfWithoutFinalElse() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with else

    @Test
    void testWithElseClause() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}has value{{else}}no value{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested actions

    @Test
    void testNestedActions() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}{{if .Y}}nested{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test action after end

    @Test
    void testActionAfterEnd() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .X}}value{{end}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test text before and after action

    @Test
    void testTextBeforeAndAfterAction() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "before{{.Value}}after");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple actions in sequence

    @Test
    void testMultipleActionsInSequence() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{.A}}{{.B}}{{.C}}");
        assertNotNull(nodes.get("test"));
    }

    // Test deeply nested structures

    @Test
    void testDeeplyNestedStructure() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{if .A}}{{with .B}}{{range .C}}{{if .D}}deep{{end}}{{end}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable in nested scope

    @Test
    void testVariableInNestedScope() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{with $outer := .}}{{if true}}{{$outer}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error with tab characters in template

    @Test
    void testAssignmentOperator() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := .Value}}{{$x}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test declare operator in range

    @Test
    void testDeclareInRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range $idx, $val := .Items}}{{$idx}}:{{$val}}{{end}}");
        assertNotNull(nodes.get("test"));
    }


    // Test error context with tab character at error position

    @Test
    void testRangeWithoutAssignment() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test if with just dot

    @Test
    void testIfWithJustDot() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .}}truthy{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with just dot

    @Test
    void testWithWithJustDot() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .}}inside{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test block with empty content

    @Test
    void testMultipleVariablesInSameScope() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $a := 1}}{{$a}}{{with $b := 2}}{{$b}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test variable shadowing

    @Test
    void testVariableShadowing() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with $x := 1}}{{$x}}{{with $x := 2}}{{$x}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test command with all argument types

    @Test
    void testNestedIfInsideRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{if .Active}}active{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested range inside if

    @Test
    void testNestedRangeInsideIf() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .HasItems}}{{range .Items}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test nested with inside range

    @Test
    void testNestedWithInsideRange() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}{{with .Name}}{{.}}{{end}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test else if with complex condition

    @Test
    void testElseIfWithComplexCondition() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{if .A}}A{{else if .B.C.D}}B{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test range with dot reference

    @Test
    void testRangeWithDotReference() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{range .Items}}Current: {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test with with dot reference

    @Test
    void testWithWithDotReference() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{with .Value}}The value is {{.}}{{end}}");
        assertNotNull(nodes.get("test"));
    }

    // Test template name with underscores
}
