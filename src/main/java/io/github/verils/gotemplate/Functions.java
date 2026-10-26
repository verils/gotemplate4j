package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.lang.ComparisonUtils;
import io.github.verils.gotemplate.internal.lang.Truthiness;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
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
                if (ComparisonUtils.equal(first, args[i])) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Function ne() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("ne requires exactly 2 arguments");
            }
            return !ComparisonUtils.equal(args[0], args[1]);
        };
    }

    private static Function lt() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("lt requires exactly 2 arguments");
            }
            return ComparisonUtils.lessThan(args[0], args[1]);
        };
    }

    private static Function le() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("le requires exactly 2 arguments");
            }
            return ComparisonUtils.lessThanOrEqual(args[0], args[1]);
        };
    }

    private static Function gt() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("gt requires exactly 2 arguments");
            }
            return ComparisonUtils.greaterThan(args[0], args[1]);
        };
    }

    private static Function ge() {
        return args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("ge requires exactly 2 arguments");
            }
            return ComparisonUtils.greaterThanOrEqual(args[0], args[1]);
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
        if (args.length != 1) {
            throw new IllegalArgumentException("not requires exactly 1 argument");
        }
        return Truthiness.isFalsy(args[0]);
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
            String str = formatArguments(args);
            return escapeHtml(str);
        };
    }

    private static Function js() {
        return args -> {
            String str = formatArguments(args);
            return escapeJs(str);
        };
    }

    private static Function urlquery() {
        return args -> {
            String str = formatArguments(args);
            return escapeUrlQuery(str);
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
                case '\0':
                    sb.append('\uFFFD');
                    break;
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
                    sb.append("&#34;");
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
        for (int i = 0; i < str.length(); ) {
            int codePoint = str.codePointAt(i);
            if (codePoint < 0x80) {
                switch (codePoint) {
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\'':
                        sb.append("\\'");
                        break;
                    case '<':
                        sb.append("\\u003C");
                        break;
                    case '>':
                        sb.append("\\u003E");
                        break;
                    case '&':
                        sb.append("\\u0026");
                        break;
                    case '=':
                        sb.append("\\u003D");
                        break;
                    default:
                        if (codePoint < ' ') {
                            appendUnicodeEscape(sb, codePoint);
                        } else {
                            sb.append((char) codePoint);
                        }
                }
            } else if (isGoPrintable(codePoint)) {
                sb.appendCodePoint(codePoint);
            } else {
                appendUnicodeEscape(sb, codePoint);
            }
            i += Character.charCount(codePoint);
        }
        return sb.toString();
    }

    private static String escapeUrlQuery(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte value : bytes) {
            int c = value & 0xFF;
            if (isUnreservedUrlCharacter(c)) {
                sb.append((char) c);
            } else if (c == ' ') {
                sb.append('+');
            } else {
                sb.append('%');
                sb.append(Character.toUpperCase(Character.forDigit(c >> 4, 16)));
                sb.append(Character.toUpperCase(Character.forDigit(c & 0x0F, 16)));
            }
        }
        return sb.toString();
    }

    private static boolean isUnreservedUrlCharacter(int c) {
        return c >= 'a' && c <= 'z'
                || c >= 'A' && c <= 'Z'
                || c >= '0' && c <= '9'
                || c == '-' || c == '.' || c == '_' || c == '~';
    }

    private static boolean isGoPrintable(int codePoint) {
        if (codePoint == ' ') {
            return true;
        }
        switch (Character.getType(codePoint)) {
            case Character.UPPERCASE_LETTER:
            case Character.LOWERCASE_LETTER:
            case Character.TITLECASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.OTHER_LETTER:
            case Character.NON_SPACING_MARK:
            case Character.ENCLOSING_MARK:
            case Character.COMBINING_SPACING_MARK:
            case Character.DECIMAL_DIGIT_NUMBER:
            case Character.LETTER_NUMBER:
            case Character.OTHER_NUMBER:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.OTHER_SYMBOL:
                return true;
            default:
                return false;
        }
    }

    private static void appendUnicodeEscape(StringBuilder sb, int codePoint) {
        sb.append("\\u");
        String hex = Integer.toHexString(codePoint).toUpperCase();
        for (int i = hex.length(); i < 4; i++) {
            sb.append('0');
        }
        sb.append(hex);
    }

    private static String formatArguments(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0 && !isStringArgument(args[i - 1]) && !isStringArgument(args[i])) {
                sb.append(' ');
            }
            if (args[i] != null) {
                sb.append(args[i]);
            }
        }
        return sb.toString();
    }

    private static boolean isStringArgument(Object value) {
        return value == null || value instanceof String;
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
            if (Truthiness.isFalsy(value)) {
                return defaultValue;
            }
            return value;
        };
    }

}
