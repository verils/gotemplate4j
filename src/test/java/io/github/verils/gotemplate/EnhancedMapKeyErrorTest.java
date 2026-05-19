package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enhanced map key error diagnostics.
 */
class EnhancedMapKeyErrorTest {

    @Test
    void missingMapKeyShowsAvailableKeys() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.Name}}");

        Map<String, Object> data = new HashMap<>();
        data.put("FirstName", "Alice");
        data.put("LastName", "Smith");
        data.put("Age", 30);

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'Name'"));
        assertTrue(message.contains("Available keys:"));
        assertTrue(message.contains("FirstName"));
        assertTrue(message.contains("LastName"));
        assertTrue(message.contains("Age"));
    }

    @Test
    void typoInMapKeyProvidesSuggestion() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // Use a realistic typo: "FristName" is close to "FirstName" (distance=1)
        template.parse("{{.FristName}}");

        Map<String, Object> data = new HashMap<>();
        data.put("FirstName", "Alice");
        data.put("LastName", "Smith");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'FristName'"));
        assertTrue(message.contains("Did you mean 'FirstName'?"));
    }

    @Test
    void nestedMapAccessShowsFullPathAndSuggestions() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // Use a realistic typo: "Emial" is close to "Email" (distance=1)
        template.parse("{{.User.Emial}}");

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Name", "Bob");
        userMap.put("Email", "bob@example.com");
        userMap.put("Phone", "123-456-7890");

        Map<String, Object> data = new HashMap<>();
        data.put("User", userMap);

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'Emial'"));
        assertTrue(message.contains("Available keys:"));
        assertTrue(message.contains("Did you mean 'Email'?"));
    }

    @Test
    void indexFunctionWithMissingKeyShowsSuggestions() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // Use index function with typo: "Nmae" is close to "Name" (distance=1)
        template.parse("{{index . \"Nmae\"}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Name", "Alice");
        data.put("Age", 30);

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'Nmae'"));
        assertTrue(message.contains("Available keys:"));
        assertTrue(message.contains("Did you mean 'Name'?"));
    }

    @Test
    void mapWithNumericKeysShowsAvailableKeys() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{index . \"5\"}}");

        Map<Integer, String> data = new HashMap<>();
        data.put(1, "one");
        data.put(2, "two");
        data.put(3, "three");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key '5'"));
        assertTrue(message.contains("Available keys:"));
        assertTrue(message.contains("1"));
        assertTrue(message.contains("2"));
        assertTrue(message.contains("3"));
    }

    @Test
    void emptyMapShowsNoAvailableKeys() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.Missing}}");

        Map<String, Object> data = new HashMap<>();
        // Empty map

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'Missing'"));
        assertFalse(message.contains("Available keys:"));
    }

    @Test
    void multipleTyposShowsBestMatch() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // "UserNmae" should match "UserName" better than other keys
        template.parse("{{.UserNmae}}");

        Map<String, Object> data = new HashMap<>();
        data.put("UserName", "Alice");
        data.put("EmailAddress", "alice@example.com");
        data.put("PhoneNumber", "123-456-7890");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'UserNmae'"));
        assertTrue(message.contains("Did you mean 'UserName'?"));
    }

    @Test
    void caseInsensitiveMapKeyMatchProvidesSuggestion() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // Case difference: "username" vs "UserName"
        template.parse("{{.username}}");

        Map<String, Object> data = new HashMap<>();
        data.put("UserName", "Alice");
        data.put("Age", 30);

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'username'"));
        assertTrue(message.contains("Did you mean 'UserName'?"));
    }

    @Test
    void noGoodMatchDoesNotProvideSuggestion() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // "xyz" is very different from all available keys
        template.parse("{{.xyz}}");

        Map<String, Object> data = new HashMap<>();
        data.put("FirstName", "Alice");
        data.put("LastName", "Smith");
        data.put("Age", 30);

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'xyz'"));
        assertTrue(message.contains("Available keys:"));
        // Should not provide a suggestion when no good match exists
        assertFalse(message.contains("Did you mean"));
    }

    @Test
    void defaultPolicyReturnsNullWithoutError() throws Exception {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.INVALID);
        template.parse("Name: '{{.Missing}}'");

        Map<String, Object> data = new HashMap<>();
        data.put("FirstName", "Alice");

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        // Should not throw exception, just render <no value>
        assertEquals("Name: '<no value>'", writer.toString());
    }

    @Test
    void complexTypoScenario() throws TemplateException {
        Template template = new Template("test");
        template.withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        // Multiple character transpositions
        template.parse("{{.Addres}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Address", "123 Main St");
        data.put("City", "Springfield");
        data.put("State", "IL");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), data));

        String message = exception.getMessage();
        assertTrue(message.contains("missing map key 'Addres'"));
        assertTrue(message.contains("Did you mean 'Address'?"));
    }
}
