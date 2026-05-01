package io.github.verils.gotemplate;

/**
 * Base class for all template-related exceptions.
 * <p>
 * This exception hierarchy covers errors that can occur during template parsing and execution.
 *
 * @see TemplateParseException for syntax errors during parsing
 * @see TemplateExecutionException for runtime errors during execution
 * @see TemplateNotFoundException for missing template references
 */

public class TemplateException extends Exception {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
