package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Function;
import com.github.verils.gotemplate.parse.Parser;

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

    public void execute(Writer writer, Object data) throws IOException {
        Executor executor = new Executor(parser);
        executor.execute(writer, name, data);
    }

}
