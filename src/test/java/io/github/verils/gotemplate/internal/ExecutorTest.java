package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Functions;
import io.github.verils.gotemplate.TemplateExecutionException;
import io.github.verils.gotemplate.TemplateNotFoundException;
import io.github.verils.gotemplate.internal.ast.CommentNode;
import io.github.verils.gotemplate.internal.ast.ListNode;
import io.github.verils.gotemplate.internal.ast.Node;
import io.github.verils.gotemplate.internal.ast.TextNode;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutorTest {

    @Test
    void executeWritesManuallyConstructedTextList() throws Exception {
        ListNode root = new ListNode();
        root.append(new TextNode("hello"));
        Executor executor = new Executor(rootNodes("root", root), Functions.BUILTIN);

        StringWriter writer = new StringWriter();
        executor.execute("root", null, writer);

        assertEquals("hello", writer.toString());
    }

    @Test
    void executeThrowsWhenRootTemplateIsMissing() {
        Executor executor = new Executor(Collections.emptyMap(), Functions.BUILTIN);

        assertThrows(TemplateNotFoundException.class,
                () -> executor.execute("missing", null, new StringWriter()));
    }

    @Test
    void writeNodeWritesTextAndIgnoresCommentsDirectly() throws Exception {
        Executor executor = new Executor(Collections.emptyMap(), Functions.BUILTIN);
        StringWriter writer = new StringWriter();

        executor.writeNode(writer, new TextNode("text"), null, null, new HashMap<>());
        executor.writeNode(writer, new CommentNode("ignored"), null, null, new HashMap<>());

        assertEquals("text", writer.toString());
    }

    @Test
    void writeNodeRejectsUnknownNodeDirectly() {
        Executor executor = new Executor(Collections.emptyMap(), Functions.BUILTIN);
        Node unknown = new Node() {
            @Override
            public String toString() {
                return "unknown";
            }
        };

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> executor.writeNode(new StringWriter(), unknown, null, null, new HashMap<>()));

        assertEquals("unknown node: unknown", exception.getMessage());
    }

    private Map<String, Node> rootNodes(String name, Node node) {
        Map<String, Node> nodes = new HashMap<>();
        nodes.put(name, node);
        return nodes;
    }
}
