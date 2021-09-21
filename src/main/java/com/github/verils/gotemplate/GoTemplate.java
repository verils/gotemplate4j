package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Function;
import com.github.verils.gotemplate.parse.Parser;

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

    public String execute(Object data) {
        StringBuilder sb = new StringBuilder();

        Writer writer = new Writer(sb, parser);
        writer.write(name, data);
        return sb.toString();
    }

}
