package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.Parser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GoTemplateFactory {

    private final Map<String, GoTemplate> templates;

    private final Map<String, Function> functions;

    /**
     * Factory constructor, using default settings
     */
    public GoTemplateFactory() {
        this(null);
    }

    /**
     * Factory constructor, you can configure the available functions
     *
     * @param functions The functions map, will merge the builtin functions
     */
    public GoTemplateFactory(Map<String, Function> functions) {
        this.templates = new HashMap<>();

        LinkedHashMap<String, Function> map = new LinkedHashMap<>(Functions.BUILT_IN);
        if (functions != null) {
            map.putAll(functions);
        }
        this.functions = Collections.unmodifiableMap(map);
    }


    /**
     * Parse an unnamed template, it will be treated as a library template
     *
     * @param text Template text
     */
    public void parse(String text) {
        parse("", text);
    }

    /**
     * Parse a named template, then you can obtain the template using
     * {@link GoTemplateFactory#getTemplate(String)} by its name
     *
     * @param name The template name
     * @param text Template text
     */
    public void parse(String name, String text) {
        Parser parser = new Parser(this);
        parser.parse(name, text);
    }

    /**
     * Parse a named template from a {@link Reader}, then you can obtain the template using
     * {@link GoTemplateFactory#getTemplate(String)} by its name
     *
     * @param name   The template name
     * @param reader Template text reader
     * @throws IOException if fail on reading the content
     */
    public void parse(String name, Reader reader) throws IOException {
        String text = IOUtils.toString(reader);
        parse(name, text);
    }

    /**
     * Directly add a template in the pool
     *
     * @param template Parsed go template
     */
    public void putTemplate(GoTemplate template) {
        templates.put(template.getName(), template);
    }

    /**
     * Get a named template
     *
     * @param name Template name
     * @return the template with the name
     * @throws TemplateNotFoundException if template is missing. Did you put or parse it?
     */
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
