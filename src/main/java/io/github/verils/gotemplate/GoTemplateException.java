package io.github.verils.gotemplate;

public class GoTemplateException extends RuntimeException {

    public GoTemplateException(String message) {
        super(message);
    }

    public GoTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
