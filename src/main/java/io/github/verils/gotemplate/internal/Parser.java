package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateParseException;
import io.github.verils.gotemplate.internal.ast.*;
import io.github.verils.gotemplate.internal.lang.CharUtils;
import io.github.verils.gotemplate.internal.lang.Complex;
import io.github.verils.gotemplate.internal.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Document of go template：<a href="https://pkg.go.dev/text/template#pkg-overview">Template</a>
 */
public class Parser {

    private final Map<String, Function> functions;

    private final Map<String, Node> rootNodes;


    // Position marker

    private int tokenIndex;


    /**
     * A list which contains all the variables in a branch context
     */
    private final List<String> variables = new ArrayList<>();


    public Parser(Map<String, Function> functions) {
        this.functions = functions;
        this.rootNodes = new LinkedHashMap<>();
        this.variables.add("$");
    }


    /**
     * Parser a template text
     *
     * @param name the name of template
     * @param text template content
     * @return a map containing all ast root nodes
     * @throws TemplateParseException if reach invalid syntax
     */
    public Map<String, Node> parse(String name, String text) throws TemplateParseException {
        // Parse the template text, build a list node as the root node
        ListNode rootNode = new ListNode();

        Lexer lexer = new Lexer(text);
        parseList(rootNode, lexer);

        // Can not have ELSE and END node as the last in root list node
        Node lastNode = rootNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError("unexpected " + rootNode);
        }
        if (lastNode instanceof EndNode) {
            throwUnexpectError("unexpected " + rootNode);
        }

        ListNode root = (ListNode) rootNodes.get(name);
        if (root == null) {
            rootNodes.put(name, rootNode);
        } else {
            for (Node node : root) {
                rootNode.append(node);
            }
        }

