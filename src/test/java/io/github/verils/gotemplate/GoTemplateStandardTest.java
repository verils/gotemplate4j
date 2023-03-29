package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These test cases are the same as those in go standard library
 */
class GoTemplateStandardTest {

    @Test
    void test() throws IOException, TemplateParseException, TemplateNotFoundException, TemplateExecutionException {
        String letter = "\n" +
                "Dear {{.Name}},\n" +
                "{{if .Attended}}\n" +
                "It was a pleasure to see you at the wedding.\n" +
                "{{- else}}\n" +
                "It is a shame you couldn't make it to the wedding.\n" +
                "{{- end}}\n" +
                "{{with .Gift -}}\n" +
                "Thank you for the lovely {{.}}.\n" +
                "{{end}}\n" +
                "Best wishes,\n" +
                "Josie\n";


        GoTemplateFactory goTemplateFactory = new GoTemplateFactory();
        goTemplateFactory.parse("letter", letter);


        GoTemplate goTemplate = goTemplateFactory.getTemplate("letter");


        Writer writer = new StringWriter();
        goTemplate.execute(new Recipient("Aunt Mildred", "bone china tea set", true), writer);

        assertEquals(
                "\n" +
                        "Dear Aunt Mildred,\n" +
                        "\n" +
                        "It was a pleasure to see you at the wedding.\n" +
                        "Thank you for the lovely bone china tea set.\n" +
                        "\n" +
                        "Best wishes,\n" +
                        "Josie\n",
                writer.toString()
        );


        Writer writer2 = new StringWriter();
        goTemplate.execute(new Recipient("Uncle John", "moleskin pants", false), writer2);

        assertEquals(
                "\n" +
                        "Dear Uncle John,\n" +
                        "\n" +
                        "It is a shame you couldn't make it to the wedding.\n" +
                        "Thank you for the lovely moleskin pants.\n" +
                        "\n" +
                        "Best wishes,\n" +
                        "Josie\n",
                writer2.toString());


        Writer writer3 = new StringWriter();
        goTemplate.execute(new Recipient("Cousin Rodney", "", false), writer3);

        assertEquals(
                "\n" +
                        "Dear Cousin Rodney,\n" +
                        "\n" +
                        "It is a shame you couldn't make it to the wedding.\n" +
                        "\n" +
                        "Best wishes,\n" +
                        "Josie\n",
                writer3.toString());
    }


    @Test
    void testDefinition() throws IOException, TemplateParseException, TemplateNotFoundException, TemplateExecutionException {
        String masterTemplate = "Names:{{block \"list\" .}}{{\"\\n\"}}{{range .}}{{println \"-\" .}}{{end}}{{end}}";
        String overlayTemplate = "{{define \"list\"}} {{join . \", \"}}{{end}} ";

        String[] guardians = {"Gamora", "Groot", "Nebula", "Rocket", "Star-Lord"};

        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("join", args -> {
            CharSequence delimiter = (CharSequence) args[1];
            CharSequence[] elements = (CharSequence[]) args[0];
            return String.join(delimiter, elements);
        });


        GoTemplateFactory goTemplateFactory = new GoTemplateFactory(functions);
        goTemplateFactory.parse("master", masterTemplate);

        GoTemplate goTemplate = goTemplateFactory.getTemplate("master");

        Writer writer = new StringWriter();
        goTemplate.execute(guardians, writer);
        String text = writer.toString();

        assertEquals(
                "Names:\n" +
                        "- Gamora\n" +
                        "- Groot\n" +
                        "- Nebula\n" +
                        "- Rocket\n" +
                        "- Star-Lord\n",
                text
        );


        goTemplateFactory.parse(overlayTemplate);
        goTemplate = goTemplateFactory.getTemplate("master");

        writer = new StringWriter();
        goTemplate.execute(guardians, writer);
        String overlayText = writer.toString();

        assertEquals("Names: Gamora, Groot, Nebula, Rocket, Star-Lord", overlayText);
    }

    @Test
    void invoke() throws TemplateParseException, TemplateNotFoundException, IOException, TemplateExecutionException {
        GoTemplateFactory goTemplateFactory = new GoTemplateFactory();
        goTemplateFactory.parse("T0.tmpl", "T0 invokes T1: ({{template \"T1\"}})");
        goTemplateFactory.parse("T1.tmpl", "{{define \"T1\"}}T1 invokes T2: ({{template \"T2\"}}){{end}}");
        goTemplateFactory.parse("T2.tmpl", "{{define \"T2\"}}This is T2{{end}}");


        GoTemplate goTemplate = goTemplateFactory.getTemplate("T0.tmpl");

        StringWriter writer = new StringWriter();
        goTemplate.execute(null, writer);
        assertEquals("T0 invokes T1: (T1 invokes T2: (This is T2))", writer.toString());
    }
}