package io.github.verils.gotemplate;

/**
 * Exception thrown when a template contains syntax errors or cannot be parsed.
 * <p>
 * This exception includes detailed information about the parsing error, including
 * line and column numbers where the error occurred (when available).
 * <p>
 * Common causes:
 * <ul>
 *   <li>Unclosed actions: {{if .Condition without {{end}}</li>
 *   <li>Undefined variables: {{$undefinedVar}}</li>
 *   <li>Invalid syntax: {{.Field invalid syntax}}</li>
 *   <li>Mismatched delimiters: {{if}}...{{else}} without {{end}}</li>
 * </ul>
 *
 * @see Template#parse(String) for parsing templates that may throw this exception
 */

public class TemplateParseException extends TemplateException {

    private final int line;
    private final int column;

    /**
     * Creates a new template parse exception without location information.
     *
     * @param message the error message
     */
    public TemplateParseException(String message) {
        this(message, -1, -1);
    }

    /**
     * Creates a new template parse exception with location information.
     *
     * @param message the error message
     * @param line    the line number where the error occurred (1-based), or -1 if unknown
     * @param column  the column number where the error occurred (1-based), or -1 if unknown
     */
    public TemplateParseException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    /**
     * Creates a new template parse exception with a cause and without location information.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public TemplateParseException(String message, Throwable cause) {
        this(message, -1, -1, cause);
    }

    /**
     * Creates a new template parse exception with a cause and location information.
     *
     * @param message the error message
     * @param line    the line number where the error occurred (1-based), or -1 if unknown
     * @param column  the column number where the error occurred (1-based), or -1 if unknown
     * @param cause   the underlying cause
     */
    public TemplateParseException(String message, int line, int column, Throwable cause) {
        super(message, cause);
        this.line = line;
        this.column = column;
    }

    /**
     * Gets the line number where the parse error occurred.
     *
     * @return the line number (1-based), or -1 if location is unknown
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number where the parse error occurred.
     *
     * @return the column number (1-based), or -1 if location is unknown
     */
    public int getColumn() {
        return column;
    }

}
