package io.github.verils.gotemplate;

public class TemplateParseException extends GoTemplateException {

    public TemplateParseException(String message) {
        super(message);
    }

    public TemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
