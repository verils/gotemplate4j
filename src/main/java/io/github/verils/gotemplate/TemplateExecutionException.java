package io.github.verils.gotemplate;

/**
 * Exception thrown when template execution fails at runtime.
 * <p>
 * This exception occurs during the execution phase after successful parsing.
 * Common causes include:
 * <ul>
 *   <li>Accessing non-existent fields on data objects</li>
 *   <li>Function invocation errors (wrong argument types/count)</li>
 *   <li>Type conversion failures in pipelines</li>
 *   <li>Null pointer access in template actions</li>
 * </ul>
 *
 * @see Template#execute(java.io.Writer, Object) for execution that may throw this exception
 */

public class TemplateExecutionException extends TemplateException {

    public TemplateExecutionException(String message) {
        super(message);
    }

    public TemplateExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
