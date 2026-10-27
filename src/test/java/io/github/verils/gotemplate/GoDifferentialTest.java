package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GoDifferentialTest {

    @Test
    void coreTemplateSemanticsMatchGoTextTemplate() throws Exception {
        assumeTrue(goAvailable(), "Go toolchain is not available");

        Map<String, Object> sortedMap = new LinkedHashMap<>();
        sortedMap.put("b", 2);
        sortedMap.put("a", 1);

        List<TemplateCase> cases = new ArrayList<>();
        cases.add(new TemplateCase("Hello {{.}}", "World"));
        cases.add(new TemplateCase("{{if true}}yes{{else}}no{{end}}", null));
        cases.add(new TemplateCase("{{with \"value\"}}{{.}}{{else}}empty{{end}}", null));
        cases.add(new TemplateCase("{{range .}}[{{.}}]{{end}}", List.of("a", "b")));
        cases.add(new TemplateCase("{{range .}}value{{else}}empty{{end}}", List.of()));
        cases.add(new TemplateCase("{{eq 1 1 2}}", null));
        cases.add(new TemplateCase("{{ne 1 2}}", null));
        cases.add(new TemplateCase("{{and true \"x\"}}", null));
        cases.add(new TemplateCase("{{or false \"x\"}}", null));
        cases.add(new TemplateCase("{{not false}}", null));
        cases.add(new TemplateCase("{{print \"hello\" \"world\"}}", null));
        cases.add(new TemplateCase("{{printf \"%s:%d\" \"n\" 3}}", null));
        cases.add(new TemplateCase("{{len \"abc\"}}", null));
        cases.add(new TemplateCase("{{index . 1}}", List.of("a", "b")));
        cases.add(new TemplateCase("{{len (slice . 1 3)}}", List.of("a", "b", "c", "d")));
        cases.add(new TemplateCase("{{slice \"abc\" 1 3}}", null));
        cases.add(new TemplateCase("{{html \"<b>\"}}", null));
        cases.add(new TemplateCase("{{js \"</script>\"}}", null));
        cases.add(new TemplateCase("{{urlquery \"a b\"}}", null));
        cases.add(new TemplateCase("{{range .}}{{.}}{{end}}", sortedMap));
        cases.add(new TemplateCase("a{{/* comment */}}b", null));
        cases.add(new TemplateCase("{{block \"content\" .}}body{{end}}", null));
        cases.add(new TemplateCase("{{define \"child\"}}child{{end}}", null));
        cases.add(new TemplateCase("{{template \"missing\"}}", null));
        cases.add(new TemplateCase(
                "{{define \"item\"}}first{{end}}{{define \"item\"}}second{{end}}", null));

        List<ProbeResult> goResults = runGoProbe(cases);
        assertEquals(cases.size(), goResults.size());

        for (int i = 0; i < cases.size(); i++) {
            TemplateCase testCase = cases.get(i);
            ProbeResult goResult = goResults.get(i);
            ProbeResult javaResult = runJava(testCase);

            assertEquals(goResult.error, javaResult.error,
                    "Go and Java disagree on success/error for case " + i + ": "
                            + testCase.template + "\nGo: " + goResult.value
                            + "\nJava: " + javaResult.value);
            if (!goResult.error) {
                assertEquals(goResult.value, javaResult.value,
                        "Output mismatch for case " + i + ": " + testCase.template);
            }
        }
    }

    private static ProbeResult runJava(TemplateCase testCase) {
        try {
            Template template = new Template("differential");
            template.parse(testCase.template);
            StringWriter writer = new StringWriter();
            template.execute(writer, testCase.data);
            return ProbeResult.output(writer.toString());
        } catch (TemplateException | IOException e) {
            return ProbeResult.error(e.getMessage());
        }
    }

    private static List<ProbeResult> runGoProbe(List<TemplateCase> cases) throws Exception {
        URL helper = GoDifferentialTest.class.getResource("/go/template_probe.go");
        assertTrue(helper != null, "Go differential helper is missing");

        Path helperPath = Files.createTempFile("gotemplate4j-differential-", ".go");
        try (InputStream input = helper.openStream()) {
            Files.copy(input, helperPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        Process process = new ProcessBuilder("go", "run", helperPath.toString()).start();
        try (Writer writer = new OutputStreamWriter(
                process.getOutputStream(), StandardCharsets.UTF_8)) {
            for (TemplateCase testCase : cases) {
                writer.write(encodeRequest(testCase));
                writer.write('\n');
            }
        }

        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("Go differential probe timed out");
        }

        String standardError = new String(
                process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), "Go differential probe failed:\n" + standardError);

        List<ProbeResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(decodeResult(line));
            }
        } finally {
            Files.deleteIfExists(helperPath);
        }
        return results;
    }

    private static boolean goAvailable() {
        try {
            Process process = new ProcessBuilder("go", "list", "text/template")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private static String encodeRequest(TemplateCase testCase) {
        Base64.Encoder encoder = Base64.getEncoder();
        String template = encoder.encodeToString(
                testCase.template.getBytes(StandardCharsets.UTF_8));
        String data = encoder.encodeToString(
                toJson(testCase.data).getBytes(StandardCharsets.UTF_8));
        return template + '\t' + data;
    }

    private static ProbeResult decodeResult(String line) {
        String[] parts = line.split("\\t", 2);
        assertEquals(2, parts.length, "Malformed Go differential response: " + line);
        String value = new String(
                Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return "E".equals(parts[0]) ? ProbeResult.error(value) : ProbeResult.output(value);
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String || value instanceof Character) {
            return quoteJson(String.valueOf(value));
        }
        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }
        if (value instanceof Map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    json.append(',');
                }
                json.append(quoteJson(String.valueOf(entry.getKey())))
                        .append(':')
                        .append(toJson(entry.getValue()));
                first = false;
            }
            return json.append('}').toString();
        }
        if (value instanceof Collection) {
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            for (Object element : (Collection<?>) value) {
                if (!first) {
                    json.append(',');
                }
                json.append(toJson(element));
                first = false;
            }
            return json.append(']').toString();
        }
        if (value.getClass().isArray()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append(toJson(Array.get(value, i)));
            }
            return json.append(']').toString();
        }
        return quoteJson(String.valueOf(value));
    }

    private static String quoteJson(String value) {
        StringBuilder json = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        json.append(String.format("\\u%04x", (int) ch));
                    } else {
                        json.append(ch);
                    }
            }
        }
        return json.append('"').toString();
    }

    private static final class TemplateCase {
        private final String template;
        private final Object data;

        private TemplateCase(String template, Object data) {
            this.template = template;
            this.data = data;
        }
    }

    private static final class ProbeResult {
        private final boolean error;
        private final String value;

        private ProbeResult(boolean error, String value) {
            this.error = error;
            this.value = value;
        }

        private static ProbeResult output(String value) {
            return new ProbeResult(false, value);
        }

        private static ProbeResult error(String value) {
            return new ProbeResult(true, value);
        }
    }
}
