package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateExecutionTruthinessTest {

    @Test
    void truthinessCoversJavaNumberKinds() throws IOException, TemplateException {
        assertEquals("no", renderIfValue(0L));
        assertEquals("yes", renderIfValue(2L));
        assertEquals("no", renderIfValue(0.0f));
        assertEquals("yes", renderIfValue(1.5f));
        assertEquals("no", renderIfValue(0.0d));
        assertEquals("yes", renderIfValue(2.5d));
        assertEquals("no", renderIfValue((short) 0));
        assertEquals("yes", renderIfValue((short) 1));
        assertEquals("no", renderIfValue((byte) 0));
        assertEquals("yes", renderIfValue((byte) 1));
        assertEquals("no", renderIfValue(BigDecimal.ZERO));
        assertEquals("yes", renderIfValue(new BigDecimal("0.25")));
    }

    @Test
    void truthinessCoversArraysCollectionsMapsAndObjects() throws IOException, TemplateException {
        assertEquals("no", renderIfValue(new String[0]));
        assertEquals("yes", renderIfValue(new String[]{"x"}));
        assertEquals("no", renderIfValue(Collections.emptyList()));
        assertEquals("yes", renderIfValue(Collections.singletonList("x")));
        assertEquals("no", renderIfValue(Collections.emptyMap()));
        assertEquals("yes", renderIfValue(Collections.singletonMap("x", "y")));
        assertEquals("yes", renderIfValue(new Object()));
    }

    @Test
    void pipelineFinalValueForShortCircuitFunctions() throws IOException, TemplateException {
        assertEquals("false", TemplateTestSupport.render("{{.Value | and}}", TemplateTestSupport.data("Value", false)));
        assertEquals("true", TemplateTestSupport.render("{{.Value | and}}", TemplateTestSupport.data("Value", true)));
        assertEquals("true", TemplateTestSupport.render("{{.Value | or}}", TemplateTestSupport.data("Value", true)));
        assertEquals("false", TemplateTestSupport.render("{{.Value | or}}", TemplateTestSupport.data("Value", false)));
    }

    private String renderIfValue(Object value) throws IOException, TemplateException {
        return TemplateTestSupport.render("{{if .Value}}yes{{else}}no{{end}}", TemplateTestSupport.data("Value", value));
    }
}
