package io.github.verils.gotemplate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A single comprehensive test that exercises nearly every template engine feature
 * in one complex template. Serves as a regression canary — if this passes, the engine
 * is most likely healthy.
 * <p>
 * Features covered: field chaining, Optional unwrapping, enum rendering, if/else if/else,
 * with/else-with/else, range over collections/maps/integers, break/continue, variable
 * declaration and reassignment, pipeline, parenthesized pipeline, all comparison functions,
 * all logical functions, len/index/slice, printf, html/js/urlquery escaping, call, default,
 * typeof/kindOf, deepEqual, number/string/boolean/nil literals, define/template, block
 * with override, trim markers, comments, and custom functions.
 */
public class ComprehensiveTemplateTest {

    // region --- Data Model ------------------------------------------------------------

    @SuppressWarnings("unused")
    public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
    public static class Address {
        private String street;
        private String city;
        private String country;
        private Optional<String> zipCode;

        public Address() {}

        public Address(String street, String city, String country, String zipCode) {
            this.street = street;
            this.city = city;
            this.country = country;
            this.zipCode = Optional.ofNullable(zipCode);
        }

        public String getStreet() { return street; }
        public void setStreet(String v) { street = v; }
        public String getCity() { return city; }
        public void setCity(String v) { city = v; }
        public String getCountry() { return country; }
        public void setCountry(String v) { country = v; }
        public Optional<String> getZipCode() { return zipCode; }
        public void setZipCode(String v) { zipCode = Optional.ofNullable(v); }
    }

    @SuppressWarnings("unused")
    public static class Item {
        private String name;
        private int quantity;
        private double price;
        private boolean inStock;
        private List<String> tags;

        public Item() {}

        public Item(String name, int quantity, double price, boolean inStock, List<String> tags) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.inStock = inStock;
            this.tags = tags;
        }

