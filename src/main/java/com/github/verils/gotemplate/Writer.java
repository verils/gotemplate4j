package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class Writer {

    private final StringBuilder sb;

    public Writer(StringBuilder sb) {
        this.sb = sb;
    }


    public void write(Node rootNode, Object data) {
        BeanInfo beanInfo = getBeanInfo(data);
        writeNode(rootNode, data, beanInfo);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void writeNode(Node node, Object data, BeanInfo beanInfo) {
        if (node instanceof ListNode) {
            writeList((ListNode) node, data, beanInfo);
        } else if (node instanceof ActionNode) {
            writeAction((ActionNode) node, data, beanInfo);
        } else if (node instanceof CommentNode) {
            // Ignore comment
        } else if (node instanceof IfNode) {
            writeIf((IfNode) node, data, beanInfo);
        } else if (node instanceof TextNode) {
            writeText((TextNode) node, data);
        } else if (node instanceof WithNode) {
            writeWith((WithNode) node, data, beanInfo);
        } else {
            throw new ExecutionException(String.format("unknown node: %s", node.toString()));
        }
    }

    private void writeAction(ActionNode actionNode, Object data, BeanInfo beanInfo) {
        PipeNode pipeNode = actionNode.getPipeNode();
        Object value = executePipe(pipeNode, data, beanInfo);
        if (pipeNode.getVariableCount() == 0) {
            printValue(value);
        }
    }

    private void writeIf(IfNode ifNode, Object data, BeanInfo beanInfo) {
        Object value = executePipe(ifNode.getPipeNode(), data, beanInfo);
        if (isTrue(value)) {
            writeNode(ifNode.getIfListNode(), data, beanInfo);
        } else if (ifNode.getElseListNode() != null) {
            writeNode(ifNode.getElseListNode(), data, beanInfo);
        }
    }

    private void writeList(ListNode listNode, Object data, BeanInfo beanInfo) {
        for (Node node : listNode) {
            writeNode(node, data, beanInfo);
        }
    }

    private void writeText(TextNode textNode, Object data) {
        printText(textNode.getText());
    }

    private void writeWith(WithNode withNode, Object data, BeanInfo beanInfo) {
        Object value = executePipe(withNode.getPipeNode(), data, beanInfo);
        if (isTrue(value)) {
            BeanInfo valueBeanInfo = getBeanInfo(value);
            writeNode(withNode.getIfListNode(), value, valueBeanInfo);
        } else if (withNode.getElseListNode() != null) {
            writeNode(withNode.getElseListNode(), data, beanInfo);
        }
    }

    private Object executePipe(PipeNode pipeNode, Object data, BeanInfo beanInfo) {
        if (pipeNode == null) {
            return data;
        }

        Object value = null;
        for (CommandNode command : pipeNode.getCommands()) {
            value = executeCommand(command, data, beanInfo);
        }

        for (VariableNode variable : pipeNode.getVariables()) {
        }

        return value;
    }

    private Object executeCommand(CommandNode command, Object data, BeanInfo beanInfo) {
        Node firstArgument = command.getFirstArgument();
        if (firstArgument instanceof FieldNode) {
            return executeField((FieldNode) firstArgument, data, beanInfo);
        }


        if (firstArgument instanceof DotNode) {
            return data;
        }

        throw new ExecutionException(String.format("can't evaluate command %s", firstArgument));
    }

    private Object executeField(FieldNode fieldNode, Object data, BeanInfo beanInfo) {
        String[] identifiers = fieldNode.getIdentifiers();
        for (String identifier : identifiers) {
            if (data == null) {
                return null;
            }

            if (data instanceof Map) {
                //noinspection unchecked
                Map<String, Object> map = (Map<String, Object>) data;
                return null;
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
                        throw new ExecutionException(String.format("can't get value '%s' from data", identifier), e);
                    }
                }
            }

            throw new ExecutionException(String.format("can't get value '%s' from data", identifier));
        }

        return null;
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

    private void printText(String text) {
        sb.append(text);
    }

    private void printValue(Object value) {
        sb.append(value);
    }
}
