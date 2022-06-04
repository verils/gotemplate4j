package io.github.verils.gotemplate;

public class GoTemplateExecutionException extends GoTemplateException {

    public GoTemplateExecutionException(String message) {
        super(message);
    }

    public GoTemplateExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
