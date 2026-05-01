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

    public TemplateParseException(String message) {
        super(message);
    }

    public TemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
