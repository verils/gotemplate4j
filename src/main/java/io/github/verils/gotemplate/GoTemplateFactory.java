package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.Parser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GoTemplateFactory {

    private final Map<String, GoTemplate> templates;

    private final Map<String, Function> functions;

    public GoTemplateFactory() {
        this(null);
    }

    public GoTemplateFactory(Map<String, Function> functions) {
        this.templates = new HashMap<>();

        LinkedHashMap<String, Function> map = new LinkedHashMap<>(Functions.BUILT_IN);
        if (functions != null) {
            map.putAll(functions);
        }
        this.functions = map;
    }


    public void parse(String text) {
        parse("", text);
    }

    public void parse(String name, String text) {
        Parser parser = new Parser(this);
        parser.parse(name, text);
    }

    public void parse(String name, Reader reader) throws IOException {
        String text = IOUtils.toString(reader);
        parse(name, text);
    }

    public void putTemplate(GoTemplate template) {
        templates.put(template.getName(), template);
    }

    public GoTemplate getTemplate(String name) throws TemplateNotFoundException {
        GoTemplate template = templates.get(name);
        if (template == null) {
            throw new TemplateNotFoundException(String.format("Template '%s' not found.", name));
        }
        return template;
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }
}
