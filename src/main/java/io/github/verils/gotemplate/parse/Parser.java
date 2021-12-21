package io.github.verils.gotemplate.parse;

import io.github.verils.gotemplate.java.Complex;
import io.github.verils.gotemplate.lex.*;

import java.util.*;

/**
 * Document of go template：<a href="https://pkg.go.dev/text/template#pkg-overview">Template</a>
 */
public class Parser {

    public static final Map<String, Function> DEFAULT_FUNCTIONS = new LinkedHashMap<>();

    static {
        DEFAULT_FUNCTIONS.put("println", args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            stringBuilder.append("\n");
            return stringBuilder.toString();
        });
    }


    private final Map<String, Function> functions;

    private final Map<String, ListNode> nodeMap = new HashMap<>();

    /**
     * A list which contains all the variables in a branch context
     */
    private final List<String> variables = new ArrayList<>();

    public Parser() {
        this(Collections.emptyMap());
    }

    public Parser(Map<String, Function> functions) {
        this.variables.add("$");

        LinkedHashMap<String, Function> map = new LinkedHashMap<>(DEFAULT_FUNCTIONS);
        map.putAll(functions);
        this.functions = map;
    }


    public void parse(String name, String template) {
        // Parse the template text, build a list node as the root node
        ListNode listNode = new ListNode();
        LexerViewer lexerViewer = new Lexer(template).getViewer();


        parseList(listNode, lexerViewer);

        // Can not have ELSE and END node as the last in root list node
        Node lastNode = listNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError("unexpected " + listNode);
        }
        if (lastNode instanceof EndNode) {
            throwUnexpectError("unexpected " + listNode);
        }

        ListNode aListNode = nodeMap.get(name);
        if (aListNode != null) {
            for (Node node : aListNode) {
                listNode.append(node);
            }
        } else {
            nodeMap.put(name, listNode);
        }
    }


    /**
     * Parse list node. Must check the last node in the list when this method return
     *
     * @param listNode    List node which contains all nodes in this context
     * @param lexerViewer Lex container
     */
    private void parseList(ListNode listNode, LexerViewer lexerViewer) {
        loop:
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
                    item = lexerViewer.nextNonSpaceItem();
                    if (item.type() == ItemType.DEFINE) {
                        parseDefinition(lexerViewer);
                        continue;
                    }
                    lexerViewer.prevItem();

                    parseAction(listNode, lexerViewer);

                    // Stop parsing for list in current context, keep the last node, let the method caller handles it
                    Node lastNode = listNode.getLast();
                    if (lastNode instanceof ElseNode) {
                        break loop;
                    }
                    if (lastNode instanceof EndNode) {
                        break loop;
                    }

                    break;
                default:
                    throwUnexpectError(String.format("unexpected %s in input", item));
            }
        }
    }


    private void parseAction(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();
        switch (item.type()) {
            case BLOCK:
                parseBlock(listNode, lexerViewer);
                break;
            case ELSE:
                parseElse(listNode, lexerViewer);
                break;
            case END:
                parseEnd(listNode, lexerViewer);
                break;
            case IF:
                parseIf(listNode, lexerViewer);
                break;
            case RANGE:
                parseRange(listNode, lexerViewer);
                break;
            case TEMPLATE:
                parseTemplate(listNode, lexerViewer);
                break;
            case WITH:
                parseWith(listNode, lexerViewer);
                break;
            default:
                lexerViewer.prevItem();

                // Just action
                ActionNode actionNode = new ActionNode();

                PipeNode pipeNode = new PipeNode("command");
                parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
                actionNode.setPipeNode(pipeNode);

                listNode.append(actionNode);
        }
    }


    private void parseBlock(ListNode listNode, LexerViewer lexerViewer) {
        String context = "block clause";

        Item item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.STRING && item.type() != ItemType.RAW_STRING) {
            throw new ParseException(String.format("unexpected '%s' in %s", item.value(), context));
        }

        String blockTemplateName = StringUtils.unquote(item.value());
        TemplateNode blockTemplateNode = new TemplateNode(blockTemplateName);

        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
        blockTemplateNode.setPipeNode(pipeNode);


        // Parse block content as an associate template
        ListNode blockListNode = new ListNode();
        parseList(blockListNode, lexerViewer);

        Node lastNode = blockListNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError(String.format("unexpected '%s' in block clause", lastNode));
        }
        if (lastNode instanceof EndNode) {
            blockListNode.removeLast();
        }

        nodeMap.put(blockTemplateName, blockListNode);


        listNode.append(blockTemplateNode);
    }


    private void parseDefinition(LexerViewer lexerViewer) {
        String context = "define clause";

        Item item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.STRING && item.type() != ItemType.RAW_STRING) {
            throw new ParseException(String.format("unexpected '%s' in %s", item.value(), context));
        }

        String definitionTemplateName = StringUtils.unquote(item.value());

        item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.RIGHT_DELIM) {
            throw new ParseException(String.format("unexpected '%s' in %s", item.value(), context));
        }

        ListNode definitionListNode = new ListNode();
        parseList(definitionListNode, lexerViewer);

        Node lastNode = definitionListNode.getLast();
        if (lastNode instanceof EndNode) {
            definitionListNode.removeLast();
        } else {
            throwUnexpectError(String.format("unexpected '%s' in %s", lastNode, context));
            return;
        }

        nodeMap.put(definitionTemplateName, definitionListNode);
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
                throwUnexpectError(String.format("unexpected %s in end", item));
        }
    }

    private void parseEnd(ListNode listNode, LexerViewer lexerViewer) {
        Item item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.RIGHT_DELIM) {
            throwUnexpectError(String.format("unexpected %s in end", item));
        }
        listNode.append(new EndNode());
    }

    private void parseIf(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        IfNode ifNode = new IfNode();
        parseBranch(ifNode, lexerViewer, "if", true);
        listNode.append(ifNode);
    }

    private void parseRange(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        RangeNode rangeNode = new RangeNode();
        parseBranch(rangeNode, lexerViewer, "range", true);
        listNode.append(rangeNode);
    }

    private void parseTemplate(ListNode listNode, LexerViewer lexerViewer) {
        String context = "template clause";

        Item item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.STRING && item.type() != ItemType.RAW_STRING) {
            throw new ParseException(String.format("unexpected '%s' in %s", item.value(), context));
        }

        String templateName = StringUtils.unquote(item.value());
        TemplateNode templateNode = new TemplateNode(templateName);

        item = lexerViewer.nextNonSpaceItem();
        if (item.type() != ItemType.RIGHT_DELIM) {
            lexerViewer.prevItem();

            PipeNode pipeNode = new PipeNode(context);
            parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
            templateNode.setPipeNode(pipeNode);
        }

        listNode.append(templateNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        WithNode withNode = new WithNode();
        parseBranch(withNode, lexerViewer, "with", false);
        listNode.append(withNode);
    }

    private void parseBranch(BranchNode branchNode, LexerViewer lexerViewer, String context, boolean allowElseIf) {
        int variableCount = variables.size();

        // Parse pipeline, the executable part
        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexerViewer, ItemType.RIGHT_DELIM);
        branchNode.setPipeNode(pipeNode);

        // Parse 'if' clause
        ListNode ifListNode = new ListNode();
        parseList(ifListNode, lexerViewer);
        branchNode.setIfListNode(ifListNode);

        // Parse if 'else' clause exists
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

            ListNode elseListNode = new ListNode();
            parseList(elseListNode, lexerViewer);
            branchNode.setElseListNode(elseListNode);

            listNode = branchNode.getElseListNode();
        }

        // Check if the branch is closed accurately
        lastNode = listNode.getLast();
        if (lastNode instanceof EndNode) {
            listNode.removeLast();
        } else {
            throwUnexpectError("expected end, found " + lastNode);
        }

        variables.subList(variableCount, variables.size()).clear();
    }

    private void parsePipe(PipeNode pipeNode, LexerViewer lexerViewer, ItemType end) {
        Item item = lexerViewer.lookNextNonSpaceItem();

        if (item.type() == ItemType.VARIABLE) {
            parseVariable(pipeNode, lexerViewer, item);
        }

        while (true) {
            item = lexerViewer.nextNonSpaceItem();
            if (item.type() == end) {
                List<CommandNode> commands = pipeNode.getCommands();
                if (commands.isEmpty()) {
                    throwUnexpectError("missing value for " + pipeNode.getContext());
                }
                for (int i = 1; i < commands.size(); i++) {
                    Node firstArgument = commands.get(i).getFirstArgument();
                    if (firstArgument instanceof BoolNode) {
                        throwUnexpectError(String.format("non executable command in pipeline stage %d", i + 1));
                    } else if (firstArgument instanceof DotNode) {
                        throwUnexpectError(String.format("non executable command in pipeline stage %d", i + 1));
                    } else if (firstArgument instanceof NilNode) {
                        throwUnexpectError(String.format("non executable command in pipeline stage %d", i + 1));
                    } else if (firstArgument instanceof NumberNode) {
                        throwUnexpectError(String.format("non executable command in pipeline stage %d", i + 1));
                    } else if (firstArgument instanceof StringNode) {
                        throwUnexpectError(String.format("non executable command in pipeline stage %d", i + 1));
                    }
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
                    lexerViewer.prevItem();
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
                                throwUnexpectError("");
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
                        throwUnexpectError(String.format("function %s not defined", item.value()));
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
                    node = parseNumber(lexerViewer);
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
                    } else if (node instanceof BoolNode) {
                        throw new ParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof StringNode) {
                        throw new ParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NumberNode) {
                        throw new ParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NilNode) {
                        throw new ParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof DotNode) {
                        throw new ParseException(String.format("unexpected . after term %s", node));
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
                    throwUnexpectError(String.format("unexpected %s in operand", item));
            }

            break;
        }

        if (commandNode.getArgumentCount() == 0) {
            throwUnexpectError("empty command");
        }

        pipeNode.append(commandNode);
    }

    private Node parseNumber(LexerViewer lexerViewer) {
        Item nextItem = lexerViewer.nextItem();
        String value = nextItem.value();
        int length = value.length();

        if (nextItem.type() == ItemType.CHAR_CONSTANT) {
            if (value.charAt(0) != '\'') {
                throw new ParseException(String.format("malformed character constant: %s", value));
            }
            int ch = Char.unquotedChar(value);
            return new NumberNode(value, ch);
        }

        if (length > 0 && value.charAt(length - 1) == 'i') {
            try {
                double image = Double.parseDouble(value.substring(0, length - 1));
                Complex complex = new Complex(0, image);
                return new NumberNode(value, complex);
            } catch (NumberFormatException ignored) {
            }
        }


        Node node;
        Number number = null;
        try {
            number = Long.valueOf(value);
        } catch (NumberFormatException ignored) {
        }

        if (number == null) {
            try {
                number = Double.valueOf(value);
            } catch (NumberFormatException ignored) {
            }
        }

        if (number == null) {
            try {
                number = Double.valueOf(value);
            } catch (NumberFormatException ignored) {
            }
        }

        if (number == null) {
            throw new ParseException(String.format("illegal number syntax: %s", value));
        }

        NumberNode numberNode = new NumberNode(value, number);

//                    if (value.endsWith("i")) {
//                        String numberString = value.substring(0, value.length() - 1);
//                        double number = Double.parseDouble(numberString);
//                    } else {
//                        numberNode
//                    }
//
//                    int i = Integer.parseInt(value);

        node = numberNode;
        return node;
    }

    private Node findVariable(String value) {
        VariableNode variableNode = new VariableNode(value);
        String name = variableNode.getIdentifier(0);
        if (variables.contains(name)) {
            return variableNode;
        }
        throwUnexpectError(String.format("undefined variable %s", name));
        return null;
    }

    private boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    private void throwUnexpectError(String message) throws ParseException {
        throw new ParseException(message);
    }


    public Map<String, ListNode> getNodeMap() {
        return nodeMap;
    }

    public Node getNode(String name) {
        return nodeMap.get(name);
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }
}
