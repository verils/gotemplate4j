package io.github.verils.gotemplate;

/**
 * Exception thrown when a referenced template cannot be found.
 * <p>
 * This occurs when attempting to execute or include a template that has not been defined or parsed.
 * Common scenarios:
 * <ul>
 *   <li>Executing a template that was never parsed</li>
 *   <li>Using {{template "name" .}} with an undefined template name</li>
 *   <li>Calling executeTemplate() with a non-existent template name</li>
 * </ul>
 *
 * @see Template#executeTemplate(java.io.Writer, String, Object) for operations that may throw this exception
 */

public class TemplateNotFoundException extends TemplateException {

    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
