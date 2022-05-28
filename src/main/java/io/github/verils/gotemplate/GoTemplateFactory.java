package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class GoTemplateFactory {

    private final Map<String, GoTemplate> templates;

    private final Map<String, Function> functions;

    public GoTemplateFactory() {
        this(null);
    }

    public GoTemplateFactory(Map<String, Function> functions) {
        this.templates = new HashMap<>();
        this.functions = functions;
    }


    public void parse(String name, String text) {
        GoTemplate goTemplate = templates.get(name);
        if (goTemplate == null) {
            goTemplate = functions != null ? new GoTemplate(name, functions) : new GoTemplate(name);
            templates.put(name, goTemplate);
        }
        goTemplate.parse(text);
    }

    public void parse(String name, Reader reader) throws IOException {
        String text = IOUtils.toString(reader);
        parse(name, text);
    }

    public GoTemplate getTemplate(String name) {
        GoTemplate goTemplate = templates.get(name);
        if (goTemplate == null) {
            throw new TemplateNotFoundException(String.format("Template '%s' not found.", name));
        }
        return goTemplate;
    }
}
