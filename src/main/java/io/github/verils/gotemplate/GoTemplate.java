package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;
import io.github.verils.gotemplate.parse.Parser;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class GoTemplate {

    private final String name;
    private final Parser parser;

    public GoTemplate(String name) {
        this.name = name;
        this.parser = new Parser();
    }

    public GoTemplate(String name, Map<String, Function> functions) {
        this.name = name;
        this.parser = new Parser(functions);
    }


    public void parse(String template) {
        parser.parse(name, template);
    }

    public void execute(Object data, Writer writer) throws IOException {
        Executor executor = new Executor(parser);
        executor.execute(writer, name, data);
    }

}
