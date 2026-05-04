package io.github.verils.gotemplate.internal.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AST node classes to improve coverage
 */
class ASTNodeCoverageTest {

    @Test
    void testNodeTypeEnumValues() {
        // Test all enum values exist and have correct codes
        assertEquals(0, NodeType.TEXT.getCode());
        assertEquals(1, NodeType.ACTION.getCode());
        assertEquals(2, NodeType.BOOL.getCode());
        assertEquals(3, NodeType.CHAIN.getCode());
        assertEquals(4, NodeType.COMMAND.getCode());
        assertEquals(5, NodeType.DOT.getCode());
        assertEquals(6, NodeType.ELSE.getCode());
        assertEquals(7, NodeType.END.getCode());
        assertEquals(8, NodeType.FIELD.getCode());
        assertEquals(9, NodeType.IDENTIFIER.getCode());
        assertEquals(10, NodeType.IF.getCode());
        assertEquals(11, NodeType.LIST.getCode());
        assertEquals(12, NodeType.NIL.getCode());
        assertEquals(13, NodeType.NUMBER.getCode());
        assertEquals(14, NodeType.PIPE.getCode());
        assertEquals(15, NodeType.RANGE.getCode());
        assertEquals(16, NodeType.STRING.getCode());
        assertEquals(17, NodeType.TEMPLATE.getCode());
        assertEquals(18, NodeType.VARIABLE.getCode());
        assertEquals(19, NodeType.WITH.getCode());
        assertEquals(20, NodeType.COMMENT.getCode());
    }

    @Test
    void testNodeTypeEnumCount() {
        assertEquals(21, NodeType.values().length);
    }

    @Test
    void testCommentNodeCreation() {
        CommentNode node = new CommentNode("/* this is a comment */");
        assertNotNull(node);
    }

    @Test
    void testCommentNodeToString() {
        String commentText = "/* this is a comment */";
        CommentNode node = new CommentNode(commentText);
        assertEquals(commentText, node.toString());
    }

    @Test
    void testCommentNodeWithEmptyComment() {
        CommentNode node = new CommentNode("");
        assertEquals("", node.toString());
    }

    @Test
    void testCommentNodeWithNullComment() {
        CommentNode node = new CommentNode(null);
        assertNull(node.toString());
    }

    @Test
    void testCommentNodeImplementsNode() {
        CommentNode node = new CommentNode("test");
        assertInstanceOf(Node.class, node);
    }

    @Test
    void testAllNodeTypesAreDistinct() {
        NodeType[] types = NodeType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i], types[j]);
            }
        }
    }
}
