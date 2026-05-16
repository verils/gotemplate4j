package io.github.verils.gotemplate;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for gotemplate4j.
 * <p>
 * To run benchmarks:
 * <pre>
 * ./mvnw test-compile exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateJmhBenchmark"
 * </pre>
 * <p>
 * Or run specific benchmark:
 * <pre>
 * ./mvnw test-compile exec:java -Dexec.mainClass="io.github.verils.gotemplate.TemplateJmhBenchmark" -Dexec.args="TemplateJmhBenchmark.parseBenchmark"
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class TemplateJmhBenchmark {

    private Template executeTemplate;
    private Template beanTemplate;
    private Template mapTemplate;
    private Template rangeTemplate;
    private Template functionTemplate;
    
    private Map<String, Object> executeData;
    private BeanData beanData;
    private Map<String, Object> mapData;
    private Map<String, Object> rangeData;
    private Map<String, Object> functionData;

    @Setup
    public void setup() throws Exception {
        // Execute benchmark template
        executeTemplate = new Template("execute");
        executeTemplate.parse("Hello {{.Name}}, you have {{.Count}} items.");
        executeData = new HashMap<>();
        executeData.put("Name", "BenchmarkUser");
        executeData.put("Count", 42);
        
        // Bean access template
        beanTemplate = new Template("bean");
        beanTemplate.parse("{{.Name}} {{.Value}}");
        beanData = new BeanData();
        
        // Map access template
        mapTemplate = new Template("map");
        mapTemplate.parse("{{.Name}} {{.Value}}");
        mapData = new HashMap<>();
        mapData.put("Name", "MapUser");
        mapData.put("Value", 123);
        
        // Range benchmark template
        rangeTemplate = new Template("range");
        rangeTemplate.parse("{{range .Items}}{{.}}{{end}}");
        String[] items = new String[100];
        for (int i = 0; i < 100; i++) items[i] = "Item" + i;
        rangeData = new HashMap<>();
        rangeData.put("Items", items);
        
        // Function benchmark template
        Map<String, Function> funcs = new HashMap<>();
        funcs.put("upper", args -> String.valueOf(args[0]).toUpperCase());
        funcs.put("lower", args -> String.valueOf(args[0]).toLowerCase());
        functionTemplate = new Template("func", funcs);
        functionTemplate.parse("{{upper .Name}} {{lower .Desc}} {{len .Items}}");
        functionData = new HashMap<>();
        functionData.put("Name", "BENCHMARK");
        functionData.put("Desc", "TESTING");
        functionData.put("Items", new String[50]);
    }

    @Benchmark
    public void parseBenchmark(Blackhole bh) throws Exception {
        String templateText = "Hello {{.Name}}, you have {{.Count}} items. {{if .Active}}Active{{else}}Inactive{{end}}";
        Template t = new Template("bench");
        t.parse(templateText);
        bh.consume(t);
    }

    @Benchmark
    public void executeBenchmark(Blackhole bh) throws Exception {
        StringWriter w = new StringWriter();
        executeTemplate.execute(w, executeData);
        bh.consume(w.toString());
    }

    @Benchmark
    public void beanAccessBenchmark(Blackhole bh) throws Exception {
        StringWriter w = new StringWriter();
        beanTemplate.execute(w, beanData);
        bh.consume(w.toString());
    }

    @Benchmark
    public void mapAccessBenchmark(Blackhole bh) throws Exception {
        StringWriter w = new StringWriter();
        mapTemplate.execute(w, mapData);
        bh.consume(w.toString());
    }

    @Benchmark
    public void rangeHeavyBenchmark(Blackhole bh) throws Exception {
        StringWriter w = new StringWriter();
        rangeTemplate.execute(w, rangeData);
        bh.consume(w.toString());
    }

    @Benchmark
    public void functionHeavyBenchmark(Blackhole bh) throws Exception {
        StringWriter w = new StringWriter();
        functionTemplate.execute(w, functionData);
        bh.consume(w.toString());
    }

    public static class BeanData {
        public String getName() { return "BeanUser"; }
        public int getValue() { return 123; }
    }

    /**
     * Main method to run benchmarks from IDE or command line.
     */
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(TemplateJmhBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(3)
                .warmupTime(org.openjdk.jmh.runner.options.TimeValue.seconds(2))
                .measurementIterations(5)
                .measurementTime(org.openjdk.jmh.runner.options.TimeValue.seconds(3))
                .forks(0)
                .build();

        new Runner(opt).run();
    }
}
