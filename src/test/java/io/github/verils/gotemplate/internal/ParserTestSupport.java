package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class ParserTestSupport {

    protected static final Logger log = Logger.getLogger(ParserTestSupport.class.getName());

    protected static void assertOK(String name, String input, String result) throws TemplateParseException {
        Parser parser = createParser1();
        Map<String, Node> nodes = parser.parse(name, input);
        Node node = nodes.get(name);
        assertNotNull(node);
        assertInstanceOf(ListNode.class, node);
        assertEquals(result, node.toString());
    }

    protected static void assertError(String name, String text) {
        Parser parser = createParser1();
        assertThrows(TemplateParseException.class, () -> parser.parse(name, text));
    }

    protected static Parser createParser1() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        return new Parser(functions);
    }

    protected static Parser createParser2() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        functions.put("len", null);
        functions.put("index", null);
        return new Parser(functions);
    }
}