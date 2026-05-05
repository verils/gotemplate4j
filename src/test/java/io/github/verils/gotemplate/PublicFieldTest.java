package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for public field access in templates.
 */
public class PublicFieldTest {

    // Test class with public fields
    public static class UserWithPublicFields {
        public String name;
        public int age;
        public boolean active;
        
        public UserWithPublicFields(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }
    }
    
    // Test class with mixed public fields and getters
    public static class MixedAccess {
        public String publicField;
        private String privateField;
        
        public MixedAccess(String publicField, String privateField) {
            this.publicField = publicField;
            this.privateField = privateField;
        }
        
        public String getPrivateField() {
            return privateField;
        }
    }
    
    // Test class with inheritance
    public static class BaseClass {
        public String baseField = "base";
    }
    
    public static class DerivedClass extends BaseClass {
        public String derivedField = "derived";
    }

    @Test
    void testPublicStringField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Name: {{.name}}");
        
        Writer writer = new StringWriter();
        UserWithPublicFields user = new UserWithPublicFields("Alice", 30, true);
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }

    @Test
    void testPublicIntField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Age: {{.age}}");
        
        Writer writer = new StringWriter();
        UserWithPublicFields user = new UserWithPublicFields("Alice", 30, true);
        
        template.execute(writer, user);
        
        assertEquals("Age: 30", writer.toString());
    }

    @Test
    void testPublicBooleanField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Active: {{.active}}");
        
        Writer writer = new StringWriter();
        UserWithPublicFields user = new UserWithPublicFields("Alice", 30, true);
        
        template.execute(writer, user);
        
        assertEquals("Active: true", writer.toString());
    }

    @Test
    void testMixedPublicFieldAndGetter() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Public: {{.publicField}}, Private: {{.privateField}}");
        
        Writer writer = new StringWriter();
        MixedAccess obj = new MixedAccess("public value", "private value");
        
        template.execute(writer, obj);
        
        assertEquals("Public: public value, Private: private value", writer.toString());
    }

    @Test
    void testInheritedPublicField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Base: {{.baseField}}, Derived: {{.derivedField}}");
        
        Writer writer = new StringWriter();
        DerivedClass obj = new DerivedClass();
        
        template.execute(writer, obj);
        
        assertEquals("Base: base, Derived: derived", writer.toString());
    }

    @Test
    void testPublicFieldInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .active}}User is active{{else}}User is inactive{{end}}");
        
        Writer writer = new StringWriter();
        UserWithPublicFields user = new UserWithPublicFields("Alice", 30, true);
        
        template.execute(writer, user);
        
        assertEquals("User is active", writer.toString());
    }

    @Test
    void testPublicFieldWithDotNotation() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.user.name}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        UserWithPublicFields user = new UserWithPublicFields("Bob", 25, false);
        data.put("user", user);
        
        template.execute(writer, data);
        
        assertEquals("Bob", writer.toString());
    }

    @Test
    void testPublicFieldInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .users}}{{.name}},{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        UserWithPublicFields user1 = new UserWithPublicFields("Alice", 30, true);
        UserWithPublicFields user2 = new UserWithPublicFields("Bob", 25, false);
        data.put("users", new UserWithPublicFields[]{user1, user2});
        
        template.execute(writer, data);
        
        assertEquals("Alice,Bob,", writer.toString());
    }
}