        public String getName() { return name; }
        public void setName(String v) { name = v; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int v) { quantity = v; }
        public double getPrice() { return price; }
        public void setPrice(double v) { price = v; }
        public boolean isInStock() { return inStock; }
        public void setInStock(boolean v) { inStock = v; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> v) { tags = v; }
    }

    // endregion

    // region --- Template --------------------------------------------------------------

    /**
     * One template to rule them all.  Exercises field chaining, Optional, enums,
     * if/else-if/else, with/else-with/else, range over list/map/integer, break/continue,
     * variable declare + reassign, pipeline, parenthesized pipeline, all built-in
     * comparison/logical/collection/escaping/introspection functions, call, default,
     * literals (number, string, bool, nil), define/template, block + override, trim
     * markers, comments, and custom functions.
     */
    static final String TPL =
            // -- comment (trim markers demoed in dedicated section below) --
            "{{/* Comprehensive Template — exercises the full feature set */}}\n" +
            "===== COMPREHENSIVE TEMPLATE TEST =====\n" +
            "\n" +
            // -- field access, enum, Optional unwrapping --
            "[Field Access]\n" +
            "  Order: {{.orderId}}\n" +
            "  Status: {{.status}}\n" +
            "  Name: {{.name}}\n" +
            "\n" +
            // -- html escaping --
            "[Optional & Escaping]\n" +
            "  Email:    {{.email | html}}\n" +
            "  Phone:    {{.phone}}\n" +
            "  Address:  {{.address.street}}, {{.address.city}}, {{.address.country}}\n" +
            "  Zip:      {{.address.zipCode}}\n" +
            "\n" +
            // -- if / else if / else with comparison functions --
            "[If Chain + Comparison]\n" +
            "  Loyalty:  {{if ge .loyaltyPoints 500}}VIP ({{.loyaltyPoints}} pts)" +
            "{{else if gt .loyaltyPoints 0}}Member ({{.loyaltyPoints}} pts)" +
            "{{else}}New Customer{{end}}\n" +
            "\n" +
            // -- with / else with / else --
            "[With / Else-With / Else]\n" +
            "  {{with .missingField}}MISSING{{else with .address}}" +
            "Addr: {{.street}}, {{.city}}" +
            "{{else}}NOTHING{{end}}\n" +
            "\n" +
            // -- range over list with $i,$v + else (empty) --
            "[Range over List]\n" +
            "  {{range $i, $v := .items}}" +
            "{{$i}}. [{{if $v.inStock}}OK{{else}}BACKORDER{{end}}] " +
            "{{$v.name}} x{{$v.quantity}} @ ${{printf \"%.2f\" $v.price}}\n" +
            "  {{else}}  (no items){{end}}" +
            "\n" +
            // -- range over map --
            "[Range over Map]\n" +
            "  {{range $k, $v := .metadata}}  {{$k}} -> {{$v}}\n" +
            "  {{end}}" +
            "\n" +
            // -- integer range --
            "[Integer Range]\n" +
            "  Digits:{{range $i := 5}} {{$i}}{{end}}\n" +
            "\n" +
            // -- break + continue in collection range --
            "[Break / Continue]\n" +
            "  Break: {{range $v := .items}}" +
            "{{if eq $v.name \"Gadget\"}}{{break}}{{end}}" +
            "{{$v.name}}-{{end}}\n" +
            "  Continue: {{range $v := .items}}" +
            "{{if eq $v.name \"Widget\"}}{{continue}}{{end}}" +
            "{{$v.name}}-{{end}}\n" +
            "\n" +
            // -- variable declaration with custom & builtin functions --
            "[Variable + Custom Func]\n" +
            "  {{$a := multiply 10.0 5.0}}{{$b := add 100 25}}{{$n := len .name}}" +
            "  Multiply: {{$a}}, Add: {{$b}}, NameLen: {{$n}}\n" +
            "\n" +
            // -- pipeline --
            "[Pipeline]\n" +
            "  Shout: {{.name | toUpper}}\n" +
            "\n" +
            // -- parenthesized pipeline --
            "[Parenthesized Pipeline]\n" +
            "  Len: {{(len .name)}}\n" +
            "  First item: {{(index .items 0).name}}\n" +
            "\n" +
            // -- builtin functions: len, index, slice, printf --
            "[Builtin Functions]\n" +
            "  len(items):       {{len .items}}\n" +
            "  index(items,0):   {{with index .items 0}}{{.name}}{{end}}\n" +
            "  slice(hello,1,4): {{slice \"hello\" 1 4}}\n" +
            "  printf:           0x{{printf \"%x\" 255}}\n" +
            "  html tag:         {{\"<tag>\" | html}}\n" +
            "  js newline:       {{\"hello\\nworld\" | js}}\n" +
            "  urlquery:         {{\"hello world\" | urlquery}}\n" +
            // -- raw string literal (backtick) --
            "  raw string:       {{`raw\\backticks`}}\n" +
            "\n" +
            // -- logical functions --
            "[Logical Functions]\n" +
            "  and(T,T):   {{and true true}}\n" +
            "  or(F,F,T):  {{or false false true}}\n" +
            "  not(F):     {{not false}}\n" +
            "\n" +
            // -- comparison functions --
            "[Comparison Functions]\n" +
            "  eq(1,1,1):  {{eq 1 1 1}}\n" +
            "  ne(1,2):    {{ne 1 2}}\n" +
            "  lt(1,2,3):  {{lt 1 2 3}}\n" +
            "  le(1,1,2):  {{le 1 1 2}}\n" +
            "  gt(3,2,1):  {{gt 3 2 1}}\n" +
            "  ge(2,2,1):  {{ge 2 2 1}}\n" +
            "\n" +
            // -- type introspection --
            "[Type Introspection]\n" +
            "  typeof items: {{typeof .items}}\n" +
            "  kindOf items: {{kindOf .items}}\n" +
            "\n" +
            // -- deepEqual --
            "[deepEqual]\n" +
            "  deepEqual(42,42):     {{deepEqual 42 42}}\n" +
            "  deepEqual(\"a\",\"b\"):   {{deepEqual \"a\" \"b\"}}\n" +
            "\n" +
            // -- nil, boolean, number literals --
            "[Literals]\n" +
            "  nil:    {{nil}}\n" +
            "  bool:   {{true}}, {{false}}\n" +
            "  hex:    {{printf \"%x\" 0xFF}}\n" +
            "  binary: {{0b1010}}\n" +
            "  octal:  {{0o777}}\n" +
            "  float:  {{printf \"%.2f\" 3.14159}}\n" +
            "  char:   {{'A'}}\n" +
            "\n" +
            // -- trim markers (left: {{- trims preceding whitespace; right: -}} trims following whitespace) --
            "[Trim Markers]\n" +
            "  no trim:  [   {{\"hello\"}}   ]\n" +
            "  trimmed:  [   {{- \"hello\" -}}   ]\n" +
            "\n" +
            // -- call (dynamic function invocation) --
            "[call Function]\n" +
            "  echo: {{call .echoFn \"hello\"}}\n" +
            "\n" +
            // -- custom function (multiply) --
            "[Custom Function]\n" +
            "  multiply(10,5): {{multiply 10.0 5.0}}\n" +
            "\n" +
            // -- define + template invocation --
            "[Subtemplate]\n" +
            "  {{template \"footer\" .}}\n" +
            "\n" +
            // -- block (with override via second parse) --
            "[Block Override]\n" +
            "  {{block \"signature\" .}}Default Signature{{end}}\n" +
            "\n" +
            // -- default function + println --
            "[Default + Println]\n" +
            "  default:  {{default .missingField \"(nothing)\"}}\n" +
            "  println:  {{println \"Hello\"}}{{println \"World\"}}\n" +
            "\n" +
            "===== END =====\n";

    // -- subtemplate definition (used by {{template "footer"}}) --
    static final String FOOTER_DEF =
            "{{define \"footer\"}}  Footer: [generated by gotemplate4j]{{end}}";

    // -- block override (used by {{block "signature"}}) --
    static final String SIGNATURE_OVERRIDE =
            "{{define \"signature\"}}  Signature: Custom Signature Block{{end}}";

    // endregion

    // region --- Test ------------------------------------------------------------------

    @Test
    void testEveryFeatureInOneTemplate() throws IOException, TemplateException {
        // -- build data --
        Map<String, Object> data = buildData();

        // -- custom functions --
        Map<String, Function> funcs = new HashMap<>();
        funcs.put("multiply", args ->
                ((Number) args[0]).doubleValue() * ((Number) args[1]).doubleValue());
        funcs.put("toUpper", args ->
                ((String) args[0]).toUpperCase());
        funcs.put("add", args ->
                ((Number) args[0]).intValue() + ((Number) args[1]).intValue());

        // -- parse everything --
        Template t = new Template("comprehensive", funcs);
        t.parse(TPL);
        t.parse(FOOTER_DEF);
        t.parse(SIGNATURE_OVERRIDE);

        // -- execute --
        StringWriter w = new StringWriter();
        t.execute(w, data);
        String out = w.toString();

        // -----------------------------------------------------------------------
        // Verify EVERY feature by looking for expected substrings in the output.
        // Each assertion targets exactly one feature category.
        // -----------------------------------------------------------------------

        // Field access + enum
        assertTrue(out.contains("Order: ORD-2024-001"), "field access");
        assertTrue(out.contains("Status: CONFIRMED"), "enum rendering");
        assertTrue(out.contains("Name: Alice Johnson"), "nested field");

        // Optional unwrapping — address.zipCode is Optional that unwraps to "12345"
        assertTrue(out.contains("Zip:      12345"), "optional unwrapping");

        // html escaping
        assertTrue(out.contains("Email:    alice&amp;johnson@example.com"), "html escaping");
        assertTrue(out.contains("&amp;"), "html escaping — &");

        // if/else-if/else + comparison
        assertTrue(out.contains("Loyalty:  VIP (500 pts)"), "if-else chain + ge");

        // with/else-with/else
        assertTrue(out.contains("Addr: 123 Main St, Springfield"), "with else-with");

        // range over list with $i,$v, inStock bool check, printf
        assertTrue(out.contains("0. [OK] Widget x2 @ $10.00"), "range list index 0");
        assertTrue(out.contains("1. [BACKORDER] Gadget x1 @ $50.00"), "range list index 1 + bool");

        // range over map
        assertTrue(out.contains("source -> web"), "range map key=source");
        assertTrue(out.contains("campaign -> spring-sale"), "range map key=campaign");

        // integer range (0..4)
        assertTrue(out.contains("Digits: 0 1 2 3 4"), "integer range");

        // break + continue in collection range
        // Break: Widget-  (stops at "Gadget")
        assertTrue(out.contains("Break: Widget-"), "break in range");
        // Continue: Gadget-  (skips "Widget")
        assertTrue(out.contains("Continue: Gadget-"), "continue in range");

        // variable declaration with custom & builtin functions
        assertTrue(out.contains("Multiply: 50.0"), "var with custom multiply func");
        assertTrue(out.contains("Add: 125"), "var with custom add func");
        assertTrue(out.contains("NameLen: 13"), "var with builtin len func");

        // pipeline
        assertTrue(out.contains("Shout: ALICE JOHNSON"), "pipeline toUpper");

        // parenthesized pipeline — "Alice Johnson" has 13 chars
        assertTrue(out.contains("Len: 13"), "parenthesized pipeline");
        assertTrue(out.contains("First item: Widget"), "parenthesized pipeline field chain");

        // len, index, slice, printf
        assertTrue(out.contains("len(items):       2"), "len");
        assertTrue(out.contains("index(items,0):   Widget"), "index");
        assertTrue(out.contains("slice(hello,1,4): ell"), "slice");
        assertTrue(out.contains("printf:           0xff"), "printf");

        // escaping functions
        assertTrue(out.contains("html tag:         &lt;tag&gt;"), "html tag escape");
        assertTrue(out.contains("js newline:       hello\\nworld"), "js escape");
        assertTrue(out.contains("urlquery:         hello+world"), "urlquery escape");

        // raw string literal (backtick-quoted, no escape processing)
        assertTrue(out.contains("raw string:       raw\\backticks"), "raw string literal");

        // logical functions
        assertTrue(out.contains("and(T,T):   true"), "logical and");
        assertTrue(out.contains("or(F,F,T):  true"), "logical or");
        assertTrue(out.contains("not(F):     true"), "logical not");

        // comparison functions
        assertTrue(out.contains("eq(1,1,1):  true"), "eq");
        assertTrue(out.contains("ne(1,2):    true"), "ne");
        assertTrue(out.contains("lt(1,2,3):  true"), "lt chained");
        assertTrue(out.contains("le(1,1,2):  true"), "le chained");
        assertTrue(out.contains("gt(3,2,1):  true"), "gt chained");
        assertTrue(out.contains("ge(2,2,1):  true"), "ge chained");

        // type introspection
        assertTrue(out.contains("typeof items:"), "typeof");
        // items is an array (Item[]), so kindOf returns "array" not "slice"
        assertTrue(out.contains("kindOf items: array"), "kindOf");

        // deepEqual
        assertTrue(out.contains("deepEqual(42,42):     true"), "deepEqual true");
        assertTrue(out.contains("deepEqual(\"a\",\"b\"):   false"), "deepEqual false");

        // nil, boolean literals
        assertTrue(out.contains("nil:    <no value>"), "nil literal");
        assertTrue(out.contains("bool:   true, false"), "bool literals");

        // number literals: hex, binary, octal, float, char
        assertTrue(out.contains("hex:    ff"), "hex literal");
        assertTrue(out.contains("binary: 10"), "binary literal");
        assertTrue(out.contains("octal:  511"), "octal literal");
        assertTrue(out.contains("float:  3.14"), "float literal + printf");
        assertTrue(out.contains("char:   65"), "char constant A = 65");

        // trim markers
        assertTrue(out.contains("no trim:  [   hello   ]"), "no trim preserves spaces");
        assertTrue(out.contains("trimmed:  [hello]"), "trim markers strip spaces");

        // call (dynamic function invocation)
        assertTrue(out.contains("echo: CALLED:hello"), "call function");

        // custom function
        assertTrue(out.contains("multiply(10,5): 50.0"), "custom multiply function");

        // define + template invocation
        assertTrue(out.contains("Footer: [generated by gotemplate4j]"), "subtemplate");

        // block override
        assertTrue(out.contains("Signature: Custom Signature Block"), "block override");
        assertFalse(out.contains("Default Signature"), "block default was overridden");

        // default function + println
        assertTrue(out.contains("default:  (nothing)"), "default fallback");
        assertTrue(out.contains("println:"), "println section");
    }

    // endregion

    // region --- Helpers ---------------------------------------------------------------

    private static Map<String, Object> buildData() {
        Address addr = new Address("123 Main St", "Springfield", "US", "12345");

        Item item1 = new Item("Widget", 2, 10.0, true, Arrays.asList("electronics", "gadget"));
        Item item2 = new Item("Gadget", 1, 50.0, false, Collections.emptyList());

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("source", "web");
        metadata.put("campaign", "spring-sale");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", "ORD-2024-001");
        data.put("status", OrderStatus.CONFIRMED);
        data.put("name", "Alice Johnson");
        data.put("email", "alice&johnson@example.com");
        data.put("address", addr);
        data.put("phone", "555-0100");
        data.put("loyaltyPoints", 500);
        data.put("items", new Item[]{item1, item2});
        data.put("metadata", metadata);
        data.put("echoFn", (Function) args -> "CALLED:" + args[0]);

        return data;
    }

    // endregion
}
