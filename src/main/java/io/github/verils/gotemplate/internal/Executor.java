package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.TemplateExecutionException;
import io.github.verils.gotemplate.TemplateNotFoundException;
import io.github.verils.gotemplate.internal.ast.*;
import io.github.verils.gotemplate.internal.lang.StringEscapeUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Executor {

    private final Map<String, Node> rootNodes;
    private final Map<String, Function> functions;

    public Executor(Map<String, Node> rootNodes, Map<String, Function> functions) {
        this.rootNodes = rootNodes;
        this.functions = functions;
    }

    public void execute(String name, Object data, Writer writer) throws IOException,
            TemplateNotFoundException, TemplateExecutionException {
        ListNode listNode = (ListNode) rootNodes.get(name);
        if (listNode == null) {
            throw new TemplateNotFoundException(String.format("template '%s' not found", name));
        }

        if (data != null) {
            BeanInfo beanInfo = getBeanInfo(data);
            writeNode(writer, listNode, data, beanInfo);
        } else {
            writeNode(writer, listNode, null, null);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void writeNode(Writer writer, Node node, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        if (node instanceof ListNode) {
            writeList(writer, (ListNode) node, data, beanInfo);
        } else if (node instanceof ActionNode) {
            writeAction(writer, (ActionNode) node, data, beanInfo);
        } else if (node instanceof CommentNode) {
            // Ignore comment
        } else if (node instanceof IfNode) {
            writeIf(writer, (IfNode) node, data, beanInfo);
        } else if (node instanceof RangeNode) {
            writeRange(writer, (RangeNode) node, data, beanInfo);
        } else if (node instanceof TemplateNode) {
            writeTemplate(writer, (TemplateNode) node, data);
        } else if (node instanceof TextNode) {
            writeText(writer, (TextNode) node);
        } else if (node instanceof WithNode) {
            writeWith(writer, (WithNode) node, data, beanInfo);
        } else {
            throw new TemplateExecutionException(String.format("unknown node: %s", node.toString()));
        }
    }

    private void writeAction(Writer writer, ActionNode actionNode, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException {
        PipeNode pipeNode = actionNode.getPipeNode();
        Object value = executePipe(pipeNode, data, beanInfo);
        if (pipeNode.getVariableCount() == 0) {
            printValue(writer, value);
        }
    }

    private void writeIf(Writer writer, IfNode ifNode, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object value = executePipe(ifNode.getPipeNode(), data, beanInfo);
        if (isTrue(value)) {
            writeNode(writer, ifNode.getIfListNode(), data, beanInfo);
        } else if (ifNode.getElseListNode() != null) {
            writeNode(writer, ifNode.getElseListNode(), data, beanInfo);
        }
    }

    private void writeList(Writer writer, ListNode listNode, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        for (Node node : listNode) {
            writeNode(writer, node, data, beanInfo);
        }
    }

    private void writeRange(Writer writer, RangeNode rangeNode, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object arrayOrList = executePipe(rangeNode.getPipeNode(), data, beanInfo);

        if (arrayOrList.getClass().isArray()) {
            int length = Array.getLength(arrayOrList);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(arrayOrList, i);
                writeRangeValue(writer, rangeNode, value);
            }
        }

        if (arrayOrList instanceof Collection) {
            Collection<?> collection = (Collection<?>) arrayOrList;
            for (Object object : collection) {
                writeRangeValue(writer, rangeNode, object);
            }
        }

        if (arrayOrList instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) arrayOrList;
            for (Object object : map.values()) {
                writeRangeValue(writer, rangeNode, object);
            }
        }
    }

    private void writeRangeValue(Writer writer, RangeNode rangeNode, Object value) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        ListNode ifListNode = rangeNode.getIfListNode();
        for (Node node : ifListNode) {
            BeanInfo itemBeanInfo = getBeanInfo(value);
            writeNode(writer, node, value, itemBeanInfo);
        }
    }

    private void writeText(Writer writer, TextNode textNode) throws IOException {
        printText(writer, textNode.getText());
    }

    private void writeWith(Writer writer, WithNode withNode, Object data, BeanInfo beanInfo) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object value = executePipe(withNode.getPipeNode(), data, beanInfo);
        if (isTrue(value)) {
            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(writer, withNode.getIfListNode(), value, valueBeanInfo);
        } else if (withNode.getElseListNode() != null) {
            writeNode(writer, withNode.getElseListNode(), data, beanInfo);
        }
    }

    private void writeTemplate(Writer writer, TemplateNode templateNode, Object data) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        String name = templateNode.getName();

        ListNode listNode = (ListNode) rootNodes.get(name);
        if (listNode == null) {
            throw new TemplateExecutionException(String.format("template %s not defined", name));
        }

        if (data != null) {
            BeanInfo beanInfo = getBeanInfo(data);
            Object value = executePipe(templateNode.getPipeNode(), data, beanInfo);

            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(writer, listNode, value, valueBeanInfo);
        } else {
            writeNode(writer, listNode, null, null);
        }
    }

    private Object executePipe(PipeNode pipeNode, Object data, BeanInfo beanInfo) throws TemplateExecutionException {
        if (pipeNode == null) {
            return data;
        }

        Object value = null;
        for (CommandNode command : pipeNode.getCommands()) {
            value = executeCommand(command, data, beanInfo, value);
        }

        for (VariableNode variable : pipeNode.getVariables()) {
        }

        return value;
    }

    private Object executeCommand(CommandNode command, Object data, BeanInfo beanInfo, Object currentPipelineValue)
            throws TemplateExecutionException {
        Node firstArgument = command.getFirstArgument();
        if (firstArgument instanceof FieldNode) {
            return executeField((FieldNode) firstArgument, data, beanInfo);
        }
        if (firstArgument instanceof IdentifierNode) {
            return executeFunction((IdentifierNode) firstArgument, command.getArguments(), data, beanInfo, currentPipelineValue);
        }


        if (firstArgument instanceof DotNode) {
            return data;
        }
        if (firstArgument instanceof StringNode) {
            return ((StringNode) firstArgument).getText();
        }

        throw new TemplateExecutionException(String.format("can't evaluate command %s", firstArgument));
    }

    private Object executeField(FieldNode fieldNode, Object data, BeanInfo beanInfo)
            throws TemplateExecutionException {
        String[] identifiers = fieldNode.getIdentifiers();
        for (String identifier : identifiers) {
            if (data == null) {
                return null;
            }

            if (data instanceof Map) {
                //noinspection unchecked
                Map<String, Object> map = (Map<String, Object>) data;
                return map.get(identifier);
            }

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyDescriptorName = propertyDescriptor.getName();
                if ("class".equals(propertyDescriptorName)) {
                    continue;
                }

                String goStyleName = toGoStylePropertyName(propertyDescriptorName);
                if (identifier.equals(propertyDescriptorName) || identifier.equals(goStyleName)) {
                    Method readMethod = propertyDescriptor.getReadMethod();
                    try {
                        return readMethod.invoke(data);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new TemplateExecutionException(String.format(
                                "can't get value '%s' from data", identifier), e);
                    }
                }
            }

            throw new TemplateExecutionException(String.format("can't get value '%s' from data", identifier));
        }

        return null;
    }

    private Object executeFunction(IdentifierNode identifierNode, List<Node> arguments, Object data, BeanInfo beanInfo,
                                   Object finalValue) throws TemplateExecutionException {
        String identifier = identifierNode.getIdentifier();

        if (functions.containsKey(identifier)) {
            Function function = functions.get(identifier);
            if (function == null) {
                throw new TemplateExecutionException("call of null for " + identifier);
            }

            List<Node> args = arguments.subList(1, arguments.size());
            Object[] argumentValues;

            if (finalValue != null) {

                // per https://pkg.go.dev/text/template, "In a chained pipeline, the result of
                // each command is passed as the last argument of the following command." (This is necessary
                // when implementing functions like 'default', for example.)

                argumentValues = new Object[args.size() + 1];
                executeArguments(data, beanInfo, args, argumentValues);
                argumentValues[args.size()] = finalValue;
            } else {
                argumentValues = new Object[args.size()];
                executeArguments(data, beanInfo, args, argumentValues);
            }

            return function.invoke(argumentValues);
        }

        throw new TemplateExecutionException(String.format("%s is not a defined function", identifier));
    }

    private void executeArguments(Object data, BeanInfo beanInfo, List<Node> args, Object[] argumentValues) throws TemplateExecutionException {
        for (int i = 0; i < args.size(); i++) {
            Object value = executeArgument(args.get(i), data, beanInfo);
            argumentValues[i] = value;
        }
    }

    private Object executeArgument(Node argument, Object data, BeanInfo beanInfo) throws TemplateExecutionException {
        if (argument instanceof DotNode) {
            return data;
        }

        if (argument instanceof StringNode) {
            StringNode stringNode = (StringNode) argument;
            return stringNode.getText();
        }
        throw new TemplateExecutionException(String.format("can't extract value of argument %s", argument));
    }


    /**
     * Introspect the data object
     *
     * @param data Data object for the template
     * @return BeanInfo telling the details of data object
     */
    private BeanInfo getBeanInfo(Object data) {
        Class<?> type = data.getClass();

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(String.format("无法获取类型'%s'的Bean信息", type.getName()), e);
        }
        return beanInfo;
    }


    /**
     * Get go style property name
     *
     * @param propertyDescriptorName Name of property in an object
     * @return Go style property name
     */
    private String toGoStylePropertyName(String propertyDescriptorName) {
        return Character.toUpperCase(propertyDescriptorName.charAt(0)) + propertyDescriptorName.substring(1);
    }


    /**
     * Determine if a pipe evaluation returns a positive result, such as 'true' for a bool,
     * a none-null value for an object, a none-empty array or list
     *
     * @param value The result of the pipe evaluation
     * @return true if evaluation returns a positive result
     */
    private boolean isTrue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() != 0;
        }
        return false;
    }

    private void printText(Writer writer, String text) throws IOException {
        writer.write(text);
    }

    private void printValue(Writer writer, Object value) throws IOException {
        if (value instanceof String) {
            String unescaped = StringEscapeUtils.unescape((String) value);
            writer.write(unescaped);
        } else if (value instanceof Number) {
            writer.write(String.valueOf(value));
        }
    }
}
