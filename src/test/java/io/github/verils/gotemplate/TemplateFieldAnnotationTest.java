package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for @TemplateField annotation support.
 */
public class TemplateFieldAnnotationTest {

    // Test class with annotated fields
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class UserWithAnnotatedFields {
        @TemplateField("UserName")
        private String userName = "Alice";
        
        @TemplateField("user_email")
        private String email = "alice@example.com";
        
        @TemplateField("AGE")
        public int age = 30;
    }
    
    // Test class with annotated methods
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
    public static class UserWithAnnotatedMethods {
        private String firstName = "John";
        private String lastName = "Doe";
        
        @TemplateField("FullName")
        public String getFullName() {
            return firstName + " " + lastName;
        }
        
        @TemplateField("display_name")
        public String getDisplayName() {
            return firstName.toUpperCase();
        }
    }
    
    // Test class with both field and method annotations (field takes precedence)
    @SuppressWarnings("unused")
    public static class FieldPrecedenceTest {
        @TemplateField("value")
        public String fieldValue = "from-field";
        
        @TemplateField("value")
        public String getFieldValue() {
            return "from-method";
        }
    }
    
    // Test class mixing annotated and non-annotated members
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class MixedAnnotationTest {
        @TemplateField("CustomName")
        private String customName = "Custom";
        
        // Non-annotated field - should use Go-style capitalization
        public String normalField = "Normal";
        
        // Non-annotated getter - should use Go-style capitalization
        public String getAnotherField() {
            return "Another";
        }
    }
    
    // Test class with inheritance
    @SuppressWarnings("unused")
    public static class BaseAnnotatedClass {
        @TemplateField("BaseField")
        public String baseField = "base-value";
    }
    
    @SuppressWarnings("unused")
    public static class DerivedAnnotatedClass extends BaseAnnotatedClass {
        @TemplateField("DerivedField")
        public String derivedField = "derived-value";
    }
    
    // Test class with nested objects
    @SuppressWarnings("unused")
    public static class Address {
        @TemplateField("StreetName")
        public String street = "Main St";
        
        @TemplateField("CityName")
        public String city = "Springfield";
    }
    
    @SuppressWarnings("unused")
    public static class PersonWithAddress {
        @TemplateField("PersonName")
        public String name = "Bob";
        
        public Address address = new Address();
    }

    @Test
    void testAnnotatedPrivateField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Name: {{.UserName}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testAnnotatedFieldWithDifferentCase() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Email: {{.user_email}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        template.execute(writer, user);
        
        assertEquals("Email: alice@example.com", writer.toString());
    }
    
    @Test
    void testAnnotatedPublicField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Age: {{.AGE}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        template.execute(writer, user);
        
        assertEquals("Age: 30", writer.toString());
    }
    
    @Test
    void testAnnotatedMethod() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Full Name: {{.FullName}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedMethods user = new UserWithAnnotatedMethods();
        
        template.execute(writer, user);
        
        assertEquals("Full Name: John Doe", writer.toString());
    }
    
    @Test
    void testAnnotatedMethodWithUnderscore() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Display: {{.display_name}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedMethods user = new UserWithAnnotatedMethods();
        
        template.execute(writer, user);
        
        assertEquals("Display: JOHN", writer.toString());
    }
    
    @Test
    void testFieldAnnotationTakesPrecedenceOverMethod() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Value: {{.value}}");
        
        Writer writer = new StringWriter();
        FieldPrecedenceTest obj = new FieldPrecedenceTest();
        
        template.execute(writer, obj);
        
        // Field annotation should take precedence
        assertEquals("Value: from-field", writer.toString());
    }
    
    @Test
    void testMixedAnnotatedAndNonAnnotated() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Custom: {{.CustomName}}, Normal: {{.normalField}}, Another: {{.anotherField}}");
        
        Writer writer = new StringWriter();
        MixedAnnotationTest obj = new MixedAnnotationTest();
        
        template.execute(writer, obj);
        
        assertEquals("Custom: Custom, Normal: Normal, Another: Another", writer.toString());
    }
    
    @Test
    void testInheritedAnnotatedField() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Base: {{.BaseField}}, Derived: {{.DerivedField}}");
        
        Writer writer = new StringWriter();
        DerivedAnnotatedClass obj = new DerivedAnnotatedClass();
        
        template.execute(writer, obj);
        
        assertEquals("Base: base-value, Derived: derived-value", writer.toString());
    }
    
    @Test
    void testNestedObjectWithAnnotations() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Person: {{.PersonName}}, Street: {{.address.StreetName}}, City: {{.address.CityName}}");
        
        Writer writer = new StringWriter();
        PersonWithAddress person = new PersonWithAddress();
        
        template.execute(writer, person);
        
        assertEquals("Person: Bob, Street: Main St, City: Springfield", writer.toString());
    }
    
    @Test
    void testAnnotatedFieldInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .UserName}}User has name{{else}}No name{{end}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        template.execute(writer, user);
        
        assertEquals("User has name", writer.toString());
    }
    
    @Test
    void testAnnotatedFieldInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .users}}{{.UserName}},{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        UserWithAnnotatedFields user1 = new UserWithAnnotatedFields();
        UserWithAnnotatedFields user2 = new UserWithAnnotatedFields();
        user2.userName = "Bob";
        
        data.put("users", new UserWithAnnotatedFields[]{user1, user2});
        
        template.execute(writer, data);
        
        assertEquals("Alice,Bob,", writer.toString());
    }
    
    @Test
    void testAnnotatedFieldWithVariableAssignment() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{$name := .UserName}}Name: {{$name}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testMissingAnnotatedFieldWithErrorPolicy() throws TemplateException {
        Template template = new Template("test")
            .withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.NonExistent}}");
        
        Writer writer = new StringWriter();
        UserWithAnnotatedFields user = new UserWithAnnotatedFields();
        
        assertThrows(TemplateExecutionException.class, () -> template.execute(writer, user));
    }
    
    @Test
    void testBackwardCompatibilityWithoutAnnotation() throws IOException, TemplateException {
        // Test that classes without annotations still work with Go-style capitalization
        Template template = new Template("test");
        template.parse("Name: {{.Name}}, Age: {{.Age}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Charlie");
        data.put("Age", 25);
        
        template.execute(writer, data);
        
        assertEquals("Name: Charlie, Age: 25", writer.toString());
    }
}
