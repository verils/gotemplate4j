package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.TextNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.verils.gotemplate.internal.ParserTestSupport.createParser2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTemplateDefinitionTest {

    @Test
    void testParseBlockWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{block .Name .}}content{{end}}"));
    }


    @Test
    void testParseBlockMissingEndToken() {
        Parser parser = createParser2();
        assertThrows(Exception.class, () -> parser.parse("test", "{{block \"name\" .}}content"));
    }


    @Test
    void testParseBlockWithUnexpectedElse() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{block \"name\" .}}content{{else}}more{{end}}"));
    }

    // Test parseDefinition error paths

    @Test
    void testParseDefinitionWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define .Name}}content{{end}}"));
    }


    @Test
    void testParseDefinitionMissingRightDelim() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\" content{{end}}"));
    }


    @Test
    void testParseDefinitionMissingEnd() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\"}}content"));
    }


    @Test
    void testParseDefinitionWithUnexpectedLastNode() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{define \"name\"}}content{{else}}other{{end}}"));
    }

    // Test parseTemplate error paths

    @Test
    void testParseTemplateWithNonStringName() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{template .Name}}"));
    }


    @Test
    void testParseTemplateWithPipeline() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test parseElse error paths

    @Test
    void testParseElseWithUnexpectedToken() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}true{{else invalid}}false{{end}}"));
    }

    // Test parseEnd error paths

    @Test
    void testParseEndWithUnexpectedToken() {
        Parser parser = createParser2();
        assertThrows(TemplateParseException.class, () -> parser.parse("test", "{{if .X}}true{{end invalid}}"));
    }

    // Test variable declaration edge cases

    @Test
    void testDefineAndUseTemplate() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"mytemplate\"}}Hello{{end}}");
        assertNotNull(nodes.get("mytemplate"));
    }

    // Test block with argument

    @Test
    void testBlockWithArgument() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{block \"foo\" .Arg}}content{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("foo"));
    }

    // Test nested if-else-if chains

    @Test
    void testMultipleTemplateDefinitions() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test",
                "{{define \"t1\"}}T1{{end}}{{define \"t2\"}}T2{{end}}");
        assertNotNull(nodes.get("t1"));
        assertNotNull(nodes.get("t2"));
    }

    @Test
    void duplicateNonEmptyDefinitionsAreRejectedWithinOneParse() {
        Parser parser = createParser2();

        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> parser.parse("test",
                        "{{define \"item\"}}First{{end}}{{define \"item\"}}Second{{end}}"));

        assertTrue(exception.getMessage().contains("multiple definition of template \"item\""));
        assertTrue(exception.getLine() > 0);
        assertTrue(exception.getColumn() > 0);
    }

    @Test
    void emptyDefinitionDoesNotBlockLaterDefinitionInSameParse() throws TemplateParseException {
        Parser parser = createParser2();

        Map<String, Node> nodes = parser.parse("test",
                "{{define \"item\"}}{{end}}{{define \"item\"}}Second{{end}}");

        assertEquals("Second", firstText(nodes.get("item")));
    }

    @Test
    void emptyOrCommentOnlyDefinitionDoesNotReplaceEarlierDefinitionInSameParse()
            throws TemplateParseException {
        Parser parser = createParser2();

        Map<String, Node> nodes = parser.parse("test",
                "{{define \"item\"}}First{{end}}" +
                        "{{define \"item\"}} \t\n {{end}}" +
                        "{{define \"item\"}}{{/* comment */}}{{end}}");

        assertEquals("First", firstText(nodes.get("item")));
    }

    @Test
    void blockAndDefineWithSameNameAreDuplicateWithinOneParse() {
        Parser parser = createParser2();

        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> parser.parse("test",
                        "{{block \"item\" .}}Block{{end}}{{define \"item\"}}Define{{end}}"));

        assertTrue(exception.getMessage().contains("multiple definition of template \"item\""));
    }

    @Test
    void nonEmptyRootAndSameNamedDefinitionAreDuplicateWithinOneParse() {
        Parser parser = createParser2();

        TemplateParseException exception = assertThrows(TemplateParseException.class,
                () -> parser.parse("root", "Root{{define \"root\"}}Definition{{end}}"));

        assertTrue(exception.getMessage().contains("multiple definition of template \"root\""));
    }

    @Test
    void sameNamedDefinitionCanSupplyRootWhenRootBodyIsEmpty() throws TemplateParseException {
        Parser parser = createParser2();

        Map<String, Node> nodes = parser.parse(
                "root", "{{define \"root\"}}Definition{{end}}");

        assertEquals("Definition", firstText(nodes.get("root")));
    }

    // Test template invocation with argument

    @Test
    void testTemplateInvocationWithArgument() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\" .Arg}}");
        assertNotNull(nodes.get("test"));
    }

    // Test error in nested pipeline

    @Test
    void testDefineWithEmptyName() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"\"}}empty{{end}}");
        assertNotNull(nodes.get(""));
    }

    // Test template with special characters in name

    @Test
    void testTemplateWithSpecialCharactersInName() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template-with-dash\"}}content{{end}}");
        assertNotNull(nodes.get("template-with-dash"));
    }

    // Test unclosed delimiter with trim - this causes lexer error

    @Test
    void testVeryLongTemplateName() throws TemplateParseException {
        Parser parser = createParser2();
        StringBuilder sb = new StringBuilder("template_");
        for (int i = 0; i < 100; i++) {
            sb.append('x');
        }
        String longName = sb.toString();
        Map<String, Node> nodes = parser.parse("test",
                "{{define \"" + longName + "\"}}content{{end}}");
        assertNotNull(nodes.get(longName));
    }


    // Test mixed delimiters (default delimiters)

    @Test
    void testBlockWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{block \"empty\" .}}{{end}}");
        assertNotNull(nodes.get("test"));
        assertNotNull(nodes.get("empty"));
    }

    // Test define with empty content

    @Test
    void testDefineWithEmptyContent() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"empty\"}}{{end}}");
        assertNotNull(nodes.get("empty"));
    }

    // Test template with no arguments

    @Test
    void testTemplateWithNoArguments() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{template \"name\"}}");
        assertNotNull(nodes.get("test"));
    }

    // Test multiple variables in same scope

    @Test
    void testTemplateNameWithUnderscores() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"my_template_name\"}}content{{end}}");
        assertNotNull(nodes.get("my_template_name"));
    }

    // Test template name with numbers

    @Test
    void testTemplateNameWithNumbers() throws TemplateParseException {
        Parser parser = createParser2();
        Map<String, Node> nodes = parser.parse("test", "{{define \"template123\"}}content{{end}}");
        assertNotNull(nodes.get("template123"));
    }

    private static String firstText(Node node) {
        ListNode listNode = (ListNode) node;
        return ((TextNode) listNode.iterator().next()).getText();
    }

}
