package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.Item;
import com.github.verils.gotemplate.lex.ItemType;
import com.github.verils.gotemplate.lex.Lexer;
import com.github.verils.gotemplate.lex.LexerViewer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文档介绍：<a href="https://pkg.go.dev/text/template#pkg-overview">Template</a>
 */
public class Parser {

    private static final Map<String, Object> DEFAULT_FUNCTIONS = new LinkedHashMap<>();

    private final Set<String> variables = new HashSet<>();
    private final Map<String, Object> functions;

    private final Node root;

    public Parser(String input) {
        this(input, DEFAULT_FUNCTIONS);
    }


    public Parser(String input, Map<String, Object> functions) {
        this.variables.add("$");
        this.functions = functions;

        ListNode listNode = new ListNode();
        LexerViewer lexerViewer = new Lexer(input).getViewer();
        parse(listNode, lexerViewer);
        this.root = listNode;
    }

    private void parse(ListNode listNode, LexerViewer lexerViewer) {
        while (true) {
            Item item = lexerViewer.nextItem();
            switch (item.type()) {
                case EOF:
                    return;
                case TEXT:
                    TextNode textNode = new TextNode(item.value());
                    listNode.append(textNode);
                    return;
                case LEFT_DELIM:
                    parseAction(listNode, lexerViewer);
                    return;
            }
        }
    }

    private void parseAction(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextItem();
        switch (item.type()) {
            case IF:
                return;
            case ELSE:
                return;
            case END:
                return;
            case RANGE:
                return;
            case TEMPLATE:
                return;
            case BLOCK:
                return;
            case WITH:
                parseWith(listNode, lexerViewer);
                return;
        }

        lexerViewer.prevNonSpaceItem();

        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexerViewer);
        listNode.append(actionNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) {
        WithNode withNode = new WithNode();

        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        parsePipe(withNode, lexerViewer);
        parseList(withNode, lexerViewer);
//        parseElseList(withNode, lexerViewer);

        listNode.append(withNode);
    }

    private void parsePipe(ActionNode actionNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("command");
        parsePipe(pipeNode, lexerViewer);
        actionNode.setPipeNode(pipeNode);
    }

    private void parsePipe(WithNode withNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("with");
        parsePipe(pipeNode, lexerViewer);
        withNode.setPipeNode(pipeNode);
    }

    private void parsePipe(PipeNode pipeNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.lookNextNonSpaceItem();
        if (item.type() == ItemType.VARIABLE) {
            lexerViewer.nextNonSpaceItem();
            Item nextItem = lexerViewer.lookNextNonSpaceItem();
            switch (nextItem.type()) {
                case ASSIGN:
                case DECLARE:
                    lexerViewer.nextNonSpaceItem();
                    pipeNode.append(new VariableNode(item.value()));
                    variables.add(item.value());
                    break;
                case CHAR:
                    break;
                default:
                    lexerViewer.prevNonSpaceItem();
                    break;
            }
        }

        loop:
        while (true) {
            item = lexerViewer.nextNonSpaceItem();
            switch (item.type()) {
                case RIGHT_DELIM:
                    break loop;
                case BOOL:
                case CHAR_CONSTANT:
                case COMPLEX:
                case DOT:
                case FIELD:
                case IDENTIFIER:
                case NUMBER:
                case NIL:
                case RAW_STRING:
                case STRING:
                case VARIABLE:
                case LEFT_PAREN:
                    lexerViewer.prevNonSpaceItem();
                    parseCommand(pipeNode, lexerViewer);
                    break;
                case ERROR:
                default:
                    throw new ParseException(String.format("unexpected %s in with", item));
            }
        }
    }

    private void parseCommand(PipeNode pipeNode, LexerViewer lexerViewer) {
        CommandNode commandNode = new CommandNode();

        loop:
        while (true) {
            Node node = null;

            Item item = lexerViewer.nextNonSpaceItem();
            switch (item.type()) {
                case IDENTIFIER:
                    if (!hasFunction(item.value())) {
                        throw new ParseException(String.format("function %s not defined", item.value()));
                    }
                    node = new IdentifierNode(item.value());
                    break;
                case VARIABLE:
                    node = findVariable(item.value());
                    break;
                case FIELD:
                    node = new FieldNode(item.value());
                    break;
                case BOOL:
                    break;
                case CHAR_CONSTANT:
                case COMPLEX:
                case NUMBER:
                    lexerViewer.prevItem();
                    Item item1 = lexerViewer.nextItem();
                    String value = item1.value();

                    NumberNode numberNode = new NumberNode(value);

                    if (value.endsWith("i")) {

                    }

                    int i = Integer.parseInt(value);

                    node = numberNode;
                    break;
            }

            if (node != null) {
                item = lexerViewer.lookNextItem();
                if (item.type() == ItemType.FIELD) {
                    ChainNode chainNode = new ChainNode(node);
                    for (item = lexerViewer.nextItem(); item.type() == ItemType.FIELD; item = lexerViewer.nextItem()) {
                        chainNode.append(item.value());
                    }
                    lexerViewer.prevItem();

                    if (node instanceof FieldNode) {
                        node = new FieldNode(chainNode.toString());
                    } else if (node instanceof VariableNode) {
                        node = new VariableNode(chainNode.toString());
                    } else {
                        node = chainNode;
                    }
                }
            }

            if (node != null) {
                commandNode.append(node);
            }

            item = lexerViewer.nextItem();
            switch (item.type()) {
                case SPACE:
                    continue loop;
                case RIGHT_DELIM:
                case RIGHT_PAREN:
                    lexerViewer.prevItem();
                    break;
                case PIPE:
                    break;
                default:
                    throw new ParseException(String.format("unexpected %s in operand", item));
            }

            break;
        }

        pipeNode.append(commandNode);
    }

    private void parseList(WithNode withNode, LexerViewer lexerViewer) {
        ListNode listNode = new ListNode();
        parse(listNode, lexerViewer);
        withNode.setIfListNode(listNode);
    }

    private Node findVariable(String value) {
        VariableNode variableNode = new VariableNode(value);
        String name = variableNode.getIdentifier(0);
        if (variables.contains(name)) {
            return variableNode;
        }
        throw new ParseException(String.format("undefined variable %s", name));
    }

    private boolean hasFunction(String name) {
        return functions.containsKey(name);
    }


    public Node getRoot() {

        return root;
    }
}
