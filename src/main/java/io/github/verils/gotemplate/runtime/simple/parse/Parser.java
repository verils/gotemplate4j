package io.github.verils.gotemplate.runtime.simple.parse;

import io.github.verils.gotemplate.GoTemplate;
import io.github.verils.gotemplate.GoTemplateFactory;
import io.github.verils.gotemplate.GoTemplateNotFoundException;
import io.github.verils.gotemplate.GoTemplateParseException;
import io.github.verils.gotemplate.internal.*;
import io.github.verils.gotemplate.runtime.simple.lex.LexerViewer;

import java.util.ArrayList;
import java.util.List;

/**
 * Document of go template：<a href="https://pkg.go.dev/text/template#pkg-overview">Template</a>
 */
public class Parser {

    private final GoTemplateFactory factory;


    /**
     * A list which contains all the variables in a branch context
     */
    private final List<String> variables = new ArrayList<>();


    public Parser(GoTemplateFactory factory) {
        this.factory = factory;
        this.variables.add("$");
    }


    public void parse(String name, String text) throws GoTemplateParseException {
        // Parse the template text, build a list node as the root node
        ListNode rootNode = new ListNode();
        LexerViewer lexerViewer = new Lexer(text).getViewer();


        parseList(rootNode, lexerViewer);

        // Can not have ELSE and END node as the last in root list node
        Node lastNode = rootNode.getLast();
        if (lastNode instanceof ElseNode) {
            throwUnexpectError("unexpected " + rootNode);
        }
        if (lastNode instanceof EndNode) {
            throwUnexpectError("unexpected " + rootNode);
        }


        try {
            GoTemplate template = factory.getTemplate(name);
            ListNode root = (ListNode) template.root();
            for (Node node : root) {
                rootNode.append(node);
            }
        } catch (GoTemplateNotFoundException e) {
            GoTemplate template = new GoTemplate(factory, name, rootNode);
            factory.putTemplate(template);
        }
    }


    /**
     * Parse list node. Must check the last node in the list when this method return
     *
     * @param listNode    List node which contains all nodes in this context
     * @param lexerViewer Lex container
     */
    private void parseList(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        loop:
        while (true) {
            Token token = lexerViewer.nextItem();
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
                    token = lexerViewer.nextNonSpaceItem();
                    if (token.type() == TokenType.DEFINE) {
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
                    throwUnexpectError(String.format("unexpected %s in input", token));
            }
        }
    }


    private void parseAction(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        Token token = lexerViewer.nextNonSpaceItem();
        switch (token.type()) {
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
                parsePipe(pipeNode, lexerViewer, TokenType.RIGHT_DELIM);
                actionNode.setPipeNode(pipeNode);

                listNode.append(actionNode);
        }
    }


    private void parseBlock(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        String context = "block clause";

        Token token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new GoTemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String blockTemplateName = StringUtils.unquote(token.value());
        TemplateNode blockTemplateNode = new TemplateNode(blockTemplateName);

        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexerViewer, TokenType.RIGHT_DELIM);
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


        GoTemplate template = new GoTemplate(factory, blockTemplateName, blockListNode);
        factory.putTemplate(template);

