package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enhanced field error diagnostics.
 */
class EnhancedFieldErrorTest {

    @Test
    void missingFieldShowsAvailableFields() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.Missing}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new PersonBean("Alice", 30)));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field Missing"));
        assertTrue(message.contains("Available fields:"));
        assertTrue(message.contains("name"));
        assertTrue(message.contains("age"));
    }

    @Test
    void typoInFieldNameProvidesSuggestion() throws TemplateException {
        Template template = new Template("test");
        // Use a realistic typo: "naem" is close to "name" (distance=2, score=0.5)
        template.parse("{{.naem}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new PersonBean("Alice", 30)));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field naem"));
        assertTrue(message.contains("Did you mean 'name'?"));
    }

    @Test
    void nestedMissingFieldShowsFullPathAndSuggestions() throws TemplateException {
        Template template = new Template("test");
        // Use a realistic typo: "FristName" is close to "firstName" (distance=1)
        template.parse("{{.User.FristName}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), 
                    TemplateTestSupport.data("User", new PersonBean("Bob", 25))));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field User.FristName"));
        assertTrue(message.contains("Available fields:"));
        assertTrue(message.contains("Did you mean 'firstName'?"));
    }

    @Test
    void publicFieldsAreListedInErrorMessage() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.WrongField}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new PublicFieldBean()));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field WrongField"));
        assertTrue(message.contains("Available fields:"));
        assertTrue(message.contains("publicField"));
    }

    @Test
    void methodNamesAreListedInErrorMessage() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.WrongMethod}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new MethodBean()));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field WrongMethod"));
        assertTrue(message.contains("Available fields:"));
        assertTrue(message.contains("getValue"));
    }

    @Test
    void annotatedFieldsAreListedInErrorMessage() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.WrongAnnotation}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new AnnotatedBean()));

        String message = exception.getMessage();
        assertTrue(message.contains("can't evaluate field WrongAnnotation"));
        assertTrue(message.contains("Available fields:"));
        assertTrue(message.contains("customName"));
    }

    // Test data classes

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    public static class PersonBean {
        private String name;
        private int age;

        PersonBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getFirstName() {
            return name != null && !name.isEmpty() ? name.split(" ")[0] : "";
        }
    }

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    public static class PublicFieldBean {
        public String publicField = "visible";
        private String privateField = "hidden";
    }

    @SuppressWarnings("unused")
    public static class MethodBean {
        public String getValue() {
            return "value";
        }

        public int getCount() {
            return 42;
        }
    }

    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    public static class AnnotatedBean {
        @TemplateField("customName")
        private String name = "annotated";

        public String getName() {
            return name;
        }
    }
}
