package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for custom type adapter/converter registry.
 * This feature allows registering custom converters for specific types.
 */
public class TypeAdapterTest {

    // Custom class that needs special formatting
    public static class CustomDate {
        private final Date date;
        
        public CustomDate(Date date) {
            this.date = date;
        }
        
        public Date getDate() {
            return date;
        }
    }
    
    // Custom class with special display requirements
    public static class Currency {
        private final double amount;
        
        public Currency(double amount) {
            this.amount = amount;
        }
        
        public double getAmount() {
            return amount;
        }
    }

    @Test
    void testDefaultToStringWithoutAdapter() throws IOException, TemplateException {
        // Without adapter, uses default toString()
        Template template = new Template("test");
        template.parse("Value: {{.value}}");
        
        Currency currency = new Currency(1234.56);
        Map<String, Object> data = new HashMap<>();
        data.put("value", currency);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        // Should use default toString
        String result = writer.toString();
        assertEquals("Value: " + currency.toString(), result);
    }

    @Test
    void testCustomTypeAdapter() throws IOException, TemplateException {
        // Register a custom adapter for Currency type
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatCurrency", args -> {
            Currency currency = (Currency) args[0];
            return String.format("$%.2f", currency.getAmount());
        });
        
        Template template = new Template("test", functions);
        template.parse("Amount: {{.amount | formatCurrency}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new Currency(1234.56));
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Amount: $1234.56", writer.toString());
    }

    @Test
    void testDateFormatterAdapter() throws IOException, TemplateException {
        // Register a date formatter function
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatDate", args -> {
            CustomDate customDate = (CustomDate) args[0];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(customDate.getDate());
        });
        
        Template template = new Template("test", functions);
        template.parse("Date: {{.date | formatDate}}");
        
        Map<String, Object> data = new HashMap<>();
        // Use a specific timestamp that's timezone-independent for testing
        Date testDate = new Date(1234567890000L);
        data.put("date", new CustomDate(testDate));
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        // Format the expected date using the same formatter
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String expectedDate = sdf.format(testDate);
        assertEquals("Date: " + expectedDate, writer.toString());
    }

    @Test
    void testMultipleTypeAdapters() throws IOException, TemplateException {
        // Register multiple custom adapters
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatCurrency", args -> {
            Currency currency = (Currency) args[0];
            return String.format("$%.2f", currency.getAmount());
        });
        functions.put("formatDate", args -> {
            CustomDate customDate = (CustomDate) args[0];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(customDate.getDate());
        });
        
        Template template = new Template("test", functions);
        template.parse("Amount: {{.amount | formatCurrency}}, Date: {{.date | formatDate}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("amount", new Currency(999.99));
        Date testDate = new Date(1234567890000L);
        data.put("date", new CustomDate(testDate));
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String expectedDate = sdf.format(testDate);
        assertEquals("Amount: $999.99, Date: " + expectedDate, writer.toString());
    }

    @Test
    void testAdapterInConditional() throws IOException, TemplateException {
        // Use adapter result in conditional
        Map<String, Function> functions = new HashMap<>();
        functions.put("getCurrencySymbol", args -> {
            String currencyCode = (String) args[0];
            if ("USD".equals(currencyCode)) return "$";
            if ("EUR".equals(currencyCode)) return "€";
            if ("GBP".equals(currencyCode)) return "£";
            return currencyCode;
        });
        
        Template template = new Template("test", functions);
        template.parse("{{if eq .currency \"USD\"}}Price: {{getCurrencySymbol .currency}}{{.amount}}{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("currency", "USD");
        data.put("amount", 100);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Price: $100", writer.toString());
    }

    @Test
    void testAdapterWithNullValue() throws IOException, TemplateException {
        // Test adapter handles missing/null gracefully using if condition
        Map<String, Function> functions = new HashMap<>();
        functions.put("safeString", args -> {
            if (args == null || args.length == 0 || args[0] == null) {
                return "N/A";
            }
            return args[0].toString();
        });
        
        Template template = new Template("test", functions);
        template.parse("{{if .value}}Value: {{.value | safeString}}{{else}}Value: N/A{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("value", null);
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("Value: N/A", writer.toString());
    }

    @Test
    void testChainedAdapters() throws IOException, TemplateException {
        // Test chaining multiple adapters
        Map<String, Function> functions = new HashMap<>();
        functions.put("upper", args -> ((String) args[0]).toUpperCase());
        functions.put("trim", args -> ((String) args[0]).trim());
        
        Template template = new Template("test", functions);
        template.parse("{{.text | trim | upper}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("text", "  hello world  ");
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("HELLO WORLD", writer.toString());
    }

    @Test
    void testAdapterInRange() throws IOException, TemplateException {
        // Use adapter in range loop
        Map<String, Function> functions = new HashMap<>();
        functions.put("formatCurrency", args -> {
            Currency currency = (Currency) args[0];
            return String.format("$%.2f", currency.getAmount());
        });
        
        Template template = new Template("test", functions);
        template.parse("{{range .items}}{{. | formatCurrency}},{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        data.put("items", new Currency[]{
            new Currency(10.50),
            new Currency(20.75),
            new Currency(30.00)
        });
        
        Writer writer = new StringWriter();
        template.execute(writer, data);
        
        assertEquals("$10.50,$20.75,$30.00,", writer.toString());
    }
}
