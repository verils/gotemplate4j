package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Java 8+ Optional type support in templates.
 */
public class OptionalSupportTest {

    // Test class with Optional fields
    public static class UserWithOptional {
        private String name;
        private Optional<String> email;
        private Optional<Integer> age;
        
        public UserWithOptional(String name, Optional<String> email, Optional<Integer> age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public Optional<String> getEmail() {
            return email;
        }
        
        public Optional<Integer> getAge() {
            return age;
        }
    }

    @Test
    void testOptionalWithValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Email: {{.email}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Alice", 
            Optional.of("alice@example.com"), 
            Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("Email: alice@example.com", writer.toString());
    }

    @Test
    void testOptionalEmpty() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Email: {{if .email}}{{.email}}{{else}}not provided{{end}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Alice", 
            Optional.empty(), 
            Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("Email: not provided", writer.toString());
    }

    @Test
    void testOptionalInteger() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Age: {{.age}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Bob", 
            Optional.empty(), 
            Optional.of(25));
        
        template.execute(writer, user);
        
        assertEquals("Age: 25", writer.toString());
    }

    @Test
    void testOptionalInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .email}}Has email{{else}}No email{{end}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Alice", 
            Optional.of("alice@example.com"), 
            Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("Has email", writer.toString());
    }

    @Test
    void testOptionalEmptyInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .email}}Has email{{else}}No email{{end}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Alice", 
            Optional.empty(), 
            Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("No email", writer.toString());
    }

    @Test
    void testOptionalNestedAccess() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Name: {{.name}}, Email: {{.email}}");
        
        Writer writer = new StringWriter();
        UserWithOptional user = new UserWithOptional("Charlie", 
            Optional.of("charlie@example.com"), 
            Optional.of(30));
        
        template.execute(writer, user);
        
        assertEquals("Name: Charlie, Email: charlie@example.com", writer.toString());
    }

    @Test
    void testOptionalWithMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.value}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("value", Optional.of("test value"));
        
        template.execute(writer, data);
        
        assertEquals("Value: test value", writer.toString());
    }

    @Test
    void testOptionalEmptyWithMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{if .value}}{{.value}}{{else}}empty{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("value", Optional.empty());
        
        template.execute(writer, data);
        
        assertEquals("Value: empty", writer.toString());
    }

    @Test
    void testOptionalInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .items}}{{if .}}{{.}},{{end}}{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("items", new Optional[]{
            Optional.of("a"),
            Optional.of("b"),
            Optional.empty()
        });
        
        template.execute(writer, data);
        
        // Empty optional should be unwrapped to null and skipped with if check
        assertEquals("a,b,", writer.toString());
    }
}
