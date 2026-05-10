package io.github.verils.gotemplate;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * A lightweight benchmark harness for gotemplate4j.
 * <p>
 * This class provides a simple way to measure parse and execute throughput
 * without requiring external dependencies like JMH.
 */
public class TemplateBenchmark {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int MEASURE_ITERATIONS = 10000;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting gotemplate4j Performance Benchmark...");
        System.out.println("Warmup: " + WARMUP_ITERATIONS + " iterations");
        System.out.println("Measurement: " + MEASURE_ITERATIONS + " iterations");
        System.out.println("--------------------------------------------------");

        runParseBenchmark();
        runExecuteBenchmark();
        runAccessPatternBenchmark();
        runRangeHeavyBenchmark();
        runFunctionHeavyBenchmark();

        System.out.println("--------------------------------------------------");
        System.out.println("Benchmark completed.");
    }

    private static void runParseBenchmark() throws Exception {
        String templateText = "Hello {{.Name}}, you have {{.Count}} items. {{if .Active}}Active{{else}}Inactive{{end}}";
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Template t = new Template("bench");
            t.parse(templateText);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            Template t = new Template("bench");
            t.parse(templateText);
        }
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        double opsPerSec = MEASURE_ITERATIONS / seconds;
        System.out.printf("[PARSE] Throughput: %.2f ops/sec (%.2f ms/op)%n", opsPerSec, (seconds / MEASURE_ITERATIONS) * 1000);
    }

    private static void runExecuteBenchmark() throws Exception {
        Template template = new Template("bench");
        template.parse("Hello {{.Name}}, you have {{.Count}} items.");
        Map<String, Object> data = new HashMap<>();
        data.put("Name", "BenchmarkUser");
        data.put("Count", 42);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            StringWriter w = new StringWriter();
            template.execute(w, data);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            StringWriter w = new StringWriter();
            template.execute(w, data);
        }
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        double opsPerSec = MEASURE_ITERATIONS / seconds;
        System.out.printf("[EXECUTE] Throughput: %.2f ops/sec (%.2f ms/op)%n", opsPerSec, (seconds / MEASURE_ITERATIONS) * 1000);
    }

    private static void runAccessPatternBenchmark() throws Exception {
        // JavaBean Access
        Template beanTemplate = new Template("bean");
        beanTemplate.parse("{{.Name}} {{.Value}}");
        BeanData beanData = new BeanData();

        warmupAndMeasure("BEAN_ACCESS", beanTemplate, beanData);

        // Map Access
        Template mapTemplate = new Template("map");
        mapTemplate.parse("{{.Name}} {{.Value}}");
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("Name", "MapUser");
        mapData.put("Value", 123);

        warmupAndMeasure("MAP_ACCESS", mapTemplate, mapData);
    }

    private static void runRangeHeavyBenchmark() throws Exception {
        Template template = new Template("range");
        template.parse("{{range .Items}}{{.}}{{end}}");
        
        Map<String, Object> data = new HashMap<>();
        String[] items = new String[100];
        for (int i = 0; i < 100; i++) items[i] = "Item" + i;
        data.put("Items", items);

        warmupAndMeasure("RANGE_HEAVY (100 items)", template, data);
    }

    private static void runFunctionHeavyBenchmark() throws Exception {
        Map<String, Function> funcs = new HashMap<>();
        funcs.put("upper", args -> String.valueOf(args[0]).toUpperCase());
        funcs.put("lower", args -> String.valueOf(args[0]).toLowerCase());
        
        Template funcTemplate = new Template("func", funcs);
        funcTemplate.parse("{{upper .Name}} {{lower .Desc}} {{len .Items}}");

        Map<String, Object> data = new HashMap<>();
        data.put("Name", "BENCHMARK");
        data.put("Desc", "TESTING");
        data.put("Items", new String[50]);

        warmupAndMeasure("FUNCTION_HEAVY", funcTemplate, data);
    }

    private static void warmupAndMeasure(String name, Template template, Object data) throws Exception {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            StringWriter w = new StringWriter();
            template.execute(w, data);
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            StringWriter w = new StringWriter();
            template.execute(w, data);
        }
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        double opsPerSec = MEASURE_ITERATIONS / seconds;
        System.out.printf("[%s] Throughput: %.2f ops/sec (%.2f ms/op)%n", name, opsPerSec, (seconds / MEASURE_ITERATIONS) * 1000);
    }

    public static class BeanData {
        public String getName() { return "BeanUser"; }
        public int getValue() { return 123; }
    }
}
