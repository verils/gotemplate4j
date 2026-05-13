package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class ParserTestSupport {

    private ParserTestSupport() {
    }

    static void assertOK(String name, String input, String result) throws TemplateParseException {
        Parser parser = createParser1();
        Map<String, Node> nodes = parser.parse(name, input);
        Node node = nodes.get(name);
        assertNotNull(node);
        assertInstanceOf(ListNode.class, node);
        assertEquals(result, node.toString());
    }

    static void assertError(String name, String text) {
        Parser parser = createParser1();
        assertThrows(TemplateParseException.class, () -> parser.parse(name, text));
    }

    static Parser createParser1() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        return new Parser(functions);
    }

    static Parser createParser2() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("printf", null);
        functions.put("contains", null);
        functions.put("len", null);
        functions.put("index", null);
        return new Parser(functions);
    }
}