package io.github.verils.gotemplate;

public class GoTemplateNotFoundException extends GoTemplateException {

    public GoTemplateNotFoundException(String message) {
        super(message);
    }

    public GoTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
