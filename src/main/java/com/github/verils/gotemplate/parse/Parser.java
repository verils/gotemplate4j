package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.Item;
import com.github.verils.gotemplate.lex.Lexer;
import com.github.verils.gotemplate.lex.LexerViewer;

import java.util.Map;

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
        lexerViewer.getPrevNonSpaceItemAndMove();

        switch (item.type()) {
            case WITH:
                parseWith(listNode, lexerViewer);
                return;
            default:

        }


        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexerViewer);
        listNode.append(actionNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) {
        WithNode withNode = new WithNode();

        parseBranch(withNode, lexerViewer);

        listNode.append(withNode);
    }

    private void parseBranch(WithNode withNode, LexerViewer lexer) {
        parsePipe(withNode, lexer);
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

        Item item = lexerViewer.getNextNonSpaceItem();
        lexerViewer.getPrevNonSpaceItemAndMove();

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

        withNode.setPipeNode(pipeNode);
    }

    private void parseCommand(PipeNode pipeNode, LexerViewer lexerViewer) {
        CommandNode commandNode = new CommandNode();

        Item item = lexerViewer.getNextItemAndMove();
        switch (item.type()) {
            case IDENTIFIER:
                if (!hasFunction(item.value())) {
                    throw new TemplateParseException(String.format("function %s not defined", item.value()));
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
        }

        pipeNode.append(commandNode);
    }

    private boolean hasFunction(String name) {
        return functions.containsKey(name);
    }


    public Node getRoot() {

        return root;
    }
}
