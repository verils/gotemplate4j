package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.NumberNode;
import io.github.verils.gotemplate.internal.lang.Complex;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
class ParserCoverageErrorTest extends ParserTestSupport {

    @Test
    void testParserWithUnclosedAction() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed action", "{{ .Name"));
    }


    @Test
    void testParserWithUnclosedIf() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed if", "{{if .Name}}hello"));
    }


    @Test
    void testParserWithUnclosedRange() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed range", "{{range .Items}}item"));
    }


    @Test
    void testParserWithUnclosedWith() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unclosed with", "{{with .Name}}hello"));
    }


    @Test
    void testParserWithUnexpectedEnd() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unexpected end", "{{end}}"));
    }


    @Test
    void testParserWithUnexpectedElse() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("unexpected else", "{{else}}"));
    }


    @Test
    void testParserWithInvalidVariableDeclaration() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("invalid var decl", "{{:= .Name}}"));
    }


    @Test
    void testParserWithMissingPipelineInIf() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in if", "{{if}}"));
    }


    @Test
    void testParserWithMissingPipelineInRange() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in range", "{{range}}"));
    }


    @Test
    void testParserWithMissingPipelineInWith() {
        Parser parser = new Parser();
        assertThrows(Exception.class, () -> parser.parse("missing pipeline in with", "{{with}}"));
    }
}