        return rootNodes;
    }


    /**
     * Parse list node. Must check the last node in the list when this method return
     *
     * @param listNode List node which contains all nodes in this context
     * @param lexer    Lexer holding tokens
     */
    private void parseList(ListNode listNode, Lexer lexer) throws TemplateParseException {
        loop:
        while (true) {
            Token token = moveToNextToken(lexer);
            switch (token.type()) {
                case EOF:
                    return;
                case TEXT:
                    TextNode textNode = new TextNode(token.value());
                    listNode.append(textNode);
                    break;
                case COMMENT:
                    CommentNode commentNode = new CommentNode(token.value());
                    listNode.append(commentNode);
                    break;
                case LEFT_DELIM:
                    token = moveToNextNonSpaceToken(lexer);
                    if (token == null) {
                        throwUnexpectError("unclosed delimiter: " + lexer.getLeftDelimiter());
                    }

                    if (token.type() == TokenType.DEFINE) {
                        parseDefinition(lexer);
                        continue;
                    }

                    moveToPrevItem(lexer);

                    parseAction(listNode, lexer);

                    // Stop parsing list in current context, keep the last node, let the method caller handles it
                    Node lastNode = listNode.getLast();
                    if (lastNode instanceof ElseNode) {
                        break loop;
                    }
                    if (lastNode instanceof EndNode) {
                        break loop;
                    }

                    break;
                default:
                    throwUnexpectError(String.format("unexpected %s in input", token));
            }
        }
    }


    private void parseAction(ListNode listNode, Lexer lexer) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing action token");
        }

        switch (token.type()) {
            case BLOCK:
                parseBlock(listNode, lexer);
                break;
            case ELSE:
                parseElse(listNode, lexer);
                break;
            case END:
                parseEnd(listNode, lexer);
                break;
            case IF:
                parseIf(listNode, lexer);
                break;
            case RANGE:
                parseRange(listNode, lexer);
                break;
            case TEMPLATE:
                parseTemplate(listNode, lexer);
                break;
            case WITH:
                parseWith(listNode, lexer);
                break;
            default:
                moveToPrevItem(lexer);

                // Just action
                ActionNode actionNode = new ActionNode();

                PipeNode pipeNode = new PipeNode("command");
                parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM);
                actionNode.setPipeNode(pipeNode);

                listNode.append(actionNode);
        }
    }


    private void parseBlock(ListNode listNode, Lexer lexer) throws TemplateParseException {
        String context = "block clause";

        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String blockTemplateName = StringUtils.unquote(token.value());
        TemplateNode blockTemplateNode = new TemplateNode(blockTemplateName);

        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM);
        blockTemplateNode.setPipeNode(pipeNode);


        // Parse block content as an associate template
        ListNode blockListNode = new ListNode();
        parseList(blockListNode, lexer);

        Node lastNode = blockListNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError(String.format("unexpected '%s' in block clause", lastNode));
        }
        if (lastNode instanceof EndNode) {
            blockListNode.removeLast();
        }

        listNode.append(blockTemplateNode);

        rootNodes.put(blockTemplateName, blockListNode);
    }


    private void parseDefinition(Lexer lexer) throws TemplateParseException {
        String context = "define clause";

        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String definitionTemplateName = StringUtils.unquote(token.value());

        token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        ListNode definitionListNode = new ListNode();
        parseList(definitionListNode, lexer);

        Node lastNode = definitionListNode.getLast();
        if (lastNode instanceof EndNode) {
            definitionListNode.removeLast();
        } else {
            throwUnexpectError(String.format("unexpected '%s' in %s", lastNode, context));
            return;
        }

        rootNodes.put(definitionTemplateName, definitionListNode);
    }


    private void parseElse(ListNode listNode, Lexer lexer) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        switch (token.type()) {
            case IF:
                moveToPrevItem(lexer);
                listNode.append(new ElseNode());
                break;
            case RIGHT_DELIM:
                listNode.append(new ElseNode());
                break;
            default:
                throwUnexpectError(String.format("unexpected %s in end", token));
        }
    }

    private void parseEnd(ListNode listNode, Lexer lexer) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            throwUnexpectError(String.format("unexpected %s in end", token));
        }
        listNode.append(new EndNode());
    }

    private void parseIf(ListNode listNode, Lexer lexer) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer);
        moveToPrevItem(lexer);

        IfNode ifNode = new IfNode();
        parseBranch(ifNode, lexer, "if", true);
        listNode.append(ifNode);
    }

    private void parseRange(ListNode listNode, Lexer lexer) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer);
        moveToPrevItem(lexer);

        RangeNode rangeNode = new RangeNode();
        parseBranch(rangeNode, lexer, "range", true);
        listNode.append(rangeNode);
    }

    private void parseTemplate(ListNode listNode, Lexer lexer) throws TemplateParseException {
        String context = "template clause";

        Token token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String templateName = StringUtils.unquote(token.value());
        TemplateNode templateNode = new TemplateNode(templateName);

        token = moveToNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            moveToPrevItem(lexer);

            PipeNode pipeNode = new PipeNode(context);
            parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM);
            templateNode.setPipeNode(pipeNode);
        }

        listNode.append(templateNode);
    }

    private void parseWith(ListNode listNode, Lexer lexer) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer);
        moveToPrevItem(lexer);

        WithNode withNode = new WithNode();
        parseBranch(withNode, lexer, "with", false);
        listNode.append(withNode);
    }

    private void parseBranch(BranchNode branchNode, Lexer lexer, String context, boolean allowElseIf) throws TemplateParseException {
        int variableCount = variables.size();

        // Parse pipeline, the executable part
        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM);
        branchNode.setPipeNode(pipeNode);

        // Parse 'if' clause
        ListNode ifListNode = new ListNode();
        parseList(ifListNode, lexer);
        branchNode.setIfListNode(ifListNode);

        // Parse if 'else' clause exists
        ListNode listNode = branchNode.getIfListNode();
        Node lastNode = listNode.getLast();
        if (lastNode instanceof ElseNode) {
            listNode.removeLast();

            if (allowElseIf) {
                Token token = lookNextNonSpaceToken(lexer);
                if (token == null) {
                    throwUnexpectError("missing token");
                }

                if (token.type() == TokenType.IF) {
                    moveToNextNonSpaceToken(lexer);

                    ListNode elseListNode = new ListNode();
                    parseIf(elseListNode, lexer);
                    branchNode.setElseListNode(elseListNode);

                    return;
                }
            }

            ListNode elseListNode = new ListNode();
            parseList(elseListNode, lexer);
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

    private void parsePipe(PipeNode pipeNode, Lexer lexer, TokenType end) throws TemplateParseException {
        Token token = lookNextNonSpaceToken(lexer);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() == TokenType.VARIABLE) {
            parseVariable(pipeNode, lexer, token);
        }

        while (true) {
            token = moveToNextNonSpaceToken(lexer);
            if (token == null) {
                throwUnexpectError("missing token");
            }

            if (token.type() == end) {
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
            switch (token.type()) {
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
                    moveToPrevItem(lexer);
                    parseCommand(pipeNode, lexer);
                    break;
                case ERROR:
                default:
                    throw new TemplateParseException(String.format("unexpected %s in %s", token, pipeNode.getContext()));
            }
        }
    }

    private void parseVariable(PipeNode pipeNode, Lexer lexer, Token variableToken) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer);
        Token nextToken = lookNextNonSpaceToken(lexer);
        if (nextToken == null) {
            throwUnexpectError("missing token");
        }

        switch (nextToken.type()) {
            case ASSIGN:
            case DECLARE:
                moveToNextNonSpaceToken(lexer);
                pipeNode.append(new VariableNode(variableToken.value()));
                variables.add(variableToken.value());
                break;
            case CHAR:
                if (",".equals(nextToken.value())) {
                    moveToNextNonSpaceToken(lexer);
                    pipeNode.append(new VariableNode(variableToken.value()));
                    variables.add(variableToken.value());
                    if ("range".equals(pipeNode.getContext()) && pipeNode.getVariableCount() < 2) {
                        nextToken = lookNextNonSpaceToken(lexer);
                        if (nextToken == null) {
                            throwUnexpectError("missing token");
                        }

                        switch (nextToken.type()) {
                            case VARIABLE:
                            case RIGHT_DELIM:
                            case RIGHT_PAREN:
                                if (variableToken.type() == TokenType.VARIABLE) {
                                    parseVariable(pipeNode, lexer, nextToken);
                                }
                                break;
                            default:
                                throwUnexpectError("");
                        }
                    }
                }
                break;
            default:
                moveToPrevNonSpaceItem(lexer);
                break;
        }
    }

    private void parseCommand(PipeNode pipeNode, Lexer lexer) throws TemplateParseException {
        CommandNode commandNode = new CommandNode();

        loop:
        while (true) {
            Node node = null;

            Token token = moveToNextNonSpaceToken(lexer);
            if (token == null) {
                throwUnexpectError("missing token");
            }

            switch (token.type()) {
                case IDENTIFIER:
                    String name = token.value();
                    if (!hasFunction(name)) {
                        throwUnexpectError(String.format("function %s not defined", token.value()));
                    }
                    node = new IdentifierNode(token.value());
                    break;
                case DOT:
                    node = new DotNode();
                    break;
                case NIL:
                    node = new NilNode();
                    break;
                case VARIABLE:
                    node = findVariable(token.value());
                    break;
                case FIELD:
                    node = new FieldNode(token.value());
                    break;
                case BOOL:
                    node = new BoolNode(token.value());
                    break;
                case CHAR_CONSTANT:
                case COMPLEX:
                case NUMBER:
                    moveToPrevItem(lexer);
                    node = parseNumber(lexer);
                    break;
                case STRING:
                case RAW_STRING:
                    node = new StringNode(token.value());
                    break;
                case LEFT_PAREN:
                    PipeNode nestedPipeNode = new PipeNode("parenthesized pipeline");
                    parsePipe(nestedPipeNode, lexer, TokenType.RIGHT_PAREN);
                    node = nestedPipeNode;
                    break;
                default:
                    moveToPrevItem(lexer);
            }

            if (node != null) {
                token = lookNextItem(lexer);
                if (token == null) {
                    throwUnexpectError("missing token");
                }

                if (token.type() == TokenType.FIELD) {
                    ChainNode chainNode = new ChainNode(node);
                    for (token = moveToNextToken(lexer); token.type() == TokenType.FIELD; token = moveToNextToken(lexer)) {
                        chainNode.append(token.value());
                    }
                    moveToPrevItem(lexer);

                    if (node instanceof FieldNode) {
                        node = new FieldNode(chainNode.toString());
                    } else if (node instanceof VariableNode) {
                        node = new VariableNode(chainNode.toString());
                    } else if (node instanceof BoolNode) {
                        throw new TemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof StringNode) {
                        throw new TemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NumberNode) {
                        throw new TemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NilNode) {
                        throw new TemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof DotNode) {
                        throw new TemplateParseException(String.format("unexpected . after term %s", node));
                    } else {
                        node = chainNode;
                    }
                }
                commandNode.append(node);
            }

            token = moveToNextToken(lexer);
            switch (token.type()) {
                case SPACE:
                    continue loop;
                case RIGHT_DELIM:
                case RIGHT_PAREN:
                    moveToPrevItem(lexer);
                    break;
                case PIPE:
                    break;
                default:
                    throwUnexpectError(String.format("unexpected %s in operand", token));
            }

            break;
        }

        if (commandNode.getArgumentCount() == 0) {
            throwUnexpectError("empty command");
        }

        pipeNode.append(commandNode);
    }

    private boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * 获取下一个元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    private Token lookNextItem(Lexer lexer) {
        if (tokenIndex < lexer.getTokens().size()) {
            return lexer.getTokens().get(tokenIndex);
        }
        return null;
    }

    private Node parseNumber(Lexer lexer) throws TemplateParseException {
        Token nextToken = moveToNextToken(lexer);
        String value = nextToken.value();
        int length = value.length();

        if (nextToken.type() == TokenType.CHAR_CONSTANT) {
            if (value.charAt(0) != '\'') {
                throw new TemplateParseException(String.format("malformed character constant: %s", value));
            }

            int ch;
            try {
                ch = CharUtils.unquotedChar(value);
            } catch (IllegalArgumentException e) {
                throw new TemplateParseException("invalid syntax: " + value, e);
            }

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
            throw new TemplateParseException(String.format("illegal number syntax: %s", value));
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

    /**
     * 获取上一个元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    private Token moveToPrevItem(Lexer lexer) {
        if (tokenIndex > 0) {
            return lexer.getTokens().get(--tokenIndex);
        }
        return null;
    }

    /**
     * 获取下一个元素，并将查找标记后移一位，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    private Token moveToNextToken(Lexer lexer) {
        Token token = lookNextItem(lexer);
        if (token != null) {
            tokenIndex++;
        }
        return token;
    }

    /**
     * 获取下一个非空白元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    private Token lookNextNonSpaceToken(Lexer lexer) {
        int count = 0;
        while (true) {
            Token token = moveToNextToken(lexer);
            count++;
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                tokenIndex -= count;
                return token;
            }
        }
    }

    /**
     * 获取上一个非空白元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    private Token moveToPrevNonSpaceItem(Lexer lexer) {
        while (true) {
            Token token = moveToPrevItem(lexer);
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                return token;
            }
        }
    }

    /**
     * 获取下一个非空白元素，并将查找标记后移到这个元素后，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    private Token moveToNextNonSpaceToken(Lexer lexer) {
        while (true) {
            Token token = moveToNextToken(lexer);
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                return token;
            }
        }
    }

    private Node findVariable(String value) throws TemplateParseException {
        VariableNode variableNode = new VariableNode(value);
        String name = variableNode.getIdentifier(0);
        if (variables.contains(name)) {
            return variableNode;
        }
        throwUnexpectError(String.format("undefined variable %s", name));
        return null;
    }

    private void throwUnexpectError(String message) throws TemplateParseException {
        throw new TemplateParseException(message);
    }

    public Node getRootNode(String name) {
        return rootNodes.get(name);
    }

}
