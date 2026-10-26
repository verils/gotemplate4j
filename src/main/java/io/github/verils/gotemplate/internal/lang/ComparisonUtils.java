package io.github.verils.gotemplate.internal.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Go-template-style comparison rules mapped to Java values.
 */
public final class ComparisonUtils {

    private enum Kind {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        STRING,
        OBJECT,
        AGGREGATE
    }

    private ComparisonUtils() {
    }

    /**
     * Tests equality using Go template type grouping.
     *
     * @param left  first value
     * @param right second value
     * @return whether the values are equal
     * @throws IllegalArgumentException for incompatible or non-comparable values
     */
    public static boolean equal(Object left, Object right) {
        if (isAggregate(left) || isAggregate(right)) {
            throw new IllegalArgumentException("non-comparable value for comparison: "
                    + describe(left) + " and " + describe(right));
        }
        if (left == null || right == null) {
            return left == right;
        }

        Kind leftKind = kindOf(left);
        Kind rightKind = kindOf(right);
        if (leftKind != rightKind) {
            throw incompatible(left, right);
        }

        switch (leftKind) {
            case BOOLEAN:
                return left.equals(right);
            case INTEGER:
                return integerValue(left).equals(integerValue(right));
            case DECIMAL:
                return decimalEqual((Number) left, (Number) right);
            case STRING:
                return left.toString().equals(right.toString());
            case OBJECT:
                if (left.getClass() != right.getClass()) {
                    throw incompatible(left, right);
                }
                return Objects.equals(left, right);
            default:
                throw new IllegalStateException("unexpected comparison kind: " + leftKind);
        }
    }

    /**
     * Tests ordered less-than using Go template type grouping.
     *
     * @param left  first value
     * @param right second value
     * @return whether {@code left < right}
     * @throws IllegalArgumentException for incompatible or unordered values
     */
    public static boolean lessThan(Object left, Object right) {
        if (isAggregate(left) || isAggregate(right)) {
            throw new IllegalArgumentException("non-comparable value for ordered comparison: "
                    + describe(left) + " and " + describe(right));
        }
        if (left == null || right == null) {
            throw invalidOrdered(left, right);
        }

        Kind leftKind = kindOf(left);
        Kind rightKind = kindOf(right);
        if (leftKind != rightKind) {
            throw incompatible(left, right);
        }

        switch (leftKind) {
            case INTEGER:
                return integerValue(left).compareTo(integerValue(right)) < 0;
            case DECIMAL:
                return decimalLessThan((Number) left, (Number) right);
            case STRING:
                return left.toString().compareTo(right.toString()) < 0;
            default:
                throw invalidOrdered(left, right);
        }
    }

    public static boolean lessThanOrEqual(Object left, Object right) {
        return lessThan(left, right) || equal(left, right);
    }

    public static boolean greaterThan(Object left, Object right) {
        return !lessThanOrEqual(left, right);
    }

    public static boolean greaterThanOrEqual(Object left, Object right) {
        return !lessThan(left, right);
    }

    private static Kind kindOf(Object value) {
        if (value instanceof Boolean) {
            return Kind.BOOLEAN;
        }
        if (value instanceof Character || value instanceof BigInteger
                || value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long
                || value instanceof AtomicInteger || value instanceof AtomicLong) {
            return Kind.INTEGER;
        }
        if (value instanceof Number) {
            return Kind.DECIMAL;
        }
        if (value instanceof CharSequence) {
            return Kind.STRING;
        }
        return Kind.OBJECT;
    }

    private static boolean isAggregate(Object value) {
        return value != null
                && (value.getClass().isArray() || value instanceof Collection || value instanceof Map);
    }

    private static BigInteger integerValue(Object value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof Character) {
            return BigInteger.valueOf((Character) value);
        }
        if (value instanceof AtomicInteger || value instanceof AtomicLong) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        return BigInteger.valueOf(((Number) value).longValue());
    }

    private static boolean decimalEqual(Number left, Number right) {
        if (isNaN(left) || isNaN(right)) {
            return false;
        }
        int leftInfinity = infinitySign(left);
        int rightInfinity = infinitySign(right);
        if (leftInfinity != 0 || rightInfinity != 0) {
            return leftInfinity != 0 && leftInfinity == rightInfinity;
        }
        return decimalValue(left).compareTo(decimalValue(right)) == 0;
    }

    private static boolean decimalLessThan(Number left, Number right) {
        if (isNaN(left) || isNaN(right)) {
            return false;
        }
        int leftInfinity = infinitySign(left);
        int rightInfinity = infinitySign(right);
        if (leftInfinity < 0 || rightInfinity > 0) {
            return true;
        }
        if (leftInfinity > 0 || rightInfinity < 0) {
            return false;
        }
        return decimalValue(left).compareTo(decimalValue(right)) < 0;
    }

    private static BigDecimal decimalValue(Number value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        return BigDecimal.valueOf(value.doubleValue());
    }

    private static boolean isNaN(Number value) {
        return value instanceof Float && Float.isNaN(value.floatValue())
                || value instanceof Double && Double.isNaN(value.doubleValue());
    }

    private static int infinitySign(Number value) {
        if (!(value instanceof Float) && !(value instanceof Double)) {
            return 0;
        }
        double number = value.doubleValue();
        if (Double.isInfinite(number)) {
            return number > 0 ? 1 : -1;
        }
        return 0;
    }

    private static IllegalArgumentException incompatible(Object left, Object right) {
        return new IllegalArgumentException("incompatible types for comparison: "
                + describe(left) + " and " + describe(right));
    }

    private static IllegalArgumentException invalidOrdered(Object left, Object right) {
        return new IllegalArgumentException("invalid type for ordered comparison: "
                + describe(left) + " and " + describe(right));
    }

    private static String describe(Object value) {
        return value == null ? "nil" : value.getClass().getName();
    }
}