        listNode.append(blockTemplateNode);
    }


    private void parseDefinition(LexerViewer lexerViewer) throws GoTemplateParseException {
        String context = "define clause";

        Token token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new GoTemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String definitionTemplateName = StringUtils.unquote(token.value());

        token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.RIGHT_DELIM) {
            throw new GoTemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
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

        GoTemplate template = new GoTemplate(factory, definitionTemplateName, definitionListNode);
        factory.putTemplate(template);
    }


    private void parseElse(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        Token token = lexerViewer.nextNonSpaceItem();
        switch (token.type()) {
            case IF:
                lexerViewer.prevItem();
                listNode.append(new ElseNode());
                break;
            case RIGHT_DELIM:
                listNode.append(new ElseNode());
                break;
            default:
                throwUnexpectError(String.format("unexpected %s in end", token));
        }
    }

    private void parseEnd(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        Token token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.RIGHT_DELIM) {
            throwUnexpectError(String.format("unexpected %s in end", token));
        }
        listNode.append(new EndNode());
    }

    private void parseIf(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        IfNode ifNode = new IfNode();
        parseBranch(ifNode, lexerViewer, "if", true);
        listNode.append(ifNode);
    }

    private void parseRange(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        RangeNode rangeNode = new RangeNode();
        parseBranch(rangeNode, lexerViewer, "range", true);
        listNode.append(rangeNode);
    }

    private void parseTemplate(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        String context = "template clause";

        Token token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.STRING && token.type() != TokenType.RAW_STRING) {
            throw new GoTemplateParseException(String.format("unexpected '%s' in %s", token.value(), context));
        }

        String templateName = StringUtils.unquote(token.value());
        TemplateNode templateNode = new TemplateNode(templateName);

        token = lexerViewer.nextNonSpaceItem();
        if (token.type() != TokenType.RIGHT_DELIM) {
            lexerViewer.prevItem();

            PipeNode pipeNode = new PipeNode(context);
            parsePipe(pipeNode, lexerViewer, TokenType.RIGHT_DELIM);
            templateNode.setPipeNode(pipeNode);
        }

        listNode.append(templateNode);
    }

    private void parseWith(ListNode listNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        lexerViewer.nextNonSpaceItem();
        lexerViewer.prevItem();

        WithNode withNode = new WithNode();
        parseBranch(withNode, lexerViewer, "with", false);
        listNode.append(withNode);
    }

    private void parseBranch(BranchNode branchNode, LexerViewer lexerViewer, String context, boolean allowElseIf) throws GoTemplateParseException {
        int variableCount = variables.size();

        // Parse pipeline, the executable part
        PipeNode pipeNode = new PipeNode(context);
        parsePipe(pipeNode, lexerViewer, TokenType.RIGHT_DELIM);
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
                Token token = lexerViewer.lookNextNonSpaceItem();
                if (token.type() == TokenType.IF) {
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

    private void parsePipe(PipeNode pipeNode, LexerViewer lexerViewer, TokenType end) throws GoTemplateParseException {
        Token token = lexerViewer.lookNextNonSpaceItem();

        if (token.type() == TokenType.VARIABLE) {
            parseVariable(pipeNode, lexerViewer, token);
        }

        while (true) {
            token = lexerViewer.nextNonSpaceItem();
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
                    lexerViewer.prevItem();
                    parseCommand(pipeNode, lexerViewer);
                    break;
                case ERROR:
                default:
                    throw new GoTemplateParseException(String.format("unexpected %s in %s", token, pipeNode.getContext()));
            }
        }
    }

    private void parseVariable(PipeNode pipeNode, LexerViewer lexerViewer, Token variableToken) throws GoTemplateParseException {
        lexerViewer.nextNonSpaceItem();
        Token nextToken = lexerViewer.lookNextNonSpaceItem();
        switch (nextToken.type()) {
            case ASSIGN:
            case DECLARE:
                lexerViewer.nextNonSpaceItem();
                pipeNode.append(new VariableNode(variableToken.value()));
                variables.add(variableToken.value());
                break;
            case CHAR:
                if (",".equals(nextToken.value())) {
                    lexerViewer.nextNonSpaceItem();
                    pipeNode.append(new VariableNode(variableToken.value()));
                    variables.add(variableToken.value());
                    if ("range".equals(pipeNode.getContext()) && pipeNode.getVariableCount() < 2) {
                        nextToken = lexerViewer.lookNextNonSpaceItem();
                        switch (nextToken.type()) {
                            case VARIABLE:
                            case RIGHT_DELIM:
                            case RIGHT_PAREN:
                                if (variableToken.type() == TokenType.VARIABLE) {
                                    parseVariable(pipeNode, lexerViewer, nextToken);
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

    private void parseCommand(PipeNode pipeNode, LexerViewer lexerViewer) throws GoTemplateParseException {
        CommandNode commandNode = new CommandNode();

        loop:
        while (true) {
            Node node = null;

            Token token = lexerViewer.nextNonSpaceItem();
            switch (token.type()) {
                case IDENTIFIER:
                    if (!hasFunction(token.value())) {
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
                    lexerViewer.prevItem();
                    node = parseNumber(lexerViewer);
                    break;
                case STRING:
                case RAW_STRING:
                    node = new StringNode(token.value());
                    break;
                case LEFT_PAREN:
                    PipeNode nestedPipeNode = new PipeNode("parenthesized pipeline");
                    parsePipe(nestedPipeNode, lexerViewer, TokenType.RIGHT_PAREN);
                    node = nestedPipeNode;
                    break;
                default:
                    lexerViewer.prevItem();
            }

            if (node != null) {
                token = lexerViewer.lookNextItem();
                if (token.type() == TokenType.FIELD) {
                    ChainNode chainNode = new ChainNode(node);
                    for (token = lexerViewer.nextItem(); token.type() == TokenType.FIELD; token = lexerViewer.nextItem()) {
                        chainNode.append(token.value());
                    }
                    lexerViewer.prevItem();

                    if (node instanceof FieldNode) {
                        node = new FieldNode(chainNode.toString());
                    } else if (node instanceof VariableNode) {
                        node = new VariableNode(chainNode.toString());
                    } else if (node instanceof BoolNode) {
                        throw new GoTemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof StringNode) {
                        throw new GoTemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NumberNode) {
                        throw new GoTemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof NilNode) {
                        throw new GoTemplateParseException(String.format("unexpected . after term %s", node));
                    } else if (node instanceof DotNode) {
                        throw new GoTemplateParseException(String.format("unexpected . after term %s", node));
                    } else {
                        node = chainNode;
                    }
                }
                commandNode.append(node);
            }

            token = lexerViewer.nextItem();
            switch (token.type()) {
                case SPACE:
                    continue loop;
                case RIGHT_DELIM:
                case RIGHT_PAREN:
                    lexerViewer.prevItem();
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

    private Node parseNumber(LexerViewer lexerViewer) throws GoTemplateParseException {
        Token nextToken = lexerViewer.nextItem();
        String value = nextToken.value();
        int length = value.length();

        if (nextToken.type() == TokenType.CHAR_CONSTANT) {
            if (value.charAt(0) != '\'') {
                throw new GoTemplateParseException(String.format("malformed character constant: %s", value));
            }

            int ch;
            try {
                ch = CharUtils.unquotedChar(value);
            } catch (IllegalArgumentException e) {
                throw new GoTemplateParseException("invalid syntax: " + value, e);
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
            throw new GoTemplateParseException(String.format("illegal number syntax: %s", value));
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

    private Node findVariable(String value) throws GoTemplateParseException {
        VariableNode variableNode = new VariableNode(value);
        String name = variableNode.getIdentifier(0);
        if (variables.contains(name)) {
            return variableNode;
        }
        throwUnexpectError(String.format("undefined variable %s", name));
        return null;
    }

    private boolean hasFunction(String name) {
        return factory.hasFunction(name);
    }

    private void throwUnexpectError(String message) throws GoTemplateParseException {
        throw new GoTemplateParseException(message);
    }

}
