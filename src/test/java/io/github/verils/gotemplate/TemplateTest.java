package io.github.verils.gotemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void test() throws IOException, TemplateException {
        Template template = new Template("demo");
        template.parse("{{ .Name }}");

        StringWriter writer = new StringWriter();

        User user = new User();
        user.setName("Bob");

        template.execute(writer, user);
        assertEquals("Bob", writer.toString());
    }

    @Test
    void testPipe() throws IOException, TemplateException {
        Template template = new Template("demo");
        template.parse("{{ .Name | print | print }}");

        StringWriter writer = new StringWriter();

        User user = new User();
        user.setName("Bob");

        template.execute(writer, user);
        assertEquals("Bob", writer.toString());
    }

    public static class User {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void testOfficial() throws IOException, TemplateException {
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


        Template template = new Template("letter");
        template.parse(letter);

        Writer writer = new StringWriter();
        template.execute(writer, new Recipient("Aunt Mildred", "bone china tea set", true));

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
        template.execute(writer2, new Recipient("Uncle John", "moleskin pants", false));

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
        template.execute(writer3, new Recipient("Cousin Rodney", "", false));

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
    void testDefinition() throws IOException, TemplateException {
        String masterTemplate = "Names:{{block \"list\" .}}{{\"\\n\"}}{{range .}}{{println \"-\" .}}{{end}}{{end}}";
        String overlayTemplate = "{{define \"list\"}} {{join . \", \"}}{{end}} ";

        String[] guardians = {"Gamora", "Groot", "Nebula", "Rocket", "Star-Lord"};

        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("join", args -> {
            CharSequence delimiter = (CharSequence) args[1];
            CharSequence[] elements = (CharSequence[]) args[0];
            return String.join(delimiter, elements);
        });


        Template template = new Template("master", functions);
        template.parse(masterTemplate);

        Writer writer = new StringWriter();
        template.execute(writer, guardians);
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


        template.parse(overlayTemplate);

        writer = new StringWriter();
        template.execute(writer, guardians);
        String overlayText = writer.toString();

        assertEquals("Names: Gamora, Groot, Nebula, Rocket, Star-Lord", overlayText);
    }

}