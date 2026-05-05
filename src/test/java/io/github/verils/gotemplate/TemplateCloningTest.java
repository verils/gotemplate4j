package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for template cloning and thread safety.
 */
public class TemplateCloningTest {

    @Test
    void testCloneCreatesIndependentCopy() throws IOException, TemplateException {
        Template original = new Template("test");
        original.parse("Hello, {{.Name}}!");
        
        // Clone the template
        Template clone = new Template(original);
        
        // Verify they are different instances
        assertNotSame(original, clone);
        
        // Both should produce the same result
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        StringWriter writer1 = new StringWriter();
        original.execute(writer1, data);
        
        StringWriter writer2 = new StringWriter();
        clone.execute(writer2, data);
        
        assertEquals(writer1.toString(), writer2.toString());
        assertEquals("Hello, World!", writer1.toString());
    }

    @Test
    void testCloneWithMultipleTemplates() throws IOException, TemplateException {
        Template original = new Template("master");
        original.parse(
            "{{define \"header\"}}Header{{end}}" +
            "{{define \"footer\"}}Footer{{end}}" +
            "{{block \"content\" .}}Default{{end}}"
        );

        Template clone = new Template(original);
        
        // Override in clone should not affect original
        clone.parse("{{define \"content\"}}Custom{{end}}");
        
        StringWriter writer1 = new StringWriter();
        original.execute(writer1, null);
        
        StringWriter writer2 = new StringWriter();
        clone.execute(writer2, null);
        
        // Original should use default content
        assertEquals("Default", writer1.toString());
        // Clone should use custom content
        assertEquals("Custom", writer2.toString());
    }

    @Test
    void testCloneWithFunctions() throws IOException, TemplateException {
        Map<String, Function> functions = new HashMap<>();
        functions.put("upper", args -> ((String) args[0]).toUpperCase());
        
        Template original = new Template("test", functions);
        original.parse("{{.text | upper}}");

        Template clone = new Template(original);
        
        Map<String, Object> data = new HashMap<>();
        data.put("text", "hello");
        
        StringWriter writer1 = new StringWriter();
        original.execute(writer1, data);
        
        StringWriter writer2 = new StringWriter();
        clone.execute(writer2, data);
        
        assertEquals("HELLO", writer1.toString());
        assertEquals("HELLO", writer2.toString());
    }

    @Test
    void testCloneWithCustomDelimiters() throws IOException, TemplateException {
        Template original = new Template("test", "<%", "%>");
        original.parse("Hello, <% .Name %>!");

        Template clone = new Template(original);
        
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "World");
        
        StringWriter writer1 = new StringWriter();
        original.execute(writer1, data);
        
        StringWriter writer2 = new StringWriter();
        clone.execute(writer2, data);
        
        assertEquals("Hello, World!", writer1.toString());
        assertEquals("Hello, World!", writer2.toString());
    }

    @Test
    void testConcurrentExecutionWithClone() throws InterruptedException, IOException, TemplateException {
        final Template original = new Template("test");
        original.parse("Thread {{.threadId}}: {{.message}}");
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final String[] results = new String[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // Each thread clones the template
                    Template clone = new Template(original);
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("threadId", threadId);
                    data.put("message", "Processing");
                    
                    StringWriter writer = new StringWriter();
                    clone.execute(writer, data);
                    results[threadId] = writer.toString();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Verify all threads completed successfully
        for (int i = 0; i < threadCount; i++) {
            assertEquals("Thread " + i + ": Processing", results[i]);
        }
    }

    @Test
    void testConcurrentParsingAndExecution() throws InterruptedException, IOException, TemplateException {
        // Test that parsing in one thread doesn't affect execution in another
        final Template template1 = new Template("t1");
        final Template template2 = new Template("t2");
        
        CountDownLatch parseLatch = new CountDownLatch(2);
        CountDownLatch execLatch = new CountDownLatch(2);
        
        final String[] result1 = new String[1];
        final String[] result2 = new String[1];
        
        // Thread 1: Parse and execute template1
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                template1.parse("Template 1: {{.value}}");
                parseLatch.countDown();
                
                Map<String, Object> data = new HashMap<>();
                data.put("value", "A");
                
                StringWriter writer = new StringWriter();
                template1.execute(writer, data);
                result1[0] = writer.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                execLatch.countDown();
            }
        });
        
        // Thread 2: Parse and execute template2
        executor.submit(() -> {
            try {
                template2.parse("Template 2: {{.value}}");
                parseLatch.countDown();
                
                Map<String, Object> data = new HashMap<>();
                data.put("value", "B");
                
                StringWriter writer = new StringWriter();
                template2.execute(writer, data);
                result2[0] = writer.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                execLatch.countDown();
            }
        });
        
        execLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertEquals("Template 1: A", result1[0]);
        assertEquals("Template 2: B", result2[0]);
    }

    @Test
    void testClonePreservesAllDefinedTemplates() throws IOException, TemplateException {
        Template original = new Template("master");
        original.parse(
            "{{define \"tmpl1\"}}Template 1{{end}}" +
            "{{define \"tmpl2\"}}Template 2{{end}}" +
            "{{define \"tmpl3\"}}Template 3{{end}}"
        );

        Template clone = new Template(original);
        
        // Execute each defined template from clone
        StringWriter writer1 = new StringWriter();
        clone.executeTemplate(writer1, "tmpl1", null);
        
        StringWriter writer2 = new StringWriter();
        clone.executeTemplate(writer2, "tmpl2", null);
        
        StringWriter writer3 = new StringWriter();
        clone.executeTemplate(writer3, "tmpl3", null);
        
        assertEquals("Template 1", writer1.toString());
        assertEquals("Template 2", writer2.toString());
        assertEquals("Template 3", writer3.toString());
    }

    @Test
    void testCloneIndependenceAfterModification() throws IOException, TemplateException {
        Template original = new Template("test");
        original.parse("Original: {{.value}}");

        Template clone = new Template(original);
        
        // Modify original by parsing additional content
        original.parse("{{define \"helper\"}}Helper{{end}}");
        
        // Clone should not have the helper template
        StringWriter writer1 = new StringWriter();
        original.execute(writer1, createData("A"));
        
        StringWriter writer2 = new StringWriter();
        clone.execute(writer2, createData("B"));
        
        assertEquals("Original: A", writer1.toString());
        assertEquals("Original: B", writer2.toString());
        
        // Original should have helper, clone should not
        StringWriter helperWriter = new StringWriter();
        original.executeTemplate(helperWriter, "helper", null);
        assertEquals("Helper", helperWriter.toString());
        
        // Trying to execute non-existent template in clone should fail
        try {
            StringWriter cloneHelperWriter = new StringWriter();
            clone.executeTemplate(cloneHelperWriter, "helper", null);
            assertTrue(false, "Should have thrown exception");
        } catch (TemplateNotFoundException e) {
            // Expected
        }
    }

    private Map<String, Object> createData(String value) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", value);
        return data;
    }
}
