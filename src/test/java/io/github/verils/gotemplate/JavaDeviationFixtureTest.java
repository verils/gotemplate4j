package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaDeviationFixtureTest {

    @Test
    void javaBeanPublicFieldOptionalAndEnumFixture() throws Exception {
        JavaFixtureData data = new JavaFixtureData();

        assertEquals("bean field optional ALPHA", TemplateTestSupport.render(
                "{{.Name}} {{.publicValue}} {{.Maybe}} {{.Kind.name}}", data));
    }

    @Test
    void missingValuesPrintNoValueByDefaultFixture() throws Exception {
        assertEquals("[<no value>]", TemplateTestSupport.render("[{{.Missing}}]", TemplateTestSupport.data()));
    }

    @Test
    void mapIterationUsesJavaMapOrderFixture() throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("b", 2);
        data.put("a", 1);

        assertEquals("b=2,a=1,", TemplateTestSupport.render("{{range $k, $v := .}}{{$k}}={{$v}},{{end}}", data));
    }

    public static class JavaFixtureData {
        public String publicValue = "field";

        public String getName() {
            return "bean";
        }

        public Optional<String> getMaybe() {
            return Optional.of("optional");
        }

        public FixtureKind getKind() {
            return FixtureKind.ALPHA;
        }
    }

    public enum FixtureKind {
        ALPHA
    }
}
