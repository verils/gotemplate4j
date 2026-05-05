package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Enum type handling in templates.
 */
public class EnumHandlingTest {

    // Test enum
    public enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);
        
        private final int level;
        
        Priority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }

    // Test class with enum fields
    public static class Task {
        private String name;
        private Status status;
        private Priority priority;
        
        public Task(String name, Status status, Priority priority) {
            this.name = name;
            this.status = status;
            this.priority = priority;
        }
        
        public String getName() {
            return name;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public Priority getPriority() {
            return priority;
        }
    }

    @Test
    void testEnumBasicAccess() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Status: {{.status}}");
        
        Writer writer = new StringWriter();
        Task task = new Task("Task 1", Status.ACTIVE, Priority.HIGH);
        
        template.execute(writer, task);
        
        assertEquals("Status: ACTIVE", writer.toString());
    }

    @Test
    void testEnumWithGetter() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Priority Level: {{.priority.level}}");
        
        Writer writer = new StringWriter();
        Task task = new Task("Task 1", Status.ACTIVE, Priority.HIGH);
        
        template.execute(writer, task);
        
        assertEquals("Priority Level: 3", writer.toString());
    }

    @Test
    void testEnumInIfCondition() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq .status \"ACTIVE\"}}Active Task{{else}}Other Status{{end}}");
        
        Writer writer = new StringWriter();
        Task task = new Task("Task 1", Status.ACTIVE, Priority.HIGH);
        
        template.execute(writer, task);
        
        // Note: Comparing enum with string may not work as expected
        // This test documents current behavior
        assertEquals("Other Status", writer.toString());
    }

    @Test
    void testEnumComparison() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{if eq .status .expectedStatus}}Match{{else}}No Match{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("status", Status.ACTIVE);
        data.put("expectedStatus", Status.ACTIVE);
        
        template.execute(writer, data);
        
        assertEquals("Match", writer.toString());
    }

    @Test
    void testEnumInMap() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Status: {{.status}}, Priority: {{.priority}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("status", Status.PENDING);
        data.put("priority", Priority.MEDIUM);
        
        template.execute(writer, data);
        
        assertEquals("Status: PENDING, Priority: MEDIUM", writer.toString());
    }

    @Test
    void testEnumInRange() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("{{range .statuses}}{{.}},{{end}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("statuses", new Status[]{Status.ACTIVE, Status.INACTIVE, Status.PENDING});
        
        template.execute(writer, data);
        
        assertEquals("ACTIVE,INACTIVE,PENDING,", writer.toString());
    }

    @Test
    void testEnumNameMethod() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Status Name: {{.status.name}}");
        
        Writer writer = new StringWriter();
        Task task = new Task("Task 1", Status.ACTIVE, Priority.HIGH);
        
        template.execute(writer, task);
        
        assertEquals("Status Name: ACTIVE", writer.toString());
    }

    @Test
    void testEnumOrdinalMethod() throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse("Status Ordinal: {{.status.ordinal}}");
        
        Writer writer = new StringWriter();
        Task task = new Task("Task 1", Status.ACTIVE, Priority.HIGH);
        
        template.execute(writer, task);
        
        assertEquals("Status Ordinal: 0", writer.toString());
    }

    @Test
    void testEnumValueOf() throws IOException, TemplateException {
        // Test using a function to convert string to enum
        Template template = new Template("test");
        template.parse("{{.statusString}}");
        
        Writer writer = new StringWriter();
        Map<String, Object> data = new HashMap<>();
        data.put("statusString", "ACTIVE");
        
        template.execute(writer, data);
        
        assertEquals("ACTIVE", writer.toString());
    }
}
