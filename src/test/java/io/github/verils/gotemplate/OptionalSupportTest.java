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
    @SuppressWarnings({"FieldMayBeFinal", "OptionalUsedAsFieldOrParameterType", "unused"})
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

    // Test classes for deep Optional chain scenarios
    @SuppressWarnings({"FieldMayBeFinal", "OptionalUsedAsFieldOrParameterType", "unused"})
    public static class Address {
        private Optional<City> city;
        
        public Address(Optional<City> city) {
            this.city = city;
        }
        
        public Optional<City> getCity() {
            return city;
        }
    }

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    public static class City {
        private String name;
        
        public City(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }

    @SuppressWarnings({"FieldMayBeFinal", "OptionalUsedAsFieldOrParameterType", "unused"})
    public static class UserWithNestedOptional {
        private String name;
        private Optional<Address> address;
        
        public UserWithNestedOptional(String name, Optional<Address> address) {
            this.name = name;
            this.address = address;
        }
        
        public String getName() {
            return name;
        }
        
        public Optional<Address> getAddress() {
            return address;
        }
    }

    @Test
    void testDeepOptionalChainWithValue() throws IOException, TemplateException {
        // User -> Optional<Address> -> Optional<City> -> String name
        Template template = new Template("test");
        template.parse("{{.name}} lives in {{.address.city.name}}");
        
        Writer writer = new StringWriter();
        City city = new City("Beijing");
        Address address = new Address(Optional.of(city));
        UserWithNestedOptional user = new UserWithNestedOptional("Alice", Optional.of(address));
        
        template.execute(writer, user);
        
        assertEquals("Alice lives in Beijing", writer.toString());
    }

    @Test
    void testDeepOptionalChainWithEmptyMiddle() throws IOException, TemplateException {
        // User -> Optional<Address> (empty) -> should stop gracefully
        Template template = new Template("test");
        template.parse("{{.name}}: {{if .address}}{{.address.city.name}}{{else}}no address{{end}}");
        
        Writer writer = new StringWriter();
        UserWithNestedOptional user = new UserWithNestedOptional("Bob", Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("Bob: no address", writer.toString());
    }

    @Test
    void testDeepOptionalChainWithEmptyInner() throws IOException, TemplateException {
        // User -> Optional<Address> -> Optional<City> (empty)
        Template template = new Template("test");
        template.parse("{{.name}}: {{if .address.city}}{{.address.city.name}}{{else}}unknown city{{end}}");
        
        Writer writer = new StringWriter();
        Address address = new Address(Optional.empty());
        UserWithNestedOptional user = new UserWithNestedOptional("Charlie", Optional.of(address));
        
        template.execute(writer, user);
        
        assertEquals("Charlie: unknown city", writer.toString());
    }

    @Test
    void testDeepOptionalChainThreeLevels() throws IOException, TemplateException {
        // Three-level nesting: User -> Optional<Address> -> Optional<City> -> Optional<String> district
        Template template = new Template("test");
        template.parse("District: {{.address.city.district}}");
        
        Writer writer = new StringWriter();
        
        CityWithDistrict city = new CityWithDistrict("Shanghai", Optional.of("Pudong"));
        Address address = new Address(Optional.of(city));
        UserWithNestedOptional user = new UserWithNestedOptional("David", Optional.of(address));
        
        template.execute(writer, user);
        
        assertEquals("District: Pudong", writer.toString());
    }

    @Test
    void testDeepOptionalChainAllEmpty() throws IOException, TemplateException {
        // All levels empty - should handle gracefully without NPE
        Template template = new Template("test");
        template.parse("Result: {{if .address}}{{if .address.city}}{{.address.city.name}}{{else}}no city{{end}}{{else}}no address{{end}}");
        
        Writer writer = new StringWriter();
        UserWithNestedOptional user = new UserWithNestedOptional("Eve", Optional.empty());
        
        template.execute(writer, user);
        
        assertEquals("Result: no address", writer.toString());
    }

    @Test
    void testDeepOptionalChainInMap() throws IOException, TemplateException {
        // Deep Optional chain with Map data structure
        Template template = new Template("test");
        template.parse("{{.user.address.city.name}}");
        
        Writer writer = new StringWriter();
        City city = new City("Guangzhou");
        Address address = new Address(Optional.of(city));
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", new UserWithNestedOptional("Frank", Optional.of(address)));
        
        template.execute(writer, data);
        
        assertEquals("Guangzhou", writer.toString());
    }

    // Helper class for three-level Optional chain test
    @SuppressWarnings({"FieldMayBeFinal", "OptionalUsedAsFieldOrParameterType", "unused"})
    public static class CityWithDistrict extends City {
        private Optional<String> district;
        
        public CityWithDistrict(String name, Optional<String> district) {
            super(name);
            this.district = district;
        }
        
        public Optional<String> getDistrict() {
            return district;
        }
    }
}
