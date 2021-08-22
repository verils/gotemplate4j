package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.Item;
import com.github.verils.gotemplate.lex.ItemType;
import com.github.verils.gotemplate.lex.Lexer;
import com.github.verils.gotemplate.lex.LexerViewer;

import java.util.Map;

/**
 * 文档介绍：<a href="https://pkg.go.dev/text/template#pkg-overview">Template</a>
 */
public class Parser {

    private static final Map<String, Object> DEFAULT_FUNCTIONS = null;

    private final Map<String, Object> functions;

    private final Node root;

    public Parser(String input) {
        this(input, DEFAULT_FUNCTIONS);
    }


    public Parser(String input, Map<String, Object> functions) {
        this.functions = functions;

        ListNode node = new ListNode();
        LexerViewer lexerViewer = new Lexer(input).getViewer();
        parse(node, lexerViewer);
        this.root = node;
    }

    private void parse(ListNode listNode, LexerViewer lexerViewer) {
        while (true) {
            Item item = lexerViewer.getNextItemAndMove();
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
        Item item = lexerViewer.getNextNonSpaceItemAndMove();
        switch (item.type()) {
            case IF:
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

        lexerViewer.getPrevNonSpaceItemAndMove();

        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexerViewer);
        listNode.append(actionNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) {
        WithNode withNode = new WithNode();

        lexerViewer.getNextNonSpaceItemAndMove();
        lexerViewer.getPrevItemAndMove();

        parsePipe(withNode, lexerViewer);
        parseList(withNode, lexerViewer);
//        parseElseList(withNode, lexerViewer);

        listNode.append(withNode);
    }

    private void parsePipe(ActionNode actionNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("command");

        Item item = lexerViewer.getNextItemAndMove();
        switch (item.type()) {
            case RIGHT_DELIM:
                pipeNode.check();
                break;
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
                lexerViewer.getPrevItemAndMove();
                parseCommand(pipeNode, lexerViewer);
                break;
        }

        actionNode.setPipeNode(pipeNode);
    }

    private void parsePipe(WithNode withNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("with");

        Item item = lexerViewer.getNextNonSpaceItemAndMove();
        if (item.type() == ItemType.VARIABLE) {
            Item nextItem = lexerViewer.getNextNonSpaceItemAndMove();
            switch (nextItem.type()) {
                case ASSIGN:
                case DECLARE:
                    pipeNode.append(new VariableNode(item.value()));
                    break;
                case CHAR:
                    break;
            }
        }

        loop:
        while (true) {
            item = lexerViewer.getNextNonSpaceItemAndMove();
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
                    lexerViewer.getPrevItemAndMove();
                    parseCommand(pipeNode, lexerViewer);
                    break;
                case ERROR:
                default:
                    throw new ParseException(String.format("unexpected %s in with", item));
            }
        }

        withNode.setPipeNode(pipeNode);
    }

    private void parseCommand(PipeNode pipeNode, LexerViewer lexerViewer) {
        CommandNode commandNode = new CommandNode();

        loop:
        while (true) {
            Item item = lexerViewer.getNextNonSpaceItemAndMove();
            switch (item.type()) {
                case IDENTIFIER:
                    if (!hasFunction(item.value())) {
                        throw new ParseException(String.format("function %s not defined", item.value()));
                    }
                    IdentifierNode identifierNode = new IdentifierNode(item.value());
                    commandNode.append(identifierNode);
                    break;
                case VARIABLE:
                    VariableNode variableNode = new VariableNode(item.value());
                    commandNode.append(variableNode);
                    break;
                case FIELD:
                    FieldNode fieldNode = new FieldNode(item.value());
                    commandNode.append(fieldNode);
                    break;
                case BOOL:
                    break;
                case CHAR_CONSTANT:
                    break;
                case COMPLEX:
                    break;
                case NUMBER:
                    lexerViewer.getPrevItemAndMove();
                    parseNumber(commandNode, lexerViewer);
                    break;
            }

            item = lexerViewer.getNextItemAndMove();
            switch (item.type()) {
                case SPACE:
                    continue loop;
                case RIGHT_DELIM:
                case RIGHT_PAREN:
                    lexerViewer.getPrevItemAndMove();
                    break;
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

    private void parseNumber(CommandNode commandNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.getNextItemAndMove();
        String value = item.value();

        NumberNode numberNode = new NumberNode(value);

        if (value.endsWith("i")) {

        }

        int i = Integer.parseInt(value);

        commandNode.append(numberNode);
    }

    private boolean hasFunction(String name) {
        return functions.containsKey(name);
    }


    public Node getRoot() {

        return root;
    }
}
