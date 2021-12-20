package io.github.verils.gotemplate.lex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StringEscapeUtils {

    private static final Map<CharSequence, CharSequence> ESCAPE_MAP;
    private static final int ESCAPE_LONGEST_LENGTH;
    private static final int ESCAPE_SHORTEST_LENGTH;

    private static final Map<CharSequence, CharSequence> UNESCAPE_MAP;
    private static final int UNESCAPE_LONGEST_LENGTH;
    private static final int UNESCAPE_SHORTEST_LENGTH;

    static {
        final Map<CharSequence, CharSequence> initialMap = new LinkedHashMap<>();
        initialMap.put("\\", "\\\\");
        initialMap.put("\b", "\\b");
        initialMap.put("\n", "\\n");
        initialMap.put("\t", "\\t");
        initialMap.put("\f", "\\f");
        initialMap.put("\r", "\\r");
        ESCAPE_MAP = initialMap;
        ESCAPE_LONGEST_LENGTH = getLongestLength(initialMap.keySet());
        ESCAPE_SHORTEST_LENGTH = getShortestLength(initialMap.keySet());

        final Map<CharSequence, CharSequence> invertMap = new LinkedHashMap<>();
        for (Map.Entry<CharSequence, CharSequence> entry : initialMap.entrySet()) {
            invertMap.put(entry.getValue(), entry.getKey());
        }
        UNESCAPE_MAP = invertMap;
        UNESCAPE_LONGEST_LENGTH = getLongestLength(invertMap.keySet());
        UNESCAPE_SHORTEST_LENGTH = getShortestLength(invertMap.keySet());
    }

    private static int getLongestLength(Set<CharSequence> keys) {
        int longest = 0;
        for (CharSequence key : keys) {
            int length = key.length();
            if (longest < length) {
                longest = length;
            }
        }
        return longest;
    }

    private static int getShortestLength(Set<CharSequence> keys) {
        int shortest = Integer.MAX_VALUE;
        for (CharSequence key : keys) {
            int length = key.length();
            if (shortest > length) {
                shortest = length;
            }
        }
        return shortest;
    }

    private StringEscapeUtils() {
    }


    /**
     * Unescape string to literal in Java. For example, replace '\\n' to '\n', '\\b' to '\b', and so on.
     *
     * @param input Java style escaped string write in editor
     * @return Unescaped string including characters like '\n', '\b', '\t', etc.
     */
    public static String unescape(String input) {
        StringBuilder sb = new StringBuilder();

        int offset = 0;
        int max = input.length();
        while (offset < max) {
            int len = UNESCAPE_LONGEST_LENGTH;
            if (offset + len > max) {
                len = max - offset;
            }

            // Longest match first
            int consumed = 0;
            for (int i = len; i >= UNESCAPE_SHORTEST_LENGTH; i--) {
                String candidate = input.substring(offset, offset + len);
                CharSequence unescaped = UNESCAPE_MAP.get(candidate);

                if (unescaped != null) {
                    sb.append(unescaped);
                    offset += i;
                    consumed = i;
                    break;
                }
            }

            if (consumed == 0) {
                sb.append(input.charAt(offset));
                offset++;
            }
        }

        return sb.toString();
    }
}
