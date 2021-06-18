package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.Item;
import com.github.verils.gotemplate.lex.Lexer;

import java.util.LinkedList;
import java.util.List;

public class Parser {

    public Node parse(String template) {
        Lexer lexer = new Lexer(template);
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
        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexer);
        listNode.append(actionNode);
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

    private void parseCommand(PipeNode pipeNode, Lexer lexer) {
        CommandNode commandNode = new CommandNode();

        Item item = lexer.nextItem();
        switch (item.type()) {
            case FIELD:
                FieldNode fieldNode = new FieldNode(item.value());
                commandNode.append(fieldNode);
        }


        pipeNode.append(commandNode);
    }

    private void pushbackItem() {

    }

}
