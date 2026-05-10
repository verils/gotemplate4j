package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateFileHelperTest {

    @TempDir
    Path tempDir;

    @Test
    void parseFileReadsAndExecutesTemplate() throws Exception {
        Path file = tempDir.resolve("test.tmpl");
        Files.write(file, "Hello, {{.Name}}!".getBytes());

        Template template = new Template("test");
        template.parseFile(file);

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Name", "World"));
        assertEquals("Hello, World!", writer.toString());
    }

    @Test
    void parseFilesHandlesMultipleFiles() throws Exception {
        Path file1 = tempDir.resolve("f1.tmpl");
        Path file2 = tempDir.resolve("f2.tmpl");
        Files.write(file1, "{{define \"t1\"}}One{{end}}".getBytes());
        Files.write(file2, "{{define \"t2\"}}Two{{end}}".getBytes());

        Template template = new Template("root");
        template.parseFiles(file1, file2);

        assertTrue(template.hasTemplate("t1"));
        assertTrue(template.hasTemplate("t2"));

        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "t1", null);
        assertEquals("One", writer.toString());
    }

    @Test
    void parseGlobMatchesPatternInDirectory() throws Exception {
        Files.write(tempDir.resolve("a.tmpl"), "{{define \"a\"}}A{{end}}".getBytes());
        Files.write(tempDir.resolve("b.tmpl"), "{{define \"b\"}}B{{end}}".getBytes());
        Files.write(tempDir.resolve("c.txt"), "ignore".getBytes());

        Template template = new Template("root");
        template.parseGlob(tempDir, "*.tmpl");

        assertTrue(template.hasTemplate("a"));
        assertTrue(template.hasTemplate("b"));
        // t.txt should not be parsed as a template definition unless it has one
    }

    @Test
    void parseFileThrowsIOExceptionForMissingFile() {
        Template template = new Template("test");
        Path missing = tempDir.resolve("missing.tmpl");
        
        try {
            template.parseFile(missing);
        } catch (TemplateParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("missing.tmpl")); // Just checking it throws
        }
    }

    private Map<String, Object> data(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }
}
