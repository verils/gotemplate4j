package io.github.verils.gotemplate;

import java.io.*;

/**
 * Golang-like template api
 */
public class Template {

    private final GoTemplateFactory goTemplateFactory = new GoTemplateFactory();

    private final String name;

    public Template(String name) {
        this.name = name;
    }


    public void parse(String template) throws GoTemplateParseException {
        goTemplateFactory.parse(name, template);
    }


    public void parse(InputStream in) throws GoTemplateParseException, IOException {
        goTemplateFactory.parse(name, in);
    }


    public void parse(Reader reader) throws GoTemplateParseException, IOException {
        goTemplateFactory.parse(name, reader);
    }

    public void execute(OutputStream out, Object data) throws GoTemplateException, IOException {
        GoTemplate template = goTemplateFactory.getTemplate(name);
        template.execute(data, new OutputStreamWriter(out));
    }

    public void execute(Writer writer, Object data) throws GoTemplateException, IOException {
        GoTemplate template = goTemplateFactory.getTemplate(name);
        template.execute(data, writer);
    }

    public void executeTemplate(Writer writer, String name, Object data) throws GoTemplateException, IOException {
        GoTemplate template = goTemplateFactory.getTemplate(name);
        template.execute(data, writer);
    }

}
