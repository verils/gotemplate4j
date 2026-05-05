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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Map<String, Object> variables = new HashMap<>();
        variables.put("$", data);
        if (data != null) {
            BeanInfo beanInfo = getBeanInfo(data);
            writeNode(writer, listNode, data, beanInfo, variables);
        } else {
            writeNode(writer, listNode, null, null, variables);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void writeNode(Writer writer, Node node, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        if (node instanceof ListNode) {
            writeList(writer, (ListNode) node, data, beanInfo, variables);
        } else if (node instanceof ActionNode) {
            writeAction(writer, (ActionNode) node, data, beanInfo, variables);
        } else if (node instanceof CommentNode) {
            // Ignore comment
        } else if (node instanceof BreakNode) {
            throw BreakException.INSTANCE;
        } else if (node instanceof ContinueNode) {
            throw ContinueException.INSTANCE;
        } else if (node instanceof IfNode) {
            writeIf(writer, (IfNode) node, data, beanInfo, variables);
        } else if (node instanceof RangeNode) {
            writeRange(writer, (RangeNode) node, data, beanInfo, variables);
        } else if (node instanceof TemplateNode) {
            writeTemplate(writer, (TemplateNode) node, data, variables);
        } else if (node instanceof TextNode) {
            writeText(writer, (TextNode) node);
        } else if (node instanceof WithNode) {
            writeWith(writer, (WithNode) node, data, beanInfo, variables);
        } else {
            throw new TemplateExecutionException(String.format("unknown node: %s", node.toString()));
        }
    }

    private void writeAction(Writer writer, ActionNode actionNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException {
        PipeNode pipeNode = actionNode.getPipeNode();
        Object value = executePipe(pipeNode, data, beanInfo, variables);
        if (pipeNode.getVariableCount() == 0) {
            printValue(writer, value);
        }
    }

    private void writeIf(Writer writer, IfNode ifNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object value = executePipe(ifNode.getPipeNode(), data, beanInfo, variables);
        if (isTrue(value)) {
            writeNode(writer, ifNode.getIfListNode(), data, beanInfo, variables);
        } else if (ifNode.getElseListNode() != null) {
            writeNode(writer, ifNode.getElseListNode(), data, beanInfo, variables);
        }
    }

    private void writeList(Writer writer, ListNode listNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        for (Node node : listNode) {
            writeNode(writer, node, data, beanInfo, variables);
        }
    }

    private void writeRange(Writer writer, RangeNode rangeNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object arrayOrList = executePipe(rangeNode.getPipeNode(), data, beanInfo, variables);

        // Get variable names from the range node's pipe
        List<VariableNode> rangeVars = rangeNode.getPipeNode().getVariables();
        // In Go templates: {{range $v := .Items}} means $v gets the value (no index)
        // {{range $i, $v := .Items}} means $i gets index, $v gets value
        String indexVarName = null;
        String valueVarName = null;

        if (rangeVars.size() == 2) {
            // Two variables: first is index, second is value
            indexVarName = rangeVars.get(0).getIdentifier(0);
            valueVarName = rangeVars.get(1).getIdentifier(0);
        } else if (rangeVars.size() == 1) {
            // One variable: it's the value
            valueVarName = rangeVars.get(0).getIdentifier(0);
        }

        if (arrayOrList.getClass().isArray()) {
            int length = Array.getLength(arrayOrList);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(arrayOrList, i);
                if (!writeRangeValue(writer, rangeNode, i, value, indexVarName, valueVarName, variables)) {
                    break;
                }
            }
        }

        if (arrayOrList instanceof Collection) {
            Collection<?> collection = (Collection<?>) arrayOrList;
            int index = 0;
            for (Object object : collection) {
                if (!writeRangeValue(writer, rangeNode, index, object, indexVarName, valueVarName, variables)) {
                    break;
                }
                index++;
            }
        }

        if (arrayOrList instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) arrayOrList;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                // For maps, when two vars are specified, first is key, second is value
                Object entryValue = entry.getValue();
                Object entryKey = entry.getKey();
                if (!writeRangeValue(writer, rangeNode, entryKey, entryValue, indexVarName, valueVarName, variables)) {
                    break;
                }
            }
        }
    }

    private boolean writeRangeValue(Writer writer, RangeNode rangeNode, Object index, Object value,
                                 String indexVarName, String valueVarName, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        // Unwrap Optional if present
        value = unwrapOptional(value);

        // Create a copy of variables for this iteration to avoid pollution
        Map<String, Object> iterationVars = new HashMap<>(variables);

        // Set index variable if specified (e.g., {{range $i, $v := .Items}})
        if (indexVarName != null) {
            iterationVars.put(indexVarName, index);
        }

        // Set value variable if specified
        if (valueVarName != null) {
            iterationVars.put(valueVarName, value);
        }

        ListNode ifListNode = rangeNode.getIfListNode();
        try {
            for (Node node : ifListNode) {
                BeanInfo itemBeanInfo = value != null ? getBeanInfo(value) : null;
                writeNode(writer, node, value, itemBeanInfo, iterationVars);
            }
            return true;
        } catch (ContinueException e) {
            return true;
        } catch (BreakException e) {
            return false;
        }
    }

    private void writeText(Writer writer, TextNode textNode) throws IOException {
        printText(writer, textNode.getText());
    }

    private void writeWith(Writer writer, WithNode withNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        Object value = executePipe(withNode.getPipeNode(), data, beanInfo, variables);
        if (isTrue(value)) {
            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(writer, withNode.getIfListNode(), value, valueBeanInfo, variables);
        } else if (withNode.getElseListNode() != null) {
            writeNode(writer, withNode.getElseListNode(), data, beanInfo, variables);
        }
    }

    private void writeTemplate(Writer writer, TemplateNode templateNode, Object data, Map<String, Object> variables) throws IOException,
            TemplateExecutionException, TemplateNotFoundException {
        String name = templateNode.getName();

        ListNode listNode = (ListNode) rootNodes.get(name);
        if (listNode == null) {
            throw new TemplateExecutionException(String.format("template %s not defined", name));
        }

        BeanInfo beanInfo = data != null ? getBeanInfo(data) : null;
        Object value = templateNode.getPipeNode() != null
                ? executePipe(templateNode.getPipeNode(), data, beanInfo, variables)
                : null;

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("$", value);

        if (value == null) {
            writeNode(writer, listNode, null, null, templateVariables);
        } else {
            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(writer, listNode, value, valueBeanInfo, templateVariables);
        }
    }

    private Object executePipe(PipeNode pipeNode, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws TemplateExecutionException {
        if (pipeNode == null) {
            return data;
        }

        Object value = null;
        for (CommandNode command : pipeNode.getCommands()) {
            value = executeCommand(command, data, beanInfo, value, variables);
        }

        // Handle variable assignments: {{$x := .Value | upper}}
        for (VariableNode variable : pipeNode.getVariables()) {
            String varName = variable.getIdentifier(0);
            variables.put(varName, value);
        }

        return value;
    }

    private Object executeCommand(CommandNode command, Object data, BeanInfo beanInfo, Object currentPipelineValue, Map<String, Object> variables)
            throws TemplateExecutionException {
        Node firstArgument = command.getFirstArgument();
        if (firstArgument instanceof FieldNode) {
            return executeField((FieldNode) firstArgument, data, beanInfo);
        }
        if (firstArgument instanceof IdentifierNode) {
            return executeFunction((IdentifierNode) firstArgument, command.getArguments(), data, beanInfo, currentPipelineValue, variables);
        }


        if (firstArgument instanceof DotNode) {
            return data;
        }
        if (firstArgument instanceof StringNode) {
            return ((StringNode) firstArgument).getText();
        }
        if (firstArgument instanceof VariableNode) {
            return executeVariable((VariableNode) firstArgument, variables);
        }
        if (firstArgument instanceof PipeNode) {
            // Support pipeline expressions as commands: {{template "name" (.X | printf "%s")}}
            return executePipe((PipeNode) firstArgument, data, beanInfo, variables);
        }

        throw new TemplateExecutionException(String.format("can't evaluate command %s", firstArgument));
    }

    private Object executeField(final FieldNode fieldNode, final Object data, final BeanInfo beanInfo)
            throws TemplateExecutionException {
        return executeFieldPath(fieldNode.getIdentifiers(), 0, data);
    }

    private Object executeFieldPath(final String[] identifiers, int start, final Object data)
            throws TemplateExecutionException {
        Object currentData = data;

        for (int i = start; i < identifiers.length; i++) {
            String identifier = identifiers[i];
            if (currentData == null) {
                return null;
            }

            // Unwrap Optional if present
            currentData = unwrapOptional(currentData);
            if (currentData == null) {
                return null;
            }

            if (currentData instanceof Map) {
                //noinspection unchecked
                Map<String, Object> map = (Map<String, Object>) currentData;
                currentData = unwrapOptional(map.get(identifier));
                continue;
            }

            BeanInfo currentBeanInfo = getBeanInfo(currentData);

            // First, try to find a getter method
            PropertyDescriptor[] propertyDescriptors = currentBeanInfo.getPropertyDescriptors();
            Object value = null;
            boolean found = false;

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyDescriptorName = propertyDescriptor.getName();
                if ("class".equals(propertyDescriptorName)) {
                    continue;
                }

                String goStyleName = toGoStylePropertyName(propertyDescriptorName);
                if (identifier.equals(propertyDescriptorName) || identifier.equals(goStyleName)) {
                    Method readMethod = propertyDescriptor.getReadMethod();
                    try {
                        value = unwrapOptional(readMethod.invoke(currentData));
                        found = true;
                        break;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new TemplateExecutionException(String.format(
                                "can't get value '%s' from data", identifier), e);
                    }
                }
            }

            // If no getter found, try public fields
            if (!found) {
                try {
                    Field field = findField(currentData.getClass(), identifier);
                    if (field != null) {
                        field.setAccessible(true);
                        value = unwrapOptional(field.get(currentData));
                        found = true;
                    }
                } catch (IllegalAccessException e) {
                    throw new TemplateExecutionException(String.format(
                            "can't access field '%s' from data", identifier), e);
                }
            }

            if (!found) {
                throw new TemplateExecutionException(String.format("can't get value '%s' from data", identifier));
            }

            // Update currentData and beanInfo for next iteration
            currentData = value;
        }

        return currentData;
    }

    /**
     * Unwrap Optional values
     *
     * @param obj The object to unwrap
     * @return The unwrapped value, or null if Optional is empty
     */
    private Object unwrapOptional(Object obj) {
        if (obj instanceof Optional) {
            Optional<?> optional = (Optional<?>) obj;
            return optional.orElse(null);
        }
        return obj;
    }

    /**
     * Find a field by name in the class hierarchy
     *
     * @param clazz     The class to search
     * @param fieldName The field name to find
     * @return The Field object if found, null otherwise
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Try superclass
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private Object executeVariable(VariableNode variableNode, Map<String, Object> variables) throws TemplateExecutionException {
        String varName = variableNode.getIdentifier(0);
        if (!variables.containsKey(varName)) {
            throw new TemplateExecutionException(String.format("undefined variable \"%s\"", varName));
        }
        Object value = variables.get(varName);
        String[] identifiers = variableNode.getIdentifiers();
        if (identifiers.length == 1) {
            return value;
        }
        return executeFieldPath(identifiers, 1, value);
    }

    private Object executeFunction(IdentifierNode identifierNode, List<Node> cmdArgNodes, Object data, BeanInfo beanInfo,
                                   Object finalValue, Map<String, Object> variables) throws TemplateExecutionException {
        String identifier = identifierNode.getIdentifier();

        if (functions.containsKey(identifier)) {
            Function function = functions.get(identifier);
            if (function == null) {
                throw new TemplateExecutionException("call of null for " + identifier);
            }

            List<Node> functionArgNodes = cmdArgNodes.subList(1, cmdArgNodes.size());

            Object[] functionArgs;
            if (finalValue != null) {

                // per https://pkg.go.dev/text/template, "In a chained pipeline, the result of
                // each command is passed as the last argument of the following command." (This is necessary
                // when implementing functions like 'default', for example.)

                functionArgs = new Object[functionArgNodes.size() + 1];
                executeArguments(data, beanInfo, functionArgNodes, functionArgs, variables);
                functionArgs[functionArgNodes.size()] = finalValue;
            } else {
                functionArgs = new Object[functionArgNodes.size()];
                executeArguments(data, beanInfo, functionArgNodes, functionArgs, variables);
            }

            return function.invoke(functionArgs);
        }

        throw new TemplateExecutionException(String.format("%s is not a defined function", identifier));
    }

    private void executeArguments(Object data, BeanInfo beanInfo, List<Node> args, Object[] argumentValues, Map<String, Object> variables) throws TemplateExecutionException {
        for (int i = 0; i < args.size(); i++) {
            Object value = executeArgument(args.get(i), data, beanInfo, variables);
            argumentValues[i] = value;
        }
    }

    private Object executeArgument(Node argument, Object data, BeanInfo beanInfo, Map<String, Object> variables) throws TemplateExecutionException {
        if (argument instanceof DotNode) {
            return data;
        }

        if (argument instanceof StringNode) {
            StringNode stringNode = (StringNode) argument;
            return stringNode.getText();
        }

        if (argument instanceof NumberNode) {
            NumberNode numberNode = (NumberNode) argument;
            if (numberNode.isInt()) {
                return numberNode.getIntValue();
            }
            if (numberNode.isFloat()) {
                return numberNode.getFloatValue();
            }
            return 0;
        }

        if (argument instanceof BoolNode) {
            BoolNode boolNode = (BoolNode) argument;
            return boolNode.getValue();
        }

        if (argument instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) argument;
            return executeField(fieldNode, data, beanInfo);
        }

        if (argument instanceof VariableNode) {
            return executeVariable((VariableNode) argument, variables);
        }

        if (argument instanceof PipeNode) {
            PipeNode pipeNode = (PipeNode) argument;
            return executePipe(pipeNode, data, beanInfo, variables);
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
        if (value instanceof Number) {
            // Zero values are falsy in Go templates
            if (value instanceof Integer) {
                return ((Integer) value) != 0;
            }
            if (value instanceof Long) {
                return ((Long) value) != 0L;
            }
            if (value instanceof Float) {
                return ((Float) value) != 0.0f;
            }
            if (value instanceof Double) {
                return ((Double) value) != 0.0;
            }
            if (value instanceof Short) {
                return ((Short) value) != 0;
            }
            if (value instanceof Byte) {
                return ((Byte) value) != 0;
            }
            // For other number types, check if not zero
            return ((Number) value).doubleValue() != 0.0;
        }
        if (value != null && value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value) > 0;
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        return value != null;
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
        } else if (value instanceof Boolean) {
            writer.write(String.valueOf(value));
        } else if (value != null) {
            // For other types (including enums), use toString()
            writer.write(String.valueOf(value));
        }
    }

    private static class BreakException extends RuntimeException {
        private static final BreakException INSTANCE = new BreakException();
    }

    private static class ContinueException extends RuntimeException {
        private static final ContinueException INSTANCE = new ContinueException();
    }
}
