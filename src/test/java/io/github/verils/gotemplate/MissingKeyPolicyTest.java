package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissingKeyPolicyTest {

    @Test
    void missingKeyPolicyEnumValuesAreStable() {
        assertEquals(Arrays.asList(MissingKeyPolicy.INVALID, MissingKeyPolicy.ZERO, MissingKeyPolicy.ERROR),
                Arrays.asList(MissingKeyPolicy.values()));
        assertSame(MissingKeyPolicy.ERROR, MissingKeyPolicy.valueOf("ERROR"));
    }

    @Test
    void defaultPolicyPrintsNoValueForMissingMapKey() throws Exception {
        assertEquals("Hello, <no value>!", TemplateTestSupport.render("Hello, {{.Name}}!", TemplateTestSupport.data()));
    }

    @Test
    void zeroPolicyFallsBackToNoValueForUnknownMapValueType() throws Exception {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ZERO);
        template.parse("Hello, {{.Name}}!");

        StringWriter writer = new StringWriter();
        template.execute(writer, TemplateTestSupport.data());

        assertEquals("Hello, <no value>!", writer.toString());
    }

    @Test
    void nullMissingKeyPolicyResetsToDefault() {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);

        template.withMissingKeyPolicy(null);

        assertSame(MissingKeyPolicy.INVALID, template.missingKeyPolicy());
    }

    @Test
    void stringOptionsCoverDefaultAndZeroPolicies() {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);

        assertSame(template, template.option("missingkey=default"));
        assertSame(MissingKeyPolicy.INVALID, template.missingKeyPolicy());
        template.option("missingkey=zero");
        assertSame(MissingKeyPolicy.ZERO, template.missingKeyPolicy());
    }

    @Test
    void errorPolicyFailsForMissingMapKey() throws Exception {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("Hello, {{.Name}}!");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data()));

        assertTrue(exception.getMessage().contains("missing map key 'Name'"));
    }

    @Test
    void errorPolicyFailsForNestedMissingMapKey() throws Exception {
        Template template = new Template("test").option("missingkey=error");
        template.parse("{{.User.Name}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("User", TemplateTestSupport.data())));

        assertTrue(exception.getMessage().contains("missing map key 'Name'"));
    }

    @Test
    void errorPolicyFailsForMissingSegmentAfterNull() throws Exception {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{.User.Name}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("User", null)));

        assertTrue(exception.getMessage().contains("missing value for field-chain segment 'Name'"));
    }

    @Test
    void errorPolicyFailsForIndexMissingMapKey() throws Exception {
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{index . \"Name\"}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data()));

        assertTrue(exception.getMessage().contains("missing map key 'Name'"));
    }

    @Test
    void indexMissingMapKeyUsesDefaultPolicyWhenNotStrict() throws Exception {
        assertEquals("[<no value>]", TemplateTestSupport.render("[{{index . \"Name\"}}]", TemplateTestSupport.data()));
    }

    @Test
    void indexExistingMapKeyUnwrapsOptional() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("Name", java.util.Optional.of("Bob"));

        assertEquals("Bob", TemplateTestSupport.render("{{index . \"Name\"}}", data));
    }

    @Test
    void indexNullValueFailsOnlyForErrorPolicy() throws Exception {
        assertEquals("[<no value>]", TemplateTestSupport.render("[{{index .Missing \"Name\"}}]", TemplateTestSupport.data()));

        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{index .Missing \"Name\"}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("Missing", null)));

        assertTrue(exception.getMessage().contains("index of null value"));
    }

    @Test
    void nilValuesPrintNoValue() throws Exception {
        assertEquals("<no value>", TemplateTestSupport.render("{{nil}}", TemplateTestSupport.data()));
        assertEquals("Name=<no value>", TemplateTestSupport.render("Name={{.Name}}",
                TemplateTestSupport.data("Name", null)));
        assertEquals("Dot=<no value>", TemplateTestSupport.render("Dot={{.}}", null));
    }

    @Test
    void indexDelegatesToCollectionFunctionForArraysAndErrors() throws Exception {
        assertEquals("b", TemplateTestSupport.render("{{index .Items 1}}",
                TemplateTestSupport.data("Items", new String[]{"a", "b"})));

        Template template = new Template("test");
        template.parse("{{index .Items}}");

        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class,
                () -> template.execute(new StringWriter(), TemplateTestSupport.data("Items", new String[]{"a"})));

        assertTrue(exception.getMessage().contains("index"));
    }

    @Test
    void customIndexFunctionStillOverridesBuiltinIndex() throws Exception {
        Map<String, Function> functions = new HashMap<>();
        functions.put("index", args -> "custom");
        Template template = new Template("test", functions).withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{index . \"Missing\"}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, TemplateTestSupport.data());

        assertEquals("custom", writer.toString());
    }

    @Test
    void streamReaderAndOutputStreamEntrypointsUseSamePolicy() throws Exception {
        Template fromInputStream = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        fromInputStream.parse(new ByteArrayInputStream("{{.Name}}".getBytes(StandardCharsets.UTF_8)));
        assertThrows(TemplateExecutionException.class,
                () -> fromInputStream.execute(new StringWriter(), TemplateTestSupport.data()));

        Template fromReader = new Template("test");
        fromReader.parse(new StringReader("{{.Name}}"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fromReader.execute(out, TemplateTestSupport.data("Name", "Bob"));
        assertEquals("Bob", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void missingKeyInBranchIsFalseyForDefaultPolicy() throws Exception {
        assertEquals("missing", TemplateTestSupport.render(
                "{{if .Name}}present{{else}}missing{{end}}", TemplateTestSupport.data()));
    }

    @Test
    void missingKeyPolicyIsPreservedByCopyConstructor() throws Exception {
        Template original = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        original.parse("{{.Name}}");

        Template copy = new Template(original);

        assertSame(MissingKeyPolicy.ERROR, copy.missingKeyPolicy());
        assertThrows(TemplateExecutionException.class,
                () -> copy.execute(new StringWriter(), TemplateTestSupport.data()));
    }

    @Test
    void errorPolicyStillAllowsPresentNullMapValues() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("Name", null);
        Template template = new Template("test").withMissingKeyPolicy(MissingKeyPolicy.ERROR);
        template.parse("{{if .Name}}present{{else}}empty{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("empty", writer.toString());
    }
}
