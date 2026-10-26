package io.github.verils.gotemplate.internal.lang;

public final class StringEscapeUtils {

    private StringEscapeUtils() {
    }

    /**
     * Decode Go-style escape sequences without surrounding quotes.
     *
     * @param input escaped string content
     * @return decoded string
     * @throws IllegalArgumentException for invalid escape sequences
     */
    public static String unescape(String input) {
        return StringUtils.decodeEscapes(input, (char) 0);
    }
}
