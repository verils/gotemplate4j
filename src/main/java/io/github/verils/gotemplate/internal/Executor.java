package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.*;
import io.github.verils.gotemplate.internal.ast.*;
import io.github.verils.gotemplate.internal.lang.StringEscapeUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.*;
import java.util.*;

public class Executor {

    private static final String NO_VALUE = "<no value>";

    private final Map<String, Node> rootNodes;
    private final Map<String, Function> functions;
    private final MissingKeyPolicy missingKeyPolicy;
    private final boolean mapKeySorting;

    public Executor(Map<String, Node> rootNodes, Map<String, Function> functions) {
        this(rootNodes, functions, MissingKeyPolicy.INVALID, true);
    }

    public Executor(Map<String, Node> rootNodes, Map<String, Function> functions, MissingKeyPolicy missingKeyPolicy) {
        this(rootNodes, functions, missingKeyPolicy, true);
    }

    public Executor(Map<String, Node> rootNodes, Map<String, Function> functions, MissingKeyPolicy missingKeyPolicy, boolean mapKeySorting) {
        this.rootNodes = rootNodes;
        this.functions = functions;
        this.missingKeyPolicy = missingKeyPolicy != null ? missingKeyPolicy : MissingKeyPolicy.INVALID;
        this.mapKeySorting = mapKeySorting;
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
        Map<String, Object> blockVariables = new HashMap<>(variables);
        Object value = executePipe(ifNode.getPipeNode(), data, beanInfo, blockVariables);
        if (isTrue(value)) {
            writeNode(writer, ifNode.getIfListNode(), data, beanInfo, blockVariables);
        } else if (ifNode.getElseListNode() != null) {
            writeNode(writer, ifNode.getElseListNode(), data, beanInfo, blockVariables);
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
        Object numberOrIterable = executePipe(rangeNode.getPipeNode(), data, beanInfo, new HashMap<>(variables), false);

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

        boolean iterated = false;

        // Support range over integers (Go compatibility)
        // e.g., {{range $i := 5}} iterates from 0 to 4
        if (numberOrIterable instanceof Number) {
            int count = ((Number) numberOrIterable).intValue();
            // Only iterate if count is positive
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    iterated = true;
                    if (!writeRangeValue(writer, rangeNode, i, i, indexVarName, valueVarName, variables)) {
                        break;
                    }
                }
            }
        }

