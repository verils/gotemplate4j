package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for enhanced function call error diagnostics.
 */
public class EnhancedFunctionErrorTest {

    @Test
    public void testFunctionArgumentMismatch() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{len}}");  // len requires exactly 1 argument - this will fail at execution time
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", new String[]{"a", "b", "c"});
        
        StringWriter writer = new StringWriter();
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class, 
            () -> template.execute(writer, data));
        
        String message = exception.getMessage();
        assertTrue(message.contains("len"), "Error message should mention the function name");
        assertTrue(message.contains("argument") || message.contains("requires"), 
            "Error message should indicate argument requirements");
    }

    @Test
    public void testFunctionWithWrongArgumentType() throws TemplateException {
        Template template = new Template("test");
        template.parse("{{len 42}}");  // len expects collection/string, not number - fails at execution
        
        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class, 
            () -> template.execute(writer, data));
        
        String message = exception.getMessage();
        assertTrue(message.contains("len"), "Error message should mention the function name");
        // Check for enhanced error with argument count
        assertTrue(message.contains("argument"), "Error message should include argument information");
    }

    @Test
    public void testCustomFunctionError() throws TemplateException {
        Map<String, Function> customFunctions = new HashMap<>();
        customFunctions.put("customFunc", args -> {
            if (args.length != 2) {
                throw new IllegalArgumentException("customFunc requires exactly 2 arguments");
            }
            return args[0] + "-" + args[1];
        });
        
        Template template = new Template("test", customFunctions);
        template.parse("{{customFunc \"only_one_arg\"}}");  // Wrong number of args - fails at execution
        
        Map<String, Object> data = new HashMap<>();
        
        StringWriter writer = new StringWriter();
        TemplateExecutionException exception = assertThrows(TemplateExecutionException.class, 
            () -> template.execute(writer, data));
        
        String message = exception.getMessage();
        assertTrue(message.contains("customFunc"), "Error message should mention the custom function name");
        assertTrue(message.contains("argument") || message.contains("requires"), 
            "Error message should indicate argument requirements");
    }

    @Test
    public void testUndefinedFunctionParseErrorWithSuggestions() {
        Template template = new Template("test");
        
        // This should fail at parse time with enhanced error message
        TemplateParseException exception = assertThrows(TemplateParseException.class, 
            () -> template.parse("{{undefinedFunc .}}"));
        
        String message = exception.getMessage();
        assertTrue(message.contains("undefinedFunc"), "Error message should contain the undefined function name");
        // Check if it lists available functions
        assertTrue(message.contains("Available functions"), 
            "Error message should list available functions");
    }

    @Test
    public void testTypoInFunctionNameParseError() {
        Template template = new Template("test");
        
        // This should fail at parse time with enhanced error message suggesting 'len'
        TemplateParseException exception = assertThrows(TemplateParseException.class, 
            () -> template.parse("{{leng .items}}"));
        
        String message = exception.getMessage();
        assertTrue(message.contains("leng"), "Error message should contain the misspelled function name");
        // Should suggest 'len' as a correction
        assertTrue(message.toLowerCase().contains("len"), 
            "Error message should suggest the correct function name 'len'");
    }
}
