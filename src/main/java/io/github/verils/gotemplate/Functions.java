package io.github.verils.gotemplate;

import io.github.verils.gotemplate.internal.lang.ComparisonUtils;
import io.github.verils.gotemplate.internal.lang.CollectionAccessUtils;
import io.github.verils.gotemplate.internal.lang.PrintFormatUtils;
import io.github.verils.gotemplate.internal.lang.Truthiness;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Functions {

    private static final Map<String, Function> BUILTIN_FUNCTIONS = createBuiltinFunctions();

    /**
     * Immutable view of the default function registry.
     */
    public static final Map<String, Function> BUILTIN =
            Collections.unmodifiableMap(BUILTIN_FUNCTIONS);

    private static Map<String, Function> createBuiltinFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("call", call());
        functions.put("html", html());
        functions.put("index", index());
        functions.put("slice", slice());
        functions.put("js", js());
        functions.put("len", len());
        functions.put("print", print());
        functions.put("printf", printf());
        functions.put("println", println());
        functions.put("urlquery", urlquery());

        // Logical operations
        functions.put("and", Functions::and);
        functions.put("or", Functions::or);
        functions.put("not", Functions::not);

        // Comparisons
        functions.put("eq", eq());
        functions.put("ge", ge());
        functions.put("gt", gt());
        functions.put("le", le());
        functions.put("lt", lt());
        functions.put("ne", ne());

        // Additional functions (Phase 2.3)
        functions.put("deepEqual", deepEqual());
        functions.put("typeof", typeof());
        functions.put("kindOf", kindOf());

        // Null-safety functions (Phase 2.2.4)
        functions.put("default", defaultValue());
        return functions;
    }

    private static Function print() {
        return PrintFormatUtils::print;
    }

    private static Function printf() {
        return PrintFormatUtils::printf;
    }

    private static Function println() {
        return PrintFormatUtils::println;
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
            Object current = args[0];
            for (int i = 1; i < args.length && current != null; i++) {
                current = CollectionAccessUtils.index(current, args[i]);
            }
            return current;
        };
    }

    private static Function slice() {
        return args -> {
            if (args.length < 1) {
                throw new IllegalArgumentException("slice requires at least 1 argument");
            }
            Object[] indexes = new Object[args.length - 1];
            System.arraycopy(args, 1, indexes, 0, indexes.length);
            return CollectionAccessUtils.slice(args[0], indexes);
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
        return deepEquals(a, b, new IdentityHashMap<>());
    }

    private static boolean deepEquals(Object a, Object b,
                                      IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> visited) {
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
            if (wasVisited(a, b, visited)) {
                return true;
            }
            int len = Array.getLength(a);
            if (len != Array.getLength(b)) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (!deepEquals(Array.get(a, i), Array.get(b, i), visited)) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof Map) {
            if (wasVisited(a, b, visited)) {
                return true;
            }
            Map<?, ?> mapA = (Map<?, ?>) a;
            Map<?, ?> mapB = (Map<?, ?>) b;
            if (mapA.size() != mapB.size()) {
                return false;
            }

            List<Map.Entry<?, ?>> remainingEntries = new ArrayList<>(mapB.entrySet());
            for (Map.Entry<?, ?> entryA : mapA.entrySet()) {
                boolean matched = false;
                for (int i = 0; i < remainingEntries.size(); i++) {
                    Map.Entry<?, ?> entryB = remainingEntries.get(i);
                    IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> branch =
                            copyVisited(visited);
                    if (deepEquals(entryA.getKey(), entryB.getKey(), branch)
                            && deepEquals(entryA.getValue(), entryB.getValue(), branch)) {
                        visited = branch;
                        remainingEntries.remove(i);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof Set) {
            if (wasVisited(a, b, visited)) {
                return true;
            }
            Collection<?> setA = (Collection<?>) a;
            Collection<?> setB = (Collection<?>) b;
            if (setA.size() != setB.size()) {
                return false;
            }

            List<Object> remaining = new ArrayList<>(setB);
            for (Object valueA : setA) {
                boolean matched = false;
                for (int i = 0; i < remaining.size(); i++) {
                    IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> branch =
                            copyVisited(visited);
                    if (deepEquals(valueA, remaining.get(i), branch)) {
                        visited = branch;
                        remaining.remove(i);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        }
        if (a instanceof Collection) {
            if (wasVisited(a, b, visited)) {
                return true;
            }
            Collection<?> collectionA = (Collection<?>) a;
            Collection<?> collectionB = (Collection<?>) b;
            if (collectionA.size() != collectionB.size()) {
                return false;
            }

            java.util.Iterator<?> iteratorA = collectionA.iterator();
            java.util.Iterator<?> iteratorB = collectionB.iterator();
            while (iteratorA.hasNext() && iteratorB.hasNext()) {
                if (!deepEquals(iteratorA.next(), iteratorB.next(), visited)) {
                    return false;
                }
            }
            return !iteratorA.hasNext() && !iteratorB.hasNext();
        }
        return a.equals(b);
    }

    private static boolean wasVisited(Object a, Object b,
                                      IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> visited) {
        IdentityHashMap<Object, Boolean> compared = visited.get(a);
        if (compared != null && compared.containsKey(b)) {
            return true;
        }
        if (compared == null) {
            compared = new IdentityHashMap<>();
            visited.put(a, compared);
        }
        compared.put(b, Boolean.TRUE);
        return false;
    }

    private static IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> copyVisited(
            IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> visited) {
        IdentityHashMap<Object, IdentityHashMap<Object, Boolean>> copy = new IdentityHashMap<>();
        for (Map.Entry<Object, IdentityHashMap<Object, Boolean>> entry : visited.entrySet()) {
            copy.put(entry.getKey(), new IdentityHashMap<>(entry.getValue()));
        }
        return copy;
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