        if (numberOrIterable != null && numberOrIterable.getClass().isArray()) {
            int length = Array.getLength(numberOrIterable);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(numberOrIterable, i);
                iterated = true;
                if (!writeRangeValue(writer, rangeNode, i, value, indexVarName, valueVarName, variables)) {
                    break;
                }
            }
        }

        if (numberOrIterable instanceof Collection) {
            Collection<?> collection = (Collection<?>) numberOrIterable;
            int index = 0;
            for (Object object : collection) {
                iterated = true;
                if (!writeRangeValue(writer, rangeNode, index, object, indexVarName, valueVarName, variables)) {
                    break;
                }
                index++;
            }
        }

        if (numberOrIterable instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) numberOrIterable;

            // Sort map keys if enabled using TreeMap for natural ordering
            List<Map.Entry<?, ?>> entries;
            if (mapKeySorting) {
                map = new TreeMap<>(map);
            }
            entries = new ArrayList<>(map.entrySet());

            for (Map.Entry<?, ?> entry : entries) {
                // For maps, when two vars are specified, first is key, second is value
                Object entryValue = entry.getValue();
                Object entryKey = entry.getKey();
                iterated = true;
                if (!writeRangeValue(writer, rangeNode, entryKey, entryValue, indexVarName, valueVarName, variables)) {
                    break;
                }
            }
        }

        if (!iterated && rangeNode.getElseListNode() != null) {
            writeNode(writer, rangeNode.getElseListNode(), data, beanInfo, new HashMap<>(variables));
        }
    }

    /**
     * Writes one iteration of a {@code range} block.
     * <p>
     * The iteration value is unwrapped when it is an {@link Optional}, then rendered as the dot value for the
     * range body. Index/key and value variables declared by the range pipeline are bound in a copied variable
     * scope so assignments from this iteration do not leak into sibling iterations or the outer scope.
     *
     * @param writer       the destination writer
     * @param rangeNode    the range node whose body should be executed
     * @param index        the current array/list index or map key
     * @param value        the current iteration value
     * @param indexVarName the range index/key variable name, or {@code null} when none was declared
     * @param valueVarName the range value variable name, or {@code null} when none was declared
     * @param variables    variables visible before this iteration starts
     * @return {@code true} to continue the enclosing range loop, or {@code false} after a {@code break}
     * @throws IOException                if writing output fails
     * @throws TemplateExecutionException if executing the range body fails
     * @throws TemplateNotFoundException  if the range body invokes an undefined template
     */
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
        Map<String, Object> blockVariables = new HashMap<>(variables);
        Object value = executePipe(withNode.getPipeNode(), data, beanInfo, blockVariables);
        if (isTrue(value)) {
            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(writer, withNode.getIfListNode(), value, valueBeanInfo, blockVariables);
        } else if (withNode.getElseListNode() != null) {
            writeNode(writer, withNode.getElseListNode(), data, beanInfo, blockVariables);
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
        return executePipe(pipeNode, data, beanInfo, variables, true);
    }

    private Object executePipe(PipeNode pipeNode, Object data, BeanInfo beanInfo, Map<String, Object> variables,
                               boolean assignVariables) throws TemplateExecutionException {
        if (pipeNode == null) {
            return data;
        }

        Object value = null;
        for (CommandNode command : pipeNode.getCommands()) {
            value = executeCommand(command, data, beanInfo, value, variables);
        }

        // Handle variable assignments: {{$x := .Value | upper}}
        if (assignVariables) {
            for (VariableNode variable : pipeNode.getVariables()) {
                String varName = variable.getIdentifier(0);
                variables.put(varName, value);
            }
        }

        return value;
    }

    private Object executeCommand(CommandNode command, Object data, BeanInfo beanInfo, Object currentPipelineValue, Map<String, Object> variables)
            throws TemplateExecutionException {
        Node firstArgument = command.getFirstArgument();
        if (firstArgument instanceof FieldNode) {
            return executeField((FieldNode) firstArgument, data);
        }
        if (firstArgument instanceof IdentifierNode) {
            return executeFunction((IdentifierNode) firstArgument, command.getArguments(), data, beanInfo, currentPipelineValue, variables);
        }


        if (firstArgument instanceof DotNode) {
            return data;
        }
        if (firstArgument instanceof NilNode) {
            return null;
        }
        if (firstArgument instanceof StringNode) {
            return ((StringNode) firstArgument).getText();
        }
        if (firstArgument instanceof NumberNode) {
            NumberNode numberNode = (NumberNode) firstArgument;
            if (numberNode.isInt()) {
                return numberNode.getIntValue();
            }
            if (numberNode.isFloat()) {
                return numberNode.getFloatValue();
            }
            return 0;
        }
        if (firstArgument instanceof BoolNode) {
            return ((BoolNode) firstArgument).getValue();
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

    private Object executeField(final FieldNode fieldNode, final Object data)
            throws TemplateExecutionException {
        return executeFieldPath(fieldNode.getIdentifiers(), 0, data);
    }

    private Object executeFieldPath(final String[] identifiers, int start, final Object data)
            throws TemplateExecutionException {
        Object currentData = data;

        for (int i = start; i < identifiers.length; i++) {
            String identifier = identifiers[i];
            if (currentData == null) {
                if (missingKeyPolicy == MissingKeyPolicy.ERROR) {
                    throw new TemplateExecutionException(String.format("missing value for field-chain segment '%s'", identifier));
                }
                return null;
            }

            // Unwrap Optional if present
            currentData = unwrapOptional(currentData);
            if (currentData == null) {
                if (missingKeyPolicy == MissingKeyPolicy.ERROR) {
                    throw new TemplateExecutionException(String.format("missing value for field-chain segment '%s'", identifier));
                }
                return null;
            }

            if (currentData instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) currentData;
                if (!map.containsKey(identifier)) {
                    currentData = handleMissingMapKey(identifier);
                    continue;
                }
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

            // Go permits no-arg method access in chains. In Java, only public no-arg methods
            // are exposed this way; methods with arguments remain unsupported.
            if (!found) {
                Method method = findNoArgMethod(currentData.getClass(), identifier);
                if (method != null) {
                    try {
                        value = unwrapOptional(method.invoke(currentData));
                        found = true;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new TemplateExecutionException(String.format(
                                "can't invoke method '%s' from data", identifier), e);
                    }
                }
            }

            // If no getter found, try public fields
            if (!found) {
                try {
                    Field field = findField(currentData.getClass(), identifier);
                    if (field != null) {
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
        try {
            Field field = clazz.getField(fieldName);
            return Modifier.isPublic(field.getModifiers()) ? field : null;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private Method findNoArgMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getParameterTypes().length == 0
                    && Modifier.isPublic(method.getModifiers())
                    && method.getName().equals(methodName)
                    && !"getClass".equals(method.getName())) {
                return method;
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

        if ("and".equals(identifier) || "or".equals(identifier)) {
            return executeShortCircuitFunction(identifier, cmdArgNodes, data, beanInfo, finalValue, variables);
        }

        if ("index".equals(identifier) && functions.get("index") == Functions.BUILTIN.get("index")) {
            return executeIndex(cmdArgNodes.subList(1, cmdArgNodes.size()), data, beanInfo, finalValue, variables);
        }

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

            try {
                return function.invoke(functionArgs);
            } catch (RuntimeException e) {
                throw new TemplateExecutionException(String.format("function '%s' failed", identifier), e);
            }
        }

        throw new TemplateExecutionException(String.format("%s is not a defined function", identifier));
    }

    private Object executeIndex(List<Node> functionArgNodes, Object data, BeanInfo beanInfo,
                                Object finalValue, Map<String, Object> variables) throws TemplateExecutionException {
        Object[] functionArgs;
        if (finalValue != null) {
            functionArgs = new Object[functionArgNodes.size() + 1];
            executeArguments(data, beanInfo, functionArgNodes, functionArgs, variables);
            functionArgs[functionArgNodes.size()] = finalValue;
        } else {
            functionArgs = new Object[functionArgNodes.size()];
            executeArguments(data, beanInfo, functionArgNodes, functionArgs, variables);
        }

        if (functionArgs.length < 2) {
            throw new TemplateExecutionException("function 'index' failed", new IllegalArgumentException("index requires at least 2 arguments"));
        }

        Object collection = functionArgs[0];
        Object key = functionArgs[1];
        if (collection == null) {
            if (missingKeyPolicy == MissingKeyPolicy.ERROR) {
                throw new TemplateExecutionException("index of null value");
            }
            return null;
        }
        if (collection instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) collection;
            if (!map.containsKey(key)) {
                return handleMissingMapKey(String.valueOf(key));
            }
            return unwrapOptional(map.get(key));
        }

        Function indexFunction = functions.get("index");
        if (indexFunction == null) {
            throw new TemplateExecutionException("index is not a defined function");
        }
        try {
            return indexFunction.invoke(functionArgs);
        } catch (RuntimeException e) {
            throw new TemplateExecutionException("function 'index' failed", e);
        }
    }

    private Object handleMissingMapKey(String key) throws TemplateExecutionException {
        if (missingKeyPolicy == MissingKeyPolicy.ERROR) {
            throw new TemplateExecutionException(String.format("missing map key '%s'", key));
        }
        return null;
    }

    private Object executeShortCircuitFunction(String identifier, List<Node> cmdArgNodes, Object data, BeanInfo beanInfo,
                                               Object finalValue, Map<String, Object> variables) throws TemplateExecutionException {
        List<Node> functionArgNodes = cmdArgNodes.subList(1, cmdArgNodes.size());

        Object last = null;
        for (Node argNode : functionArgNodes) {
            last = executeArgument(argNode, data, beanInfo, variables);
            if ("and".equals(identifier) && !isTrue(last)) {
                return last;
            }
            if ("or".equals(identifier) && isTrue(last)) {
                return last;
            }
        }

        if (finalValue != null) {
            last = finalValue;
            if ("and".equals(identifier) && !isTrue(last)) {
                return last;
            }
            if ("or".equals(identifier) && isTrue(last)) {
                return last;
            }
        }

        return last;
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

        if (argument instanceof NilNode) {
            return null;
        }

        if (argument instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) argument;
            return executeField(fieldNode, data);
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

    private void printValue(Writer writer, Object value) throws IOException {
        if (value == null) {
            // Go template behavior: display "<no value>" for null
            printText(writer, NO_VALUE);
        } else if (value instanceof String) {
            String unescaped = StringEscapeUtils.unescape((String) value);
            printText(writer, unescaped);
        } else if (value instanceof Number) {
            printText(writer, String.valueOf(value));
        } else if (value instanceof Boolean) {
            printText(writer, String.valueOf(value));
        } else {
            // For other types (including enums), use toString()
            printText(writer, String.valueOf(value));
        }
    }

    private void printText(Writer writer, String text) throws IOException {
        writer.write(text);
    }

    private static class BreakException extends RuntimeException {
        private static final BreakException INSTANCE = new BreakException();
    }

    private static class ContinueException extends RuntimeException {
        private static final ContinueException INSTANCE = new ContinueException();
    }
}
