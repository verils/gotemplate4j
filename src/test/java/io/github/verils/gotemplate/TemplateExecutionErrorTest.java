package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateExecutionErrorTest {

    @Test
    void indexWrapsBuiltinRuntimeFailure() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{index .Value 0}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("Value", new Object())));

        assertTrue(exception.getMessage().contains("function 'index' failed"));
        assertTrue(exception.getMessage().contains("2 argument(s)"));
        assertTrue(exception.getMessage().contains("index: invalid type"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void strictMissingKeyPolicyFailsAfterEmptyOptional() throws TemplateException {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.User.Name}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("User", Optional.empty())));

        assertTrue(exception.getMessage().contains("nil pointer evaluating User.Name at 'Name'"));
    }

    @Test
    void missingBeanFieldFailsClearly() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.Missing}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new SimpleBean("Bob")));

        assertTrue(exception.getMessage().contains("can't evaluate field Missing"));
    }

    @Test
    void undefinedTemplateActionFailsDuringExecution() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{template \"missing\" .}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data()));

        assertTrue(exception.getMessage().contains("template missing not defined"));
    }

    @Test
    void nestedFieldAccessShowsFullPathOnError() throws TemplateException {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.User.Address.City}}");

        // User exists but Address is null
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), 
                    TemplateTestSupport.data("User", new AddressHolder(null))));

        assertTrue(exception.getMessage().contains("nil pointer evaluating User.Address.City at 'City'"));
    }

    @Test
    void deepNestedFieldAccessShowsFullPathOnError() throws TemplateException {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.User.Address.Street.Number}}");

        // User and Address exist but Street is null
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), 
                    TemplateTestSupport.data("User", new AddressHolder(new StreetHolder(null)))));

        assertTrue(exception.getMessage().contains("nil pointer evaluating User.Address.Street.Number at 'Number'"));
    }

    @Test
    void missingFieldInNestedPathShowsFullPath() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.User.Profile.Bio}}");

        // User exists but Profile doesn't have Bio field
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), 
                    TemplateTestSupport.data("User", new ProfileHolder())));

        assertTrue(exception.getMessage().contains("can't evaluate field User.Profile.Bio"));
    }

    public static class SimpleBean {
        private final String name;

        SimpleBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @SuppressWarnings("unused")
    public static class AddressHolder {
        private final Object address;

        AddressHolder(Object address) {
            this.address = address;
        }

        public Object getAddress() {
            return address;
        }
    }

    @SuppressWarnings("unused")
    public static class StreetHolder {
        private final Object street;

        StreetHolder(Object street) {
            this.street = street;
        }

        public Object getStreet() {
            return street;
        }
    }

    @SuppressWarnings("unused")
    public static class ProfileHolder {
        private final Profile profile = new Profile();

        public Profile getProfile() {
            return profile;
        }
    }

    public static class Profile {
        // Intentionally missing 'bio' field to test error message
        public String getName() {
            return "Test Profile";
        }
    }
}
