package com.github.verils.gotemplate;

import com.github.verils.gotemplate.parse.Function;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GoTemplateTest {

    @Test
    void test() {
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

        class Recipient {
            private final String name;
            private final String gift;
            private final boolean attended;

            public Recipient(String name, String gift, boolean attended) {
                this.name = name;
                this.gift = gift;
                this.attended = attended;
            }

            public String getName() {
                return name;
            }

            public String getGift() {
                return gift;
            }

            public boolean isAttended() {
                return attended;
            }
        }

        Recipient[] recipients = new Recipient[]{
                new Recipient("Aunt Mildred", "bone china tea set", true),
                new Recipient("Uncle John", "moleskin pants", false),
                new Recipient("Cousin Rodney", "", false)
        };

        GoTemplate goTemplate = new GoTemplate("letter");
        goTemplate.parse(letter);

        String text1 = goTemplate.execute(recipients[0]);
        assertNotNull(text1);
        assertFalse(text1.contains("{{.Name}}"));
        assertEquals("\n" +
                "Dear Aunt Mildred,\n" +
                "\n" +
                "It was a pleasure to see you at the wedding.\n" +
                "Thank you for the lovely bone china tea set.\n" +
                "\n" +
                "Best wishes,\n" +
                "Josie\n", text1);

        String text2 = goTemplate.execute(recipients[1]);
        assertNotNull(text2);
        assertFalse(text2.contains("{{.Name}}"));
        assertEquals("\n" +
                "Dear Uncle John,\n" +
                "\n" +
                "It is a shame you couldn't make it to the wedding.\n" +
                "Thank you for the lovely moleskin pants.\n" +
                "\n" +
                "Best wishes,\n" +
                "Josie\n", text2);

        String text3 = goTemplate.execute(recipients[2]);
        assertNotNull(text3);
        assertFalse(text3.contains("{{.Name}}"));
        assertEquals("\n" +
                "Dear Cousin Rodney,\n" +
                "\n" +
                "It is a shame you couldn't make it to the wedding.\n" +
                "\n" +
                "Best wishes,\n" +
                "Josie\n", text3);
    }

    private String readTemplateInClassPath(String path) throws FileNotFoundException {
        InputStream in = GoTemplateTest.class.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.lines().collect(Collectors.joining("\n"));
    }


    @Test
    void testDefinition() {
        String masterTemplate = "Names:{{block \"list\" .}}{{\"\\n\"}}{{range .}}{{println \"-\" .}}{{end}}{{end}}";
        String overlayTemplate = "{{define \"list\"}} {{join . \", \"}}{{end}} ";

        String[] guardians = {"Gamora", "Groot", "Nebula", "Rocket", "Star-Lord"};

        Map<String, Function> functions = new LinkedHashMap<>();
        functions.put("join", args -> {
            CharSequence delimiter = (CharSequence) args[1];
            CharSequence[] elements = (CharSequence[]) args[0];
            return String.join(delimiter, elements);
        });


        GoTemplate goTemplate = new GoTemplate("master", functions);
        goTemplate.parse(masterTemplate);
//        goTemplate.parse(overlayTemplate);
        String text = goTemplate.execute(guardians);

        assertEquals("Names:\n" +
                "- Gamora\n" +
                "- Groot\n" +
                "- Nebula\n" +
                "- Rocket\n" +
                "- Star-Lord", text);
    }
}