package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
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


    public void parse(String name, String text) {
        GoTemplate goTemplate = templates.computeIfAbsent(name, __ -> new GoTemplate(this, name));
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

    public Map<String, Function> getFunctions() {
        return functions;
    }
}
