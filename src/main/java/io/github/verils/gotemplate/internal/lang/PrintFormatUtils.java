package io.github.verils.gotemplate.internal.lang;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements the supported Go-style print, println, and printf behavior.
 */
public final class PrintFormatUtils {

    private static final String FLAGS = "-+ 0#";
    private static final String VERBS = "vTtqdsxXf";

    private PrintFormatUtils() {
    }

    /**
     * Formats values using {@code fmt.Sprint}-compatible spacing.
     *
     * @param args values to print
     * @return the rendered values
     */
    public static String print(Object[] args) {
        StringBuilder result = new StringBuilder();
        boolean previousWasString = false;
        for (int i = 0; i < args.length; i++) {
            boolean currentIsString = args[i] instanceof CharSequence;
            if (i > 0 && !previousWasString && !currentIsString) {
                result.append(' ');
            }
            result.append(defaultValue(args[i]));
            previousWasString = currentIsString;
        }
        return result.toString();
    }

    /**
     * Formats values using {@code fmt.Sprintln}-compatible spacing.
     *
     * @param args values to print
     * @return the rendered values followed by a newline
     */
    public static String println(Object[] args) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(defaultValue(args[i]));
        }
        return result.append('\n').toString();
    }

    /**
     * Applies a validated subset of Go-style formatting verbs.
     *
     * @param args format string followed by values
     * @return the formatted string
     * @throws IllegalArgumentException if the format or an argument is invalid
     */
    public static String printf(Object[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("printf requires a format string");
        }
        if (!(args[0] instanceof String)) {
            throw new IllegalArgumentException("printf format must be a string");
        }

        String format = (String) args[0];
        StringBuilder result = new StringBuilder(format.length());
        int argumentIndex = 1;
        for (int i = 0; i < format.length(); ) {
            char current = format.charAt(i);
            if (current != '%') {
                result.append(current);
                i++;
                continue;
            }

            int specificationStart = i;
            i++;
            if (i >= format.length()) {
                throw invalidFormat(format.substring(specificationStart), "incomplete format specifier");
            }
            if (format.charAt(i) == '%') {
                result.append('%');
                i++;
                continue;
            }

            StringBuilder flags = new StringBuilder();
            while (i < format.length() && FLAGS.indexOf(format.charAt(i)) >= 0) {
                char flag = format.charAt(i);
                if (flags.indexOf(String.valueOf(flag)) >= 0) {
                    throw invalidFormat(format.substring(specificationStart, i + 1), "duplicate flag");
                }
                flags.append(flag);
                i++;
            }
            if (i < format.length() && format.charAt(i) == '[') {
                throw invalidFormat(format.substring(specificationStart, i + 1), "argument indexes are not supported");
            }

            String width = parseNumber(format, i, specificationStart);
            i += width.length();
            if (i < format.length() && format.charAt(i) == '*') {
                throw invalidFormat(format.substring(specificationStart, i + 1), "dynamic width is not supported");
            }

            String precision = "";
            if (i < format.length() && format.charAt(i) == '.') {
                i++;
                precision = parseRequiredNumber(format, i, specificationStart, "precision");
                i += precision.length();
                precision = "." + precision;
                if (i < format.length() && format.charAt(i) == '*') {
                    throw invalidFormat(format.substring(specificationStart, i + 1), "dynamic precision is not supported");
                }
            }
            if (i < format.length() && format.charAt(i) == '[') {
                throw invalidFormat(format.substring(specificationStart, i + 1), "argument indexes are not supported");
            }
            if (i >= format.length()) {
                throw invalidFormat(format.substring(specificationStart), "incomplete format specifier");
            }

            char verb = format.charAt(i++);
            String specification = format.substring(specificationStart, i);
            if (VERBS.indexOf(verb) < 0) {
                throw invalidFormat(specification, "unsupported verb '" + verb + "'");
            }
            if (argumentIndex >= args.length) {
                throw invalidFormat(specification, "missing argument " + argumentIndex);
            }

            Object argument = args[argumentIndex++];
            String stringSpecification = "%" + flags + width + precision + "s";
            switch (verb) {
                case 'v':
                    result.append(formatString(stringSpecification, defaultValue(argument)));
                    break;
                case 'T':
                    result.append(formatString(stringSpecification,
                            argument == null ? "<nil>" : argument.getClass().getName()));
                    break;
                case 'q':
                    result.append(formatString(stringSpecification, quote(stringArgument(argument, specification))));
                    break;
                case 's':
                    result.append(formatString(stringSpecification, stringArgument(argument, specification)));
                    break;
                case 't':
                    requireType(argument instanceof Boolean, specification, "boolean", argument);
                    result.append(formatString(stringSpecification, String.valueOf(argument)));
                    break;
                case 'd':
                case 'x':
                case 'X':
                    requireType(isIntegral(argument), specification, "integer", argument);
                    result.append(formatNative(specification, normalizeIntegral(argument)));
                    break;
                case 'f':
                    requireType(isFloating(argument), specification, "floating-point", argument);
                    result.append(formatNative(specification, argument));
                    break;
                default:
                    throw new IllegalStateException("unexpected printf verb: " + verb);
            }
        }

        if (argumentIndex < args.length) {
            throw new IllegalArgumentException("printf received " + (args.length - argumentIndex)
                    + " extra argument(s)");
        }
        return result.toString();
    }

    /**
     * Renders a value using the default {@code %v} representation.
     *
     * @param value value to render
     * @return the default representation
     */
    public static String defaultValue(Object value) {
        value = unwrapOptional(value);
        if (value == null) {
            return "<nil>";
        }
        if (value instanceof Object[]) {
            return Arrays.deepToString((Object[]) value);
        }
        if (value.getClass().isArray()) {
            return arrayToString(value);
        }
        return String.valueOf(value);
    }

    private static String formatNative(String specification, Object value) {
        try {
            return String.format(Locale.ROOT, specification, value);
        } catch (IllegalFormatException e) {
            throw invalidFormat(specification, e.getMessage());
        }
    }

    private static String formatString(String specification, String value) {
        try {
            return String.format(Locale.ROOT, specification, value);
        } catch (IllegalFormatException e) {
            throw invalidFormat(specification, e.getMessage());
        }
    }

    private static String stringArgument(Object value, String specification) {
        if (value instanceof CharSequence || value instanceof Character) {
            return String.valueOf(value);
        }
        throw invalidFormat(specification, "expected string, got " + typeName(value));
    }

    private static void requireType(boolean valid, String specification, String expected, Object value) {
        if (!valid) {
            throw invalidFormat(specification, "expected " + expected + ", got " + typeName(value));
        }
    }

    private static boolean isIntegral(Object value) {
        return value instanceof BigInteger || value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long
                || value instanceof AtomicInteger || value instanceof AtomicLong;
    }

    private static boolean isFloating(Object value) {
        return value instanceof Float || value instanceof Double || value instanceof BigDecimal;
    }

    private static Object normalizeIntegral(Object value) {
        if (value instanceof AtomicInteger || value instanceof AtomicLong) {
            return ((Number) value).longValue();
        }
        return value;
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    private static String parseNumber(String format, int start, int specificationStart) {
        int end = start;
        while (end < format.length() && Character.isDigit(format.charAt(end))) {
            end++;
        }
        return parseNumber(format, start, end, specificationStart, "width", false);
    }

    private static String parseRequiredNumber(String format, int start, int specificationStart, String name) {
        int end = start;
        while (end < format.length() && Character.isDigit(format.charAt(end))) {
            end++;
        }
        return parseNumber(format, start, end, specificationStart, name, true);
    }

    private static String parseNumber(String format, int start, int end, int specificationStart,
                                      String name, boolean required) {
        String number = format.substring(start, end);
        if (required && number.isEmpty()) {
            throw invalidFormat(format.substring(specificationStart), "missing " + name);
        }
        if (!number.isEmpty()) {
            try {
                Integer.parseInt(number);
            } catch (NumberFormatException e) {
                throw invalidFormat(format.substring(specificationStart), name + " is too large");
            }
        }
        return number;
    }

    private static String arrayToString(Object array) {
        if (array instanceof boolean[]) {
            return Arrays.toString((boolean[]) array);
        }
        if (array instanceof byte[]) {
            return Arrays.toString((byte[]) array);
        }
        if (array instanceof short[]) {
            return Arrays.toString((short[]) array);
        }
        if (array instanceof int[]) {
            return Arrays.toString((int[]) array);
        }
        if (array instanceof long[]) {
            return Arrays.toString((long[]) array);
        }
        if (array instanceof float[]) {
            return Arrays.toString((float[]) array);
        }
        if (array instanceof double[]) {
            return Arrays.toString((double[]) array);
        }
        if (array instanceof char[]) {
            return Arrays.toString((char[]) array);
        }
        return "[" + Array.get(array, 0) + "]";
    }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder(value.length() + 2).append('"');
        for (int offset = 0; offset < value.length(); ) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            switch (codePoint) {
                case '\u0007':
                    result.append("\\a");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                case '\u000B':
                    result.append("\\v");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '"':
                    result.append("\\\"");
                    break;
                default:
                    appendQuotedCodePoint(result, codePoint);
                    break;
            }
        }
        return result.append('"').toString();
    }

    private static void appendQuotedCodePoint(StringBuilder result, int codePoint) {
        if (isGoPrintable(codePoint)) {
            result.appendCodePoint(codePoint);
            return;
        }
        if (codePoint < 0x80) {
            result.append(String.format(Locale.ROOT, "\\x%02x", codePoint));
        } else if (codePoint <= 0xFFFF) {
            result.append(String.format(Locale.ROOT, "\\u%04x", codePoint));
        } else {
            result.append(String.format(Locale.ROOT, "\\U%08x", codePoint));
        }
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

    private static Object unwrapOptional(Object value) {
        return value instanceof Optional ? ((Optional<?>) value).orElse(null) : value;
    }

    private static IllegalArgumentException invalidFormat(String specification, String detail) {
        return new IllegalArgumentException("printf: invalid format specifier '" + specification + "': " + detail);
    }
}
