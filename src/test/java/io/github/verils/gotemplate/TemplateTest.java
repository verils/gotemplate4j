package io.github.verils.gotemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void test() throws IOException, GoTemplateException {
        Template template = new Template("demo");
        template.parse("{{ .Name }}");

        StringWriter writer = new StringWriter();

        User user = new User();
        user.setName("Bob");

        template.execute(writer, user);
        template.executeTemplate(writer,"", user);
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
}