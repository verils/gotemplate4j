package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for integer range support (Go compatibility feature).
 * Go templates support {{range $i := 5}} which iterates from 0 to 4.
 */
public class IntegerRangeTest {

    @Test
    public void testRangeOverInteger() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $i := 5}}{{$i}} {{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("0 1 2 3 4 ", writer.toString());
    }

    @Test
    public void testRangeOverIntegerWithIndexAndValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $idx, $val := 3}}idx={{$idx}} val={{$val}} | {{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("idx=0 val=0 | idx=1 val=1 | idx=2 val=2 | ", writer.toString());
    }

    @Test
    public void testRangeOverZero() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $i := 0}}should not appear{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("", writer.toString());
    }

    @Test
    public void testRangeOverNegativeNumber() throws IOException, TemplateException {
        // Negative numbers should not iterate
        // Note: -5 in template may be parsed as unary minus, so we test with data instead
        Template template = new Template("test");
        template.parse("{{range $i := .Count}}should not appear{{end}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Count", -5);

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("", writer.toString());
    }

    @Test
    public void testRangeOverIntegerFromData() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $i := .Count}}Item {{$i}}\n{{end}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Count", 3);

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("Item 0\nItem 1\nItem 2\n", writer.toString());
    }

    @Test
    public void testRangeOverIntegerWithElse() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $i := 0}}has items{{else}}no items{{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("no items", writer.toString());
    }

    // TODO: Fix break/continue in range - these require parser-level context tracking
    // @Test
    // public void testRangeOverIntegerWithBreak() throws IOException, TemplateException {
    //     Template template = new Template("test");
    //     template.parse("{{range $i := 10}}{{if eq $i 3}}{{break}}{{end}}{{$i}} {{end}}");
    //
    //     StringWriter writer = new StringWriter();
    //     template.execute(writer, null);
    //
    //     assertEquals("0 1 2 ", writer.toString());
    // }
    //
    // @Test
    // public void testRangeOverIntegerWithContinue() throws IOException, TemplateException {
    //     Template template = new Template("test");
    //     template.parse("{{range $i := 5}}{{if eq $i 2}}{{continue}}{{end}}{{$i}} {{end}}");
    //
    //     StringWriter writer = new StringWriter();
    //     template.execute(writer, null);
    //
    //     assertEquals("0 1 3 4 ", writer.toString());
    // }

    @Test
    public void testRangeOverLongValue() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $i := .Count}}{{$i}} {{end}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Count", 4L); // Long value

        StringWriter writer = new StringWriter();
        template.execute(writer, data);

        assertEquals("0 1 2 3 ", writer.toString());
    }

    @Test
    public void testRangeOverIntegerInNestedContext() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range $outer := 2}}Outer:{{$outer}} {{range $inner := 2}}{{$inner}}{{end}} | {{end}}");

        StringWriter writer = new StringWriter();
        template.execute(writer, null);

        assertEquals("Outer:0 01 | Outer:1 01 | ", writer.toString());
    }
}
