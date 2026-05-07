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
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void strictMissingKeyPolicyFailsAfterEmptyOptional() throws TemplateException {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.User.Name}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("User", Optional.empty())));

        assertTrue(exception.getMessage().contains("missing value for field-chain segment 'Name'"));
    }

    @Test
    void missingBeanFieldFailsClearly() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{.Missing}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), new SimpleBean("Bob")));

        assertTrue(exception.getMessage().contains("can't get value 'Missing' from data"));
    }

    @Test
    void undefinedTemplateActionFailsDuringExecution() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{template \"missing\" .}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data()));

        assertTrue(exception.getMessage().contains("template missing not defined"));
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
}
