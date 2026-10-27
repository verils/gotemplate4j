package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

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
        Files.write(file1, "One {{.Value}}".getBytes());
        Files.write(file2, "Two {{.Value}}".getBytes());

        Template template = new Template("root");
        template.parseFiles(file1, file2);

        assertTrue(template.hasTemplate("f1.tmpl"));
        assertTrue(template.hasTemplate("f2.tmpl"));

        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "f1.tmpl", data("Value", 1));
        assertEquals("One 1", writer.toString());

        writer = new StringWriter();
        template.executeTemplate(writer, "f2.tmpl", data("Value", 2));
        assertEquals("Two 2", writer.toString());
    }

    @Test
    void parseGlobMatchesPatternInDirectory() throws Exception {
        Files.write(tempDir.resolve("a.tmpl"), "A".getBytes());
        Files.write(tempDir.resolve("b.tmpl"), "B".getBytes());
        Files.write(tempDir.resolve("c.txt"), "ignore".getBytes());

        Template template = new Template("root");
        template.parseGlob(tempDir, "*.tmpl");

        assertTrue(template.hasTemplate("a.tmpl"));
        assertTrue(template.hasTemplate("b.tmpl"));
        assertFalse(template.hasTemplate("c.txt"));
    }

    @Test
    void parseFileThrowsIOExceptionForMissingFile() {
        Template template = new Template("test");
        Path missing = tempDir.resolve("missing.tmpl");

        IOException exception = assertThrows(IOException.class, () -> template.parseFile(missing));
        assertTrue(exception.getMessage().contains("missing.tmpl"));
    }

    // ========== Classpath Loading Tests ==========

    @Test
    void parseFromClasspathLoadsResource() throws Exception {
        Template template = new Template("test");
        template.parseFromClasspath("/file1.tmpl");

        assertTrue(template.hasTemplate("x"));
        assertTrue(template.hasTemplate("dotV"));

        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "x", null);
        assertEquals("TEXT", writer.toString());
    }

    @Test
    void parseFromClasspathWithRelativePath() throws Exception {
        Template template = new Template("test");
        // Relative path (without leading slash)
        template.parseFromClasspath("tmpl1.tmpl");

        assertTrue(template.hasTemplate("x"));
    }

    @Test
    void parseFromClasspathThrowsExceptionForResourceNotFound() {
        Template template = new Template("test");

        IOException exception = assertThrows(IOException.class, () -> {
            template.parseFromClasspath("/nonexistent/template.tmpl");
        });

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains("classpath resource"));
    }

    @Test
    void parseFromClasspathThrowsExceptionForNullPath() {
        Template template = new Template("test");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            template.parseFromClasspath(null);
        });

        assertEquals("Classpath pattern cannot be null or empty", exception.getMessage());
    }

    @Test
    void parseFromClasspathThrowsExceptionForEmptyPath() {
        Template template = new Template("test");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            template.parseFromClasspath("");
        });

        assertEquals("Classpath pattern cannot be null or empty", exception.getMessage());
    }

    // ========== Encoding Support Tests ==========

    @Test
    void parseFileWithCharset() throws Exception {
        Path file = tempDir.resolve("utf8.tmpl");
        String content = "Hello, {{.Name}}! 你好世界";
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));

        Template template = new Template("test");
        template.parseFile(file, StandardCharsets.UTF_8);

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Name", "World"));
        assertEquals("Hello, World! 你好世界", writer.toString());
    }

    @Test
    void parseInputStreamWithCharset() throws Exception {
        String content = "Hello, {{.Name}}! 你好世界";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        Template template = new Template("test");
        // Using Charset for type safety
        template.parse(inputStream, StandardCharsets.UTF_8);

        StringWriter writer = new StringWriter();
        template.execute(writer, data("Name", "World"));
        assertEquals("Hello, World! 你好世界", writer.toString());
    }

    // ========== Directory Parsing Tests ==========

    @Test
    void parseDirectoryLoadsAllTmplFiles() throws Exception {
        Files.write(tempDir.resolve("a.tmpl"), "A".getBytes());
        Files.write(tempDir.resolve("b.tmpl"), "B".getBytes());
        Files.write(tempDir.resolve("c.txt"), "ignore".getBytes());

        Template template = new Template("root");
        template.parseDirectory(tempDir);

        assertTrue(template.hasTemplate("a.tmpl"));
        assertTrue(template.hasTemplate("b.tmpl"));
        assertFalse(template.hasTemplate("c.txt"));

        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "a.tmpl", null);
        assertEquals("A", writer.toString());
        writer = new StringWriter();
        template.executeTemplate(writer, "b.tmpl", null);
        assertEquals("B", writer.toString());
    }

    @Test
    void parseDirectoryThrowsExceptionForNonExistentDirectory() {
        Template template = new Template("test");
        Path nonExistent = tempDir.resolve("nonexistent");

        IOException exception = assertThrows(IOException.class, () -> {
            template.parseDirectory(nonExistent);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void parseDirectoryThrowsExceptionForFileNotDirectory() throws Exception {
        Path file = tempDir.resolve("file.tmpl");
        Files.write(file, "content".getBytes());

        Template template = new Template("test");

        IOException exception = assertThrows(IOException.class, () -> {
            template.parseDirectory(file);
        });

        assertTrue(exception.getMessage().contains("not a directory"));
    }

    @Test
    void parseDirectoryRejectsEmptyDirectory() throws Exception {
        Path empty = Files.createDirectory(tempDir.resolve("empty"));
        Template template = new Template("test");

        IOException exception = assertThrows(IOException.class, () -> template.parseDirectory(empty));

        assertTrue(exception.getMessage().contains("No template files matched"));
    }

    @Test
    void parseGlobSupportsNestedFullGlobPatterns() throws Exception {
        Path pages = Files.createDirectories(tempDir.resolve("pages/admin"));
        Files.write(pages.resolve("page5.tmpl"), "Five".getBytes());
        Files.write(tempDir.resolve("pages/page1.tmpl"), "One".getBytes());
        Files.write(tempDir.resolve("pages/pageA.tmpl"), "Letter".getBytes());

        Template template = new Template("root");
        template.parseGlob(tempDir, "pages/**/page[0-9].tmpl");

        assertTrue(template.hasTemplate("page1.tmpl"));
        assertTrue(template.hasTemplate("page5.tmpl"));
        assertFalse(template.hasTemplate("pageA.tmpl"));
    }

    @Test
    void batchLoadingRejectsDuplicateFileNames() throws Exception {
        Path first = Files.createDirectories(tempDir.resolve("first"));
        Path second = Files.createDirectories(tempDir.resolve("second"));
        Path f1 = Files.write(first.resolve("shared.tmpl"), "One".getBytes());
        Path f2 = Files.write(second.resolve("shared.tmpl"), "Two".getBytes());

        Template template = new Template("root");
        IOException parseFilesError = assertThrows(IOException.class,
                () -> template.parseFiles(f1, f2));
        assertTrue(parseFilesError.getMessage().contains("shared.tmpl"));

        IOException parseGlobError = assertThrows(IOException.class,
                () -> template.parseGlob(tempDir, "**/*.tmpl"));
        assertTrue(parseGlobError.getMessage().contains("shared.tmpl"));
    }

    // ========== Static Batch Loading Tests ==========

    @Test
    void parseClasspathResourcesLoadsMultipleTemplates() throws Exception {
        Template template = Template.parseClasspathResources("/file*.tmpl");

        assertTrue(template.hasTemplate("file1.tmpl"));
        assertTrue(template.hasTemplate("file2.tmpl"));
        assertTrue(template.hasTemplate("x"));
        assertTrue(template.hasTemplate("dot"));

        StringWriter writer = new StringWriter();
        template.executeTemplate(writer, "x", null);
        assertEquals("TEXT", writer.toString());
    }

    @Test
    void parseClasspathResourcesThrowsExceptionForNullPattern() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Template.parseClasspathResources(null);
        });

        assertEquals("Classpath pattern cannot be null or empty", exception.getMessage());
    }

    @Test
    void parseClasspathResourcesThrowsExceptionForEmptyPattern() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Template.parseClasspathResources("");
        });

        assertEquals("Classpath pattern cannot be null or empty", exception.getMessage());
    }

    @Test
    void staticLoadingFactoriesUseCompleteFileNames() throws Exception {
        Path file = tempDir.resolve("page.tmpl");
        Files.write(file, "Hello {{.Name}}".getBytes());

        Template fromFile = Template.fromFile(file);
        assertEquals("page.tmpl", fromFile.name());
        StringWriter writer = new StringWriter();
        fromFile.execute(writer, data("Name", "Alice"));
        assertEquals("Hello Alice", writer.toString());

        Template fromClasspath = Template.fromClasspath("file1.tmpl");
        assertEquals("file1.tmpl", fromClasspath.name());
        assertTrue(fromClasspath.hasTemplate("x"));
    }

    @Test
    void fromDirectoryReturnsIndependentNamedTemplates() throws Exception {
        Files.write(tempDir.resolve("a.tmpl"), "A".getBytes());
        Files.write(tempDir.resolve("b.tmpl"), "B".getBytes());

        Map<String, Template> templates = Template.fromDirectory(tempDir);

        assertEquals(Arrays.asList("a.tmpl", "b.tmpl"), Arrays.asList(templates.keySet().toArray()));
        StringWriter writer = new StringWriter();
        templates.get("a.tmpl").execute(writer, null);
        assertEquals("A", writer.toString());
    }

    @Test
    void fromClasspathResourcesReturnsSortedIndependentTemplates() throws Exception {
        List<Template> templates = Template.fromClasspathResources("/file*.tmpl");

        assertEquals(2, templates.size());
        assertEquals("file1.tmpl", templates.get(0).name());
        assertEquals("file2.tmpl", templates.get(1).name());
        assertTrue(templates.get(0).hasTemplate("dot"));
    }

    @Test
    void classpathPatternRejectsMissingResourcesAndTraversal() {
        IOException missing = assertThrows(IOException.class,
                () -> Template.fromClasspathResources("missing/**/*.tmpl"));
        assertTrue(missing.getMessage().contains("No classpath template resources"));

        assertThrows(IllegalArgumentException.class,
                () -> Template.fromClasspathResources("../secret/*.tmpl"));
    }

    @Test
    void exactClasspathLookupFallsBackAcrossClassLoaders() throws Exception {
        Path empty = Files.createDirectory(tempDir.resolve("empty-classpath"));
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{empty.toUri().toURL()}, previous)) {
            Thread.currentThread().setContextClassLoader(loader);

            Template template = Template.fromClasspath("file1.tmpl");

            assertEquals("file1.tmpl", template.name());
            assertTrue(template.hasTemplate("x"));
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Test
    void classpathGlobScansJarWithoutDirectoryEntries() throws Exception {
        Path jar = createTemplateJar();
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, previous)) {
            Thread.currentThread().setContextClassLoader(loader);

            List<Template> templates = Template.fromClasspathResources("templates/**/*.tmpl");
            assertEquals(2, templates.size());
            assertEquals("a.tmpl", templates.get(0).name());
            assertEquals("b.tmpl", templates.get(1).name());

            StringWriter writer = new StringWriter();
            templates.get(1).execute(writer, data("Value", "jar"));
            assertEquals("B jar", writer.toString());

            Template exact = Template.fromClasspath("/templates/a.tmpl");
            assertEquals("a.tmpl", exact.name());
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    // ========== Error Diagnostics Tests ==========

    @Test
    void parseFileProvidesHelpfulErrorMessage() {
        Template template = new Template("test");
        Path missing = tempDir.resolve("missing_file.tmpl");

        IOException exception = assertThrows(IOException.class, () -> {
            template.parseFile(missing);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("missing_file.tmpl"));
        assertTrue(message.contains("Absolute path"));
        assertTrue(message.contains("Tip"));
    }

    private Map<String, Object> data(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Path createTemplateJar() throws IOException {
        Path jar = tempDir.resolve("templates.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            writeJarEntry(output, "templates/a.tmpl", "A");
            writeJarEntry(output, "templates/nested/b.tmpl", "B {{.Value}}");
        }
        return jar;
    }

    private void writeJarEntry(JarOutputStream output, String name, String content) throws IOException {
        output.putNextEntry(new JarEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }
}
