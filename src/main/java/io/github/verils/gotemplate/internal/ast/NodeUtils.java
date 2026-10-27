package io.github.verils.gotemplate.internal.ast;

/**
 * Shared helpers for inspecting parsed template nodes.
 */
public final class NodeUtils {

    private NodeUtils() {
    }

    /**
     * Reports whether a node contains only whitespace and comments.
     *
     * @param node node to inspect, or {@code null}
     * @return {@code true} if the node has no executable or visible content
     */
    public static boolean isEmpty(Node node) {
        if (node == null) {
            return true;
        }
        if (node instanceof ListNode) {
            for (Node child : (ListNode) node) {
                if (!isEmpty(child)) {
                    return false;
                }
            }
            return true;
        }
        if (node instanceof TextNode) {
            return ((TextNode) node).getText().isBlank();
        }
        return node instanceof CommentNode;
    }
}
