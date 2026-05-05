package io.github.verils.gotemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class TemplateTestSupport {

    private TemplateTestSupport() {
    }

    static String render(String source) throws IOException, TemplateException {
        return render(source, null);
    }

    static String render(String source, Object data) throws IOException, TemplateException {
        Template template = new Template("test");
        template.parse(source);

        StringWriter writer = new StringWriter();
        template.execute(writer, data);
        return writer.toString();
    }

    static Map<String, Object> data(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("entries must be key/value pairs");
        }

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            data.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return data;
    }

    static Object invoke(String name, Object... args) {
        Function function = Functions.BUILTIN.get(name);
        assertNotNull(function, "missing builtin function: " + name);
        return function.invoke(args);
    }
}
