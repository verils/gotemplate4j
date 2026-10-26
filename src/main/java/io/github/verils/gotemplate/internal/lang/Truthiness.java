package io.github.verils.gotemplate.internal.lang;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Shared Go-template-style truthiness rules for Java values.
 */
public final class Truthiness {

    private Truthiness() {
    }

    /**
     * Returns whether a value is truthy.
     *
     * @param value value to evaluate
     * @return {@code true} when the value is neither null nor an empty/zero value
     */
    public static boolean isTrue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() != 0;
        }
        if (value instanceof Number) {
            return isNonZero((Number) value);
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) != 0;
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return !((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Optional) {
            Optional<?> optional = (Optional<?>) value;
            return optional.isPresent() && isTrue(optional.get());
        }
        return true;
    }

    /**
     * Returns whether a value is falsy.
     *
     * @param value value to evaluate
     * @return {@code true} when {@link #isTrue(Object)} returns {@code false}
     */
    public static boolean isFalsy(Object value) {
        return !isTrue(value);
    }

    private static boolean isNonZero(Number value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).compareTo(BigDecimal.ZERO) != 0;
        }
        if (value instanceof BigInteger) {
            return ((BigInteger) value).signum() != 0;
        }
        if (value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long) {
            return value.longValue() != 0;
        }
        return value.doubleValue() != 0.0;
    }
}
