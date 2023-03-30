package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Functions;
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


    public Parser() {
        this(Functions.BUILTIN);
    }

    public Parser(Map<String, Function> functions) {
        this.functions = functions;
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

        State state = new State();
        state.variables.add("$");

        parseList(rootNode, lexer, state);

        // Can not have ELSE and END node as the last in root list node
        Node lastNode = rootNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError("unexpected " + rootNode);
        }
        if (lastNode instanceof EndNode) {
            throwUnexpectError("unexpected " + rootNode);
        }

        ListNode root = (ListNode) state.rootNodes.get(name);
        if (root == null) {
            state.rootNodes.put(name, rootNode);
        } else {
            for (Node node : root) {
                rootNode.append(node);
            }
        }

        return state.rootNodes;
    }


    /**
     * Parse list node. Must check the last node in the list when this method return
     *
     * @param listNode List node which contains all nodes in this context
     * @param lexer    Lexer holding tokens
     * @param state
     */
    private void parseList(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        loop:
        while (true) {
            Token token = moveToNextToken(lexer, state);
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
                    token = moveToNextNonSpaceToken(lexer, state);
                    if (token == null) {
                        throwUnexpectError("unclosed delimiter: " + lexer.getLeftDelimiter());
                    }

                    if (token.type() == TokenType.DEFINE) {
                        parseDefinition(lexer, state);
                        continue;
                    }

                    moveToPrevItem(lexer, state);

                    parseAction(listNode, lexer, state);

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


    private void parseAction(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing action token");
        }

        switch (token.type()) {
            case BLOCK:
                parseBlock(listNode, lexer, state);
                break;
            case ELSE:
                parseElse(listNode, lexer, state);
                break;
            case END:
                parseEnd(listNode, lexer, state);
                break;
            case IF:
                parseIf(listNode, lexer, state);
                break;
            case RANGE:
                parseRange(listNode, lexer, state);
                break;
            case TEMPLATE:
                parseTemplate(listNode, lexer, state);
                break;
            case WITH:
                parseWith(listNode, lexer, state);
                break;
            default:
                moveToPrevItem(lexer, state);

                // Just action
                ActionNode actionNode = new ActionNode();

                PipeNode pipeNode = new PipeNode("command");
                parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM, state);
                actionNode.setPipeNode(pipeNode);

                listNode.append(actionNode);
        }
    }


    private void parseBlock(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        String context = "block clause";

        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String blockTemplateName = StringUtils.unquote(token.value());
        TemplateNode blockTemplateNode = new TemplateNode(blockTemplateName);

        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM, state);
        blockTemplateNode.setPipeNode(pipeNode);


        // Parse block content as an associate template
        ListNode blockListNode = new ListNode();
        parseList(blockListNode, lexer, state);

        Node lastNode = blockListNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError(String.format("unexpected '%s' in block clause", lastNode));
        }
        if (lastNode instanceof EndNode) {
            blockListNode.removeLast();
        }

        listNode.append(blockTemplateNode);

        state.rootNodes.put(blockTemplateName, blockListNode);
    }


    private void parseDefinition(Lexer lexer, State state) throws TemplateParseException {
        String context = "define clause";

        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String definitionTemplateName = StringUtils.unquote(token.value());

        token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        ListNode definitionListNode = new ListNode();
        parseList(definitionListNode, lexer, state);

        Node lastNode = definitionListNode.getLast();
        if (lastNode instanceof EndNode) {
            definitionListNode.removeLast();
        } else {
            throwUnexpectError(String.format("unexpected '%s' in %s", lastNode, context));
            return;
        }

        state.rootNodes.put(definitionTemplateName, definitionListNode);
    }


    private void parseElse(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        switch (token.type()) {
            case IF:
                moveToPrevItem(lexer, state);
                listNode.append(new ElseNode());
                break;
            case RIGHT_DELIM:
                listNode.append(new ElseNode());
                break;
            default:
                throwUnexpectError(String.format("unexpected %s in end", token));
        }
    }

    private void parseEnd(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            throwUnexpectError(String.format("unexpected %s in end", token));
        }
        listNode.append(new EndNode());
    }

    private void parseIf(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer, state);
        moveToPrevItem(lexer, state);

        IfNode ifNode = new IfNode();
        parseBranch(ifNode, lexer, "if", true, state);
        listNode.append(ifNode);
    }

    private void parseRange(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer, state);
        moveToPrevItem(lexer, state);

        RangeNode rangeNode = new RangeNode();
        parseBranch(rangeNode, lexer, "range", true, state);
        listNode.append(rangeNode);
    }

    private void parseTemplate(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        String context = "template clause";

        Token token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new TemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String templateName = StringUtils.unquote(token.value());
        TemplateNode templateNode = new TemplateNode(templateName);

        token = moveToNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() != TokenType.RIGHT_DELIM) {
            moveToPrevItem(lexer, state);

            PipeNode pipeNode = new PipeNode(context);
            parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM, state);
            templateNode.setPipeNode(pipeNode);
        }

        listNode.append(templateNode);
    }

    private void parseWith(ListNode listNode, Lexer lexer, State state) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer, state);
        moveToPrevItem(lexer, state);

        WithNode withNode = new WithNode();
        parseBranch(withNode, lexer, "with", false, state);
        listNode.append(withNode);
    }

    private void parseBranch(BranchNode branchNode, Lexer lexer, String context, boolean allowElseIf, State state) throws TemplateParseException {
        int variableCount = state.variables.size();

        // Parse pipeline, the executable part
        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexer, TokenType.RIGHT_DELIM, state);
        branchNode.setPipeNode(pipeNode);

        // Parse 'if' clause
        ListNode ifListNode = new ListNode();
        parseList(ifListNode, lexer, state);
        branchNode.setIfListNode(ifListNode);

        // Parse if 'else' clause exists
        ListNode listNode = branchNode.getIfListNode();
        Node lastNode = listNode.getLast();
        if (lastNode instanceof ElseNode) {
            listNode.removeLast();

            if (allowElseIf) {
                Token token = lookNextNonSpaceToken(lexer, state);
                if (token == null) {
                    throwUnexpectError("missing token");
                }

                if (token.type() == TokenType.IF) {
                    moveToNextNonSpaceToken(lexer, state);

                    ListNode elseListNode = new ListNode();
                    parseIf(elseListNode, lexer, state);
                    branchNode.setElseListNode(elseListNode);

                    return;
                }
            }

            ListNode elseListNode = new ListNode();
            parseList(elseListNode, lexer, state);
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

        state.variables.subList(variableCount, state.variables.size()).clear();
    }

    private void parsePipe(PipeNode pipeNode, Lexer lexer, TokenType end, State state) throws TemplateParseException {
        Token token = lookNextNonSpaceToken(lexer, state);
        if (token == null) {
            throwUnexpectError("missing token");
        }

        if (token.type() == TokenType.VARIABLE) {
            parseVariable(pipeNode, lexer, state, token);
        }

        while (true) {
            token = moveToNextNonSpaceToken(lexer, state);
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
                    moveToPrevItem(lexer, state);
                    parseCommand(pipeNode, lexer, state);
                    break;
                case ERROR:
                default:
                    throw new TemplateParseException(String.format("unexpected %s in %s", token, pipeNode.getContext()));
            }
        }
    }

    private void parseVariable(PipeNode pipeNode, Lexer lexer, State state, Token variableToken) throws TemplateParseException {
        moveToNextNonSpaceToken(lexer, state);
        Token nextToken = lookNextNonSpaceToken(lexer, state);
        if (nextToken == null) {
            throwUnexpectError("missing token");
        }

        switch (nextToken.type()) {
            case ASSIGN:
            case DECLARE:
                moveToNextNonSpaceToken(lexer, state);
                pipeNode.append(new VariableNode(variableToken.value()));
                state.variables.add(variableToken.value());
                break;
            case CHAR:
                if (",".equals(nextToken.value())) {
                    moveToNextNonSpaceToken(lexer, state);
                    pipeNode.append(new VariableNode(variableToken.value()));
                    state.variables.add(variableToken.value());
                    if ("range".equals(pipeNode.getContext()) && pipeNode.getVariableCount() < 2) {
                        nextToken = lookNextNonSpaceToken(lexer, state);
                        if (nextToken == null) {
                            throwUnexpectError("missing token");
                        }

                        switch (nextToken.type()) {
                            case VARIABLE:
                            case RIGHT_DELIM:
                            case RIGHT_PAREN:
                                if (variableToken.type() == TokenType.VARIABLE) {
                                    parseVariable(pipeNode, lexer, state, nextToken);
                                }
                                break;
                            default:
                                throwUnexpectError("");
                        }
                    }
                }
                break;
            default:
                moveToPrevNonSpaceItem(lexer, state);
                break;
        }
    }

    private void parseCommand(PipeNode pipeNode, Lexer lexer, State state) throws TemplateParseException {
        CommandNode commandNode = new CommandNode();

        loop:
        while (true) {
            Node node = null;

            Token token = moveToNextNonSpaceToken(lexer, state);
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
                    node = findVariable(token.value(), state);
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
                    NumberNode numberNode = new NumberNode(token.value());
                    parseNumber(numberNode, token);
                    node = numberNode;
                    break;
                case STRING:
                case RAW_STRING:
                    node = new StringNode(token.value());
                    break;
                case LEFT_PAREN:
                    PipeNode nestedPipeNode = new PipeNode("parenthesized pipeline");
                    parsePipe(nestedPipeNode, lexer, TokenType.RIGHT_PAREN, state);
                    node = nestedPipeNode;
                    break;
                default:
                    moveToPrevItem(lexer, state);
            }

            if (node != null) {
                token = lookNextItem(lexer, state);
                if (token == null) {
                    throwUnexpectError("missing token");
                }

                if (token.type() == TokenType.FIELD) {
                    ChainNode chainNode = new ChainNode(node);
                    for (token = moveToNextToken(lexer, state); token.type() == TokenType.FIELD; token = moveToNextToken(lexer, state)) {
                        chainNode.append(token.value());
                    }
                    moveToPrevItem(lexer, state);

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

            token = moveToNextToken(lexer, state);
            switch (token.type()) {
                case SPACE:
                    continue loop;
                case RIGHT_DELIM:
                case RIGHT_PAREN:
                    moveToPrevItem(lexer, state);
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
    private Token lookNextItem(Lexer lexer, State state) {
        if (state.tokenIndex < lexer.getTokens().size()) {
            return lexer.getTokens().get(state.tokenIndex);
        }
        return null;
    }

    private void parseNumber(NumberNode numberNode, Token token) throws TemplateParseException {
        String value = token.value();
        TokenType type = token.type();
        parseNumber(numberNode, value, type);
    }

    void parseNumber(NumberNode numberNode, String value, TokenType type) throws TemplateParseException {
        if (type == TokenType.CHAR_CONSTANT) {
            if (value.charAt(0) != '\'') {
                throw new TemplateParseException(String.format("malformed character constant: %s", value));
            }

            int ch;
            try {
                ch = CharUtils.unquotedChar(value);
            } catch (IllegalArgumentException e) {
                throw new TemplateParseException("invalid syntax: " + value, e);
            }

            numberNode.setIsInt(true);
            numberNode.setIntValue(ch);
            numberNode.setIsFloat(true);
            numberNode.setFloatValue(ch);
            return;
        }

        if (type == TokenType.COMPLEX) {
            throw new TemplateParseException(String.format("complex number is unsupported: %s", value));
        }

        int length = value.length();
        if (length > 0 && value.charAt(length - 1) == 'i') {
            try {
                double image = Double.parseDouble(value.substring(0, length - 1));
                Complex complex = new Complex(0, image);
                numberNode.setComplex(complex);
                return;
            } catch (NumberFormatException ignored) {
            }
        }


        try {
            long intValue = Long.parseLong(value.replace("_", ""));
            numberNode.setIsInt(true);
            numberNode.setIntValue(intValue);
        } catch (NumberFormatException ignored) {
        }

        try {
            double floatValue = Double.parseDouble(value.replace("_", ""));
            numberNode.setIsFloat(true);
            numberNode.setFloatValue(floatValue);
        } catch (NumberFormatException ignored) {
        }


//                    if (value.endsWith("i")) {
//                        String numberString = value.substring(0, value.length() - 1);
//                        double number = Double.parseDouble(numberString);
//                    } else {
//                        numberNode
//                    }
//
//                    int i = Integer.parseInt(value);

    }

    /**
     * Retrieve previous token.
     * <p>
     * If the index is 0 (never run {@code moveToNextToken()}), then it won't change, this method will return null.
     * If the index points to the last token, then return the last to
     */
    private Token moveToPrevItem(Lexer lexer, State state) {
        if (state.tokenIndex > 0) {
            return lexer.getTokens().get(--state.tokenIndex);
        }
        return null;
    }

    /**
     * Retrieve next token, and move the index to the next token.
     * <p>
     * If current index points to the last token, then it won't change, this method will return null
     */
    private Token moveToNextToken(Lexer lexer, State state) {
        Token token = lookNextItem(lexer, state);
        if (token != null) {
            state.tokenIndex++;
        }
        return token;
    }

    /**
     * 获取下一个非空白元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    private Token lookNextNonSpaceToken(Lexer lexer, State state) {
        int count = 0;
        while (true) {
            Token token = moveToNextToken(lexer, state);
            count++;
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                state.tokenIndex -= count;
                return token;
            }
        }
    }

    /**
     * 获取上一个非空白元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    private Token moveToPrevNonSpaceItem(Lexer lexer, State state) {
        while (true) {
            Token token = moveToPrevItem(lexer, state);
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
    private Token moveToNextNonSpaceToken(Lexer lexer, State state) {
        while (true) {
            Token token = moveToNextToken(lexer, state);
            if (token == null) {
                return null;
            }
            if (token.type() != TokenType.SPACE) {
                return token;
            }
        }
    }

    private Node findVariable(String value, State state) throws TemplateParseException {
        VariableNode variableNode = new VariableNode(value);
        String name = variableNode.getIdentifier(0);
        if (state.variables.contains(name)) {
            return variableNode;
        }
        throwUnexpectError(String.format("undefined variable %s", name));
        return null;
    }

    private void throwUnexpectError(String message) throws TemplateParseException {
        throw new TemplateParseException(message);
    }


    private static class State {

        private final Map<String, Node> rootNodes = new LinkedHashMap<>();

        /**
         * A list which contains all the variables in a branch context
         */
        private final List<String> variables = new ArrayList<>();

        /**
         * Position marker
         */
        private int tokenIndex;

    }
}
