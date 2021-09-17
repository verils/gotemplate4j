package com.github.verils.gotemplate.parse;

import com.github.verils.gotemplate.lex.*;

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
                    break;
                case COMMENT:
                    CommentNode commentNode = new CommentNode(item.value());
                    listNode.append(commentNode);
                    break;
                case LEFT_DELIM:
                    parseAction(listNode, lexerViewer);
                    break;
                default:
                    throw new ParseException(String.format("unexpected '%s' in input: '%s'", item, lexerViewer.getInput()));
            }

            Node lastNode = listNode.getLast();
            if (lastNode instanceof ElseNode) {
                throw new ParseException("unexpected " + listNode);
            }
            if (lastNode instanceof EndNode) {
                throw new ParseException("unexpected " + listNode);
            }
        }
    }

    private void parseAction(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();
        switch (item.type()) {
            case IF:
                parseIf(listNode, lexerViewer);
                break;
            case RANGE:
                parseRange(listNode, lexerViewer);
                break;
            case TEMPLATE:
                parseTemplate(listNode, lexerViewer);
                break;
            case BLOCK:
                break;
            case WITH:
                parseWith(listNode, lexerViewer);
                break;
            case ELSE:
                parseElse(listNode, lexerViewer);
                break;
            case END:
                parseEnd(listNode, lexerViewer);
                break;
            default:
                lexerViewer.prevItem();
                parsePipe(listNode, lexerViewer);
        }
    }

    private void parseIf(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        IfNode ifNode = new IfNode();

        BranchNode branchNode = new BranchNode();
        parseBranch(branchNode, lexerViewer, "if", true);

        ifNode.setBranch(branchNode);
        listNode.append(ifNode);
    }

    private void parseRange(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        RangeNode rangeNode = new RangeNode();

        BranchNode branchNode = new BranchNode();
        parseBranch(branchNode, lexerViewer, "range", true);

        rangeNode.setBranch(branchNode);
        listNode.append(rangeNode);
    }

    private void parseTemplate(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();

        String templateName = StringUtils.unquote(item.value());
        TemplateNode templateNode = new TemplateNode(templateName);

        item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.RIGHT_DELIM) {
            lexerViewer.prevItem();
            parsePipe(templateNode, lexerViewer);
        }

        listNode.append(templateNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        WithNode withNode = new WithNode();

        BranchNode branchNode = new BranchNode();
        parseBranch(branchNode, lexerViewer, "with", false);

        withNode.setBranch(branchNode);
        listNode.append(withNode);
    }

    private void parseElse(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();
        switch (item.type()) {
            case IF:
                lexerViewer.prevItem();
                listNode.append(new ElseNode());
                break;
            case RIGHT_DELIM:
                listNode.append(new ElseNode());
                break;
            default:
                throw new ParseException(String.format("unexpected %s in end", item));
        }
    }

    private void parseEnd(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.RIGHT_DELIM) {
            throw new ParseException(String.format("unexpected %s in end", item));
        }
        listNode.append(new EndNode());
    }

    private void parseBranch(BranchNode branchNode, LexerViewer lexerViewer, String context, boolean allowElseIf) {
        parsePipe(branchNode, lexerViewer, context);
        parseIfList(branchNode, lexerViewer);

        ListNode listNode = branchNode.getIfListNode();
        Node lastNode = listNode.getLast();

        if (lastNode instanceof ElseNode) {
            listNode.removeLast();

            if (allowElseIf) {
                Item item = lexerViewer.lookNextNonSpaceItem();
                if (item.type() == ItemType.IF) {
                    lexerViewer.nextNonSpaceItem();

                    ListNode elseListNode = new ListNode();
                    parseIf(elseListNode, lexerViewer);
                    branchNode.setElseListNode(elseListNode);
                    return;
                }
            }

            parseElseList(branchNode, lexerViewer);

            listNode = branchNode.getElseListNode();
        }

        lastNode = listNode.getLast();
        if (lastNode instanceof EndNode) {
            listNode.removeLast();
        } else {
            throw new ParseException("expected end, found " + lastNode);
        }
    }

    private void parsePipe(ListNode listNode, LexerViewer lexerViewer) {
        ActionNode actionNode = new ActionNode();
        parsePipe(actionNode, lexerViewer);
        listNode.append(actionNode);
    }

    private void parsePipe(ActionNode actionNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("command");
        parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
        actionNode.setPipeNode(pipeNode);
    }

    private void parsePipe(BranchNode branchNode, LexerViewer lexerViewer, String context) {
        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
        branchNode.setPipeNode(pipeNode);
    }

    private void parsePipe(TemplateNode templateNode, LexerViewer lexerViewer) {
        PipeNode pipeNode = new PipeNode("template clause");
        parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
        templateNode.setPipeNode(pipeNode);
    }

    private void parsePipe(PipeNode pipeNode, LexerViewer lexerViewer, ItemType end) {
        Item item = lexerViewer.lookNextNonSpaceItem();

        if (item.type() == ItemType.VARIABLE) {
            parseVariable(pipeNode, lexerViewer, item);
        }

        while (true) {
            item = lexerViewer.nextNonSpaceItem();
            if (item.type() == end) {
                if (pipeNode.getCommands().isEmpty()) {
                    throw new ParseException("missing value for " + pipeNode.getContext());
                }
                break;
            }
            switch (item.type()) {
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
                    throw new ParseException(String.format("unexpected %s in %s", item, pipeNode.getContext()));
            }
        }
    }

    private void parseVariable(PipeNode pipeNode, LexerViewer lexerViewer, Item variableItem) {
        lexerViewer.nextNonSpaceItem();
        Item nextItem = lexerViewer.lookNextNonSpaceItem();
        switch (nextItem.type()) {
            case ASSIGN:
            case DECLARE:
                lexerViewer.nextNonSpaceItem();
                pipeNode.append(new VariableNode(variableItem.value()));
                variables.add(variableItem.value());
                break;
            case CHAR:
                if (",".equals(nextItem.value())) {
                    lexerViewer.nextNonSpaceItem();
                    pipeNode.append(new VariableNode(variableItem.value()));
                    variables.add(variableItem.value());
                    if ("range".equals(pipeNode.getContext()) && pipeNode.getVariableCount() < 2) {
                        nextItem = lexerViewer.lookNextNonSpaceItem();
                        switch (nextItem.type()) {
                            case VARIABLE:
                            case RIGHT_DELIM:
                            case RIGHT_PAREN:
                                if (variableItem.type() == ItemType.VARIABLE) {
                                    parseVariable(pipeNode, lexerViewer, nextItem);
                                }
                                break;
                            default:
                                throw new ParseException("");
                        }
                    }
                }
                break;
            default:
                lexerViewer.prevNonSpaceItem();
                break;
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
                case DOT:
                    node = new DotNode();
                    break;
                case NIL:
                    node = new NilNode();
                    break;
                case VARIABLE:
                    node = findVariable(item.value());
                    break;
                case FIELD:
                    node = new FieldNode(item.value());
                    break;
                case BOOL:
                    node = new BoolNode(item.value());
                    break;
                case CHAR_CONSTANT:
                case COMPLEX:
                case NUMBER:
                    lexerViewer.prevItem();
                    Item nextItem = lexerViewer.nextItem();
                    String value = nextItem.value();

                    NumberNode numberNode = new NumberNode(value);

//                    if (value.endsWith("i")) {
//                        String numberString = value.substring(0, value.length() - 1);
//                        double number = Double.parseDouble(numberString);
//                    } else {
//                        numberNode
//                    }
//
//                    int i = Integer.parseInt(value);

                    node = numberNode;
                    break;
                case STRING:
                case RAW_STRING:
                    node = new StringNode(item.value());
                    break;
                case LEFT_PAREN:
                    PipeNode nestedPipeNode = new PipeNode("parenthesized pipeline");
                    parsePipe(nestedPipeNode, lexerViewer, ItemType.RIGHT_PAREN);
                    node = nestedPipeNode;
                    break;
                default:
                    lexerViewer.prevItem();
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

    private void parseIfList(BranchNode branchNode, LexerViewer lexerViewer) {
        ListNode listNode = new ListNode();
        parseList(listNode, lexerViewer);
        branchNode.setIfListNode(listNode);
    }

    private void parseElseList(BranchNode branchNode, LexerViewer lexerViewer) {
        ListNode listNode = new ListNode();
        parseList(listNode, lexerViewer);
        branchNode.setElseListNode(listNode);
    }

    private void parseList(ListNode listNode, LexerViewer lexerViewer) {
        while (true) {
            Item item = lexerViewer.nextItem();
            switch (item.type()) {
                case EOF:
                    return;
                case TEXT:
                    TextNode textNode = new TextNode(item.value());
                    listNode.append(textNode);
                    break;
                case COMMENT:
                    CommentNode commentNode = new CommentNode(item.value());
                    listNode.append(commentNode);
                    break;
                case LEFT_DELIM:
                    parseAction(listNode, lexerViewer);
                    break;
                default:
                    throw new ParseException(String.format("unexpected %s in input", item));
            }

            Node lastNode = listNode.getLast();
            if (lastNode instanceof ElseNode) {
                break;
            }
            if (lastNode instanceof EndNode) {
                break;
            }
        }
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
