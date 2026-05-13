package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Go-style field name conversion (camelCase to PascalCase).
 * Verifies that both lowercase and capitalized field names work correctly.
 */
public class GoStyleFieldNameTest {

    // Test JavaBean with standard getter methods
    public static class UserBean {
        private String userName = "Alice";
        private String emailAddress = "alice@example.com";
        private int age = 30;
        private boolean active = true;
        
        public String getUserName() { return userName; }
        public String getEmailAddress() { return emailAddress; }
        public int getAge() { return age; }
        public boolean isActive() { return active; }
    }
    
    // Test class with public fields
    public static class DataObject {
        public String firstName = "John";
        public String lastName = "Doe";
        public int itemCount = 5;
    }
    
    // Test nested objects
    public static class Address {
        private String streetName = "Main St";
        private String cityName = "Springfield";
        
        public String getStreetName() { return streetName; }
        public String getCityName() { return cityName; }
    }
    
    public static class Person {
        private String fullName = "Bob Smith";
        private Address homeAddress = new Address();
        
        public String getFullName() { return fullName; }
        public Address getHomeAddress() { return homeAddress; }
    }

    @Test
    void testLowercaseFieldName() throws IOException, TemplateException {
        // Test accessing with lowercase (exact match on bean property name)
        Template template = new Template("test");
        template.parse("Name: {{.userName}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testCapitalizedFieldName() throws IOException, TemplateException {
        // Test accessing with capitalized (Go-style conversion)
        Template template = new Template("test");
        template.parse("Name: {{.UserName}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testBothLowercaseAndCapitalizedWork() throws IOException, TemplateException {
        // Verify both styles produce the same result
        Template template1 = new Template("test1");
        template1.parse("{{.userName}}");
        
        Template template2 = new Template("test2");
        template2.parse("{{.UserName}}");
        
        UserBean user = new UserBean();
        
        StringWriter writer1 = new StringWriter();
        template1.execute(writer1, user);
        
        StringWriter writer2 = new StringWriter();
        template2.execute(writer2, user);
        
        assertEquals(writer1.toString(), writer2.toString());
        assertEquals("Alice", writer1.toString());
    }
    
    @Test
    void testMultipleFieldsLowercase() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Name: {{.userName}}, Email: {{.emailAddress}}, Age: {{.age}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice, Email: alice@example.com, Age: 30", writer.toString());
    }
    
    @Test
    void testMultipleFieldsCapitalized() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Name: {{.UserName}}, Email: {{.EmailAddress}}, Age: {{.Age}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice, Email: alice@example.com, Age: 30", writer.toString());
    }
    
    @Test
    void testBooleanFieldLowercase() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Active: {{.active}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Active: true", writer.toString());
    }
    
    @Test
    void testBooleanFieldCapitalized() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Active: {{.Active}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Active: true", writer.toString());
    }
    
    @Test
    void testPublicFieldLowercase() throws IOException, TemplateException {
        // Public fields are accessed by their exact name
        Template template = new Template("test");
        template.parse("First: {{.firstName}}, Last: {{.lastName}}");
        
        Writer writer = new StringWriter();
        DataObject obj = new DataObject();
        
        template.execute(writer, obj);
        
        assertEquals("First: John, Last: Doe", writer.toString());
    }
    
    @Test
    void testNestedObjectLowercase() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Person: {{.fullName}}, Street: {{.homeAddress.streetName}}, City: {{.homeAddress.cityName}}");
        
        Writer writer = new StringWriter();
        Person person = new Person();
        
        template.execute(writer, person);
        
        assertEquals("Person: Bob Smith, Street: Main St, City: Springfield", writer.toString());
    }
    
    @Test
    void testNestedObjectCapitalized() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Person: {{.FullName}}, Street: {{.HomeAddress.StreetName}}, City: {{.HomeAddress.CityName}}");
        
        Writer writer = new StringWriter();
        Person person = new Person();
        
        template.execute(writer, person);
        
        assertEquals("Person: Bob Smith, Street: Main St, City: Springfield", writer.toString());
    }
    
    @Test
    void testMixedCaseInSameTemplate() throws IOException, TemplateException {
        // Test using both lowercase and capitalized in the same template
        Template template = new Template("test");
        template.parse("{{.userName}} and {{.UserName}} should be the same");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Alice and Alice should be the same", writer.toString());
    }
    
    @Test
    void testLowercaseInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .active}}User is active{{else}}User is inactive{{end}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("User is active", writer.toString());
    }
    
    @Test
    void testCapitalizedInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if .Active}}User is active{{else}}User is inactive{{end}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("User is active", writer.toString());
    }
    
    @Test
    void testLowercaseInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .items}}{{.name}},{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        Map<String, String> item1 = new HashMap<>();
        item1.put("name", "Item1");
        Map<String, String> item2 = new HashMap<>();
        item2.put("name", "Item2");
        
        data.put("items", new Object[]{item1, item2});
        
        template.execute(writer, data);
        
        assertEquals("Item1,Item2,", writer.toString());
    }
    
    @Test
    void testCapitalizedInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .Items}}{{.Name}},{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        
        Map<String, String> item1 = new HashMap<>();
        item1.put("Name", "Item1");
        Map<String, String> item2 = new HashMap<>();
        item2.put("Name", "Item2");
        
        data.put("Items", new Object[]{item1, item2});
        
        template.execute(writer, data);
        
        assertEquals("Item1,Item2,", writer.toString());
    }
    
    @Test
    void testLowercaseWithVariableAssignment() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{$name := .userName}}Name: {{$name}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testCapitalizedWithVariableAssignment() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{$name := .UserName}}Name: {{$name}}");
        
        Writer writer = new StringWriter();
        UserBean user = new UserBean();
        
        template.execute(writer, user);
        
        assertEquals("Name: Alice", writer.toString());
    }
    
    @Test
    void testComplexMultiLevelPathLowercase() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.homeAddress.cityName}}");
        
        Writer writer = new StringWriter();
        Person person = new Person();
        
        template.execute(writer, person);
        
        assertEquals("Springfield", writer.toString());
    }
    
    @Test
    void testComplexMultiLevelPathCapitalized() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{.HomeAddress.CityName}}");
        
        Writer writer = new StringWriter();
        Person person = new Person();
        
        template.execute(writer, person);
        
        assertEquals("Springfield", writer.toString());
    }
}
