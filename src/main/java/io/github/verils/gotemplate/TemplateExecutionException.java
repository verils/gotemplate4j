package io.github.verils.gotemplate;

public class TemplateExecutionException extends GoTemplateException {

    public TemplateExecutionException(String message) {
        super(message);
    }

    public TemplateExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
