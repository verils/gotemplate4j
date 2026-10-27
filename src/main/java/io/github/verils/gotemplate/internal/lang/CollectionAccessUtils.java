package io.github.verils.gotemplate.internal.lang;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements the boundary and type rules shared by the built-in collection functions.
 */
public final class CollectionAccessUtils {

    private CollectionAccessUtils() {
    }

    /**
     * Indexes a value by one key or sequence position.
     *
     * @param value the value to index
     * @param key   the map key or zero-based sequence position
     * @return the indexed value, or {@code null} for a null/Optional value or missing map key
     */
    public static Object index(Object value, Object key) {
        value = unwrapOptional(value);
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).get(key);
        }
        if (value.getClass().isArray()) {
            int index = sequenceIndex(key, Array.getLength(value) - 1);
            return Array.get(value, index);
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.get(sequenceIndex(key, list.size() - 1));
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            int index = sequenceIndex(key, collection.size() - 1);
            int current = 0;
            for (Object item : collection) {
                if (current == index) {
                    return item;
                }
                current++;
            }
            throw new IllegalArgumentException("index out of range: " + index);
        }
        if (value instanceof String) {
            String string = (String) value;
            return String.valueOf(string.charAt(sequenceIndex(key, string.length() - 1)));
        }
        throw new IllegalArgumentException("can't index item of type " + value.getClass().getName());
    }

    /**
     * Slices an array, list, or string using Go-style optional start, end, and maximum indexes.
     *
     * @param value   the value to slice
     * @param indexes zero to three slice indexes
     * @return the sliced value
     * @throws IllegalArgumentException if the value or an index is invalid
     */
    public static Object slice(Object value, Object... indexes) {
        value = unwrapOptional(value);
        if (value == null) {
            throw new IllegalArgumentException("slice of null value");
        }
        if (indexes.length > 3) {
            throw new IllegalArgumentException("too many slice indexes: " + indexes.length);
        }

        if (value instanceof String) {
            if (indexes.length == 3) {
                throw new IllegalArgumentException("cannot 3-index slice a string");
            }
            String string = (String) value;
            int[] bounds = sliceBounds(string.length(), indexes);
            return string.substring(bounds[0], bounds[1]);
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            int[] bounds = sliceBounds(length, indexes);
            Object result = Array.newInstance(value.getClass().getComponentType(), bounds[1] - bounds[0]);
            System.arraycopy(value, bounds[0], result, 0, bounds[1] - bounds[0]);
            return result;
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            int[] bounds = sliceBounds(list.size(), indexes);
            return new ArrayList<>(list.subList(bounds[0], bounds[1]));
        }

        throw new IllegalArgumentException("can't slice item of type " + value.getClass().getName());
    }

    private static int[] sliceBounds(int length, Object[] indexes) {
        int[] bounds = {0, length, length};
        for (int i = 0; i < indexes.length; i++) {
            bounds[i] = sequenceIndex(indexes[i], length);
        }
        if (bounds[0] > bounds[1]) {
            throw new IllegalArgumentException("invalid slice index: " + bounds[0] + " > " + bounds[1]);
        }
        if (indexes.length == 3 && bounds[1] > bounds[2]) {
            throw new IllegalArgumentException("invalid slice index: " + bounds[1] + " > " + bounds[2]);
        }
        return bounds;
    }

    private static int sequenceIndex(Object value, int maximum) {
        BigInteger integer = integralValue(value);
        if (integer.signum() < 0 || integer.compareTo(BigInteger.valueOf(maximum)) > 0) {
            throw new IllegalArgumentException("index out of range: " + integer);
        }
        return integer.intValue();
    }

    private static BigInteger integralValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("cannot index slice/array with nil");
        }
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
                || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        throw new IllegalArgumentException("cannot index slice/array with type " + value.getClass().getName());
    }

    private static Object unwrapOptional(Object value) {
        return value instanceof Optional ? ((Optional<?>) value).orElse(null) : value;
    }
}
