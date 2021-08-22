package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.Item;
import com.github.verils.gotemplate.lex.Lexer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser {

    private static final Map<String, Object> DEFAULT_FUNCTIONS = null;

    private final String input;
    private final Map<String, Object> functions;

    private final Node root;

    public Parser(String input) {
        this(input, DEFAULT_FUNCTIONS);
    }

    public Parser(String input, Map<String, Object> functions) {
        this.input = input;
        this.functions = functions;

        this.root = parse();
    }

    private Node parse() {
        Lexer lexer = new Lexer(input);
        List<Item> buffer = new LinkedList<>();

        ListNode node = new ListNode();
        parse(node, lexer);

        return node;
    }

    private void parse(ListNode listNode, Lexer lexer) {
        while (true) {
            Item item = lexer.nextItem();
            switch (item.type()) {
                case EOF:
                    return;
                case TEXT:
                    TextNode textNode = new TextNode(item.value());
                    listNode.append(textNode);
                    return;
                case LEFT_DELIM:
                    parseAction(listNode, lexer);
                    return;
            }
        }
    }

    private void parseAction(ListNode listNode, Lexer lexer) {
        Item item = lexer.nextNonSpaceItem();
        lexer.prevNonSpaceItem();

        switch (item.type()) {
            case WITH:
                parseWith(listNode, lexer);
                return;
        }


        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexer);
        listNode.append(actionNode);
    }

    private void parseWith(ListNode listNode, Lexer lexer) {
        WithNode withNode = new WithNode();

        parseBranch(withNode, lexer);

        listNode.append(withNode);
    }

    private void parseBranch(WithNode withNode, Lexer lexer) {
        parsePipe(withNode, lexer);
    }

    private void parsePipe(ActionNode actionNode, Lexer lexer) {
        PipeNode pipeNode = new PipeNode("command");

        Item item = lexer.nextItem();
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
                lexer.prevItem();
                parseCommand(pipeNode, lexer);
                break;
        }

        actionNode.setPipeNode(pipeNode);
    }

    private void parsePipe(WithNode withNode, Lexer lexer) {
        PipeNode pipeNode = new PipeNode("with");

        Item item = lexer.peekNonSpaceItem();
        lexer.prevNonSpaceItem();

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
                lexer.prevItem();
                parseCommand(pipeNode, lexer);
                break;
        }

        withNode.setPipeNode(pipeNode);
    }

    private void parseCommand(PipeNode pipeNode, Lexer lexer) {
        CommandNode commandNode = new CommandNode();

        Item item = lexer.nextItem();
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
