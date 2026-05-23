package io.github.verils.gotemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Functions {

    public static final Map<String, Function> BUILTIN = new LinkedHashMap<>();

    static {
        BUILTIN.put("call", call());
        BUILTIN.put("html", html());
        BUILTIN.put("index", index());
        BUILTIN.put("slice", slice());
        BUILTIN.put("js", js());
        BUILTIN.put("len", len());
        BUILTIN.put("print", print());
        BUILTIN.put("printf", printf());
        BUILTIN.put("println", println());
        BUILTIN.put("urlquery", urlquery());

        // Logical operations
        BUILTIN.put("and", Functions::and);
        BUILTIN.put("or", Functions::or);
        BUILTIN.put("not", Functions::not);

        // Comparisons
        BUILTIN.put("eq", eq());
        BUILTIN.put("ge", ge());
        BUILTIN.put("gt", gt());
        BUILTIN.put("le", le());
        BUILTIN.put("lt", lt());
        BUILTIN.put("ne", ne());

        // Additional functions (Phase 2.3)
        BUILTIN.put("deepEqual", deepEqual());
        BUILTIN.put("typeof", typeof());
        BUILTIN.put("kindOf", kindOf());

        // Null-safety functions (Phase 2.2.4)
        BUILTIN.put("default", defaultValue());
    }

    private static Function print() {
        return args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            return stringBuilder.toString();
        };
    }

    private static Function printf() {
        return args -> {
            int realArgsLength = args.length - 1;
            Object[] realArgs = new Object[realArgsLength];
            System.arraycopy(args, 1, realArgs, 0, realArgsLength);
            return String.format(String.valueOf(args[0]), realArgs);
        };
    }

    private static Function println() {
        return args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            stringBuilder.append("\n");
            return stringBuilder.toString();
        };
    }

    // Comparison operators
    private static Function eq() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("eq requires at least 2 arguments");
            }
            Object first = args[0];
            for (int i = 1; i < args.length; i++) {
                if (!equal(first, args[i])) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Function ne() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("ne requires at least 2 arguments");
            }
            Object first = args[0];
            for (int i = 1; i < args.length; i++) {
                if (equal(first, args[i])) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Function lt() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("lt requires at least 2 arguments");
            }
            return compare(args, -1);
        };
    }

    private static Function le() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("le requires at least 2 arguments");
            }
            return compare(args, 0);
        };
    }

    private static Function gt() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("gt requires at least 2 arguments");
            }
            return compare(args, 1);
        };
    }

    private static Function ge() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("ge requires at least 2 arguments");
            }
            // ge means greater than or equal, so we want cmp >= 0
            return compare(args, 0, true);
        };
    }

    // Logical operators

    /**
     * Short-circuit AND function.
     * This method is never actually invoked because short-circuit evaluation
     * is handled directly in Executor#executeShortCircuitFunction.
     * It exists only as a placeholder for function registration.
     */
    private static Object and(Object[] args) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Short-circuit OR function.
     * This method is never actually invoked because short-circuit evaluation
     * is handled directly in Executor#executeShortCircuitFunction.
     * It exists only as a placeholder for function registration.
     */
    private static Object or(Object[] args) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Logical NOT operation.
     *
     * @param args array containing a single argument to negate
     * @return true if the argument is falsy, false otherwise
     */
    private static boolean not(Object[] args) {
        return isFalsy(args[0]);
    }

    // Collection functions
    private static Function len() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("len requires exactly 1 argument");
            }
            Object arg = args[0];
            if (arg == null) {
                return 0;
            }
            if (arg instanceof String) {
                return ((String) arg).length();
            }
            if (arg instanceof Collection) {
                return ((Collection<?>) arg).size();
            }
            if (arg.getClass().isArray()) {
                return Array.getLength(arg);
            }
            if (arg instanceof Map) {
                return ((Map<?, ?>) arg).size();
            }
            throw new IllegalArgumentException("len: invalid type " + arg.getClass().getName());
        };
    }

    private static Function index() {
        return args -> {
            if (args.length < 2) {
                throw new IllegalArgumentException("index requires at least 2 arguments");
            }
            Object collection = args[0];
            if (collection == null) {
                return null;
            }

            if (collection instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) collection;
                return map.get(args[1]);
            }

            if (collection.getClass().isArray()) {
                int index = toInt(args[1]);
                int arrayLength = Array.getLength(collection);
                if (index >= 0 && index < arrayLength) {
                    return Array.get(collection, index);
                }
                return null;
            }

            if (collection instanceof List) {
                List<?> list = (List<?>) collection;
                int index = toInt(args[1]);
                if (index >= 0 && index < list.size()) {
                    return list.get(index);
                }
                return null;
            }

            if (collection instanceof Collection) {
                Collection<?> values = (Collection<?>) collection;
                int index = toInt(args[1]);
                if (index < 0 || index >= values.size()) {
                    return null;
                }
                int current = 0;
                for (Object value : values) {
                    if (current == index) {
                        return value;
                    }
                    current++;
                }
                return null;
            }

            if (collection instanceof String) {
                String str = (String) collection;
                int index = toInt(args[1]);
                if (index >= 0 && index < str.length()) {
                    return String.valueOf(str.charAt(index));
                }
                return "";
            }

            throw new IllegalArgumentException("index: invalid type " + collection.getClass().getName());
        };
    }

    private static Function slice() {
        return args -> {
            if (args.length < 3) {
                throw new IllegalArgumentException("slice requires at least 3 arguments: collection, start, end");
            }
            Object collection = args[0];
            int start = toInt(args[1]);
            int end = toInt(args[2]);

            if (collection instanceof String) {
                String str = (String) collection;
                if (start < 0) start = 0;
                if (end > str.length()) end = str.length();
                if (start >= end) return "";
                return str.substring(start, end);
            }

            if (collection.getClass().isArray()) {
                int arrayLength = Array.getLength(collection);
                if (start < 0) start = 0;
                if (end > arrayLength) end = arrayLength;
                if (start >= end) {
                    return Array.newInstance(collection.getClass().getComponentType(), 0);
                }
                int length = end - start;
                Object newArray = Array.newInstance(collection.getClass().getComponentType(), length);
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(collection, start, newArray, 0, length);
                return newArray;
            }

            if (collection instanceof List) {
                List<?> list = (List<?>) collection;
                if (start < 0) start = 0;
                if (end > list.size()) end = list.size();
                if (start >= end) {
                    return new ArrayList<>();
                }
                return new ArrayList<>(list.subList(start, end));
            }

            throw new IllegalArgumentException("slice: invalid type " + collection.getClass().getName());
        };
    }

    // Utility functions
    private static Function call() {
        return args -> {
            if (args.length < 1) {
                throw new IllegalArgumentException("call requires at least 1 argument");
            }
            if (!(args[0] instanceof Function)) {
                throw new IllegalArgumentException("call: first argument must be a function");
            }
            Function func = (Function) args[0];
            Object[] funcArgs = new Object[args.length - 1];
            System.arraycopy(args, 1, funcArgs, 0, funcArgs.length);
            return func.invoke(funcArgs);
        };
    }

    private static Function html() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("html requires exactly 1 argument");
            }
            if (args[0] == null) {
                return "";
            }
            String str = String.valueOf(args[0]);
            return escapeHtml(str);
        };
    }

    private static Function js() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("js requires exactly 1 argument");
            }
            if (args[0] == null) {
                return "";
            }
            String str = String.valueOf(args[0]);
            return escapeJs(str);
        };
    }

    private static Function urlquery() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("urlquery requires exactly 1 argument");
            }
            if (args[0] == null) {
                return "";
            }
            String str = String.valueOf(args[0]);
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
        };
    }

    // Additional functions (Phase 2.3)
    private static Function deepEqual() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("deepEqual requires exactly 2 arguments");
            }
            return deepEquals(args[0], args[1]);
        };
    }

    private static Function typeof() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("typeof requires exactly 1 argument");
            }
            if (args[0] == null) {
                return "nil";
            }
            return args[0].getClass().getName();
        };
    }

    private static Function kindOf() {
        return args -> {
            if (args.length != 1) {
                throw new IllegalArgumentException("kindOf requires exactly 1 argument");
            }
            if (args[0] == null) {
                return "invalid";
            }
            return getKind(args[0]);
        };
    }

    // Helper methods
    private static boolean equal(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        return a.equals(b);
    }

    /**
     * Determines if a value is falsy according to Go template semantics.
     * <p>
     * Falsy values include:
     * <ul>
     *   <li>null</li>
     *   <li>false (Boolean)</li>
     *   <li>0 or 0.0 (Number)</li>
     *   <li>empty string (String)</li>
     * </ul>
     *
     * @param obj the object to check
     * @return true if the value is falsy, false otherwise
     */
    private static boolean isFalsy(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Boolean) {
            return !(Boolean) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue() == 0;
        }
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        }
        return false;
    }

    private static boolean compare(Object[] args, int expectedSign) {
        return compare(args, expectedSign, false);
    }

    private static boolean compare(Object[] args, int expectedSign, boolean reverseForGe) {
        if (args.length < 2) {
            return false;
        }

        for (int i = 0; i < args.length - 1; i++) {
            int cmp = compareValues(args[i], args[i + 1]);
            if (reverseForGe) {
                // For ge, we want cmp >= 0 (a >= b)
                if (cmp < 0) {
                    return false;
                }
            } else if (expectedSign == -1 && cmp >= 0) {
                // For lt, we want cmp < 0 (a < b)
                return false;
            } else if (expectedSign == 1 && cmp <= 0) {
                // For gt, we want cmp > 0 (a > b)
                return false;
            } else if (expectedSign == 0 && cmp > 0) {
                // For le, we want cmp <= 0 (a <= b)
                return false;
            }
        }
        return true;
    }

    private static int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            return Double.compare(da, db);
        }
        if (a instanceof String && b instanceof String) {
            return ((String) a).compareTo((String) b);
        }
        throw new IllegalArgumentException("incompatible types for comparison: " +
                a.getClass().getName() + " and " + b.getClass().getName());
    }

    private static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        // Try to convert other types
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String escapeHtml(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeJs(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 32 || c > 126) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static boolean deepEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (!a.getClass().equals(b.getClass())) {
            return false;
        }
        if (a.getClass().isArray()) {
            int len = Array.getLength(a);
            if (len != Array.getLength(b)) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (!deepEquals(Array.get(a, i), Array.get(b, i))) {
                    return false;
                }
            }
            return true;
        }
        return a.equals(b);
    }

    private static String getKind(Object obj) {
        Class<?> clazz = obj.getClass();
        if (clazz.isArray()) {
            return "array";
        }
        if (obj instanceof String) {
            return "string";
        }
        if (obj instanceof Boolean) {
            return "bool";
        }
        if (obj instanceof Number) {
            return "int"; // Simplified - Go distinguishes int/float/complex
        }
        if (obj instanceof Map) {
            return "map";
        }
        if (obj instanceof Collection) {
            return "slice";
        }
        return "struct";
    }

    // Null-safety functions (Phase 2.2.4)
    private static Function defaultValue() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("default requires exactly 2 arguments");
            }
            Object value = args[0];
            Object defaultValue = args[1];

            // Return default value if the first argument is null or falsy
            if (isFalsy(value)) {
                return defaultValue;
            }
            return value;
        };
    }

}
