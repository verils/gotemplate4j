package io.github.verils.gotemplate;

public class GoTemplateException extends Exception {

    public GoTemplateException(String message) {
        super(message);
    }

    public GoTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
