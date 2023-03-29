package io.github.verils.gotemplate;

public class TemplateException extends Exception {

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

}
