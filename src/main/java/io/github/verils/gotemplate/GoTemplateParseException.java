package io.github.verils.gotemplate;

public class GoTemplateParseException extends GoTemplateException {

    public GoTemplateParseException(String message) {
        super(message);
    }

    public GoTemplateParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
