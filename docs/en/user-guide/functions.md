# Functions Guide

gotemplate4j provides Go-compatible template functions and supports custom Java
functions. Function names are case-sensitive.

## Function Call Syntax

```gotemplate
{{functionName arg1 arg2}}
{{value | functionName arg}}
```

The pipeline value is passed as the final argument, except for the built-in
`default` function, whose pipeline value is the value being checked.

## Built-in Function Registry

The default registry contains exactly these functions:

| Category | Functions |
| --- | --- |
| Comparison | `eq`, `ne`, `lt`, `le`, `gt`, `ge` |
| Logical | `and`, `or`, `not` |
| Collections | `len`, `index`, `slice` |
| Formatting | `print`, `printf`, `println` |
| Escaping | `html`, `js`, `urlquery` |
| Invocation | `call` |
| gotemplate4j extensions | `deepEqual`, `typeof`, `kindOf`, `default` |

`Functions.BUILTIN` exposes this registry as an immutable map. Passing a custom
function with the same name to a `Template` constructor overrides the built-in
function for that template only.

## Comparisons

`eq` accepts at least two arguments and returns true when the first value equals
any later value. `ne`, `lt`, `le`, `gt`, and `ge` require exactly two arguments.

```gotemplate
{{eq .Status "active" "pending"}}
{{if lt .Age 18}}Minor{{else}}Adult{{end}}
```

Integers compare with integers, decimals with decimals, and strings with
strings. Boolean values support `eq` and `ne`. Mixing incompatible groups raises
a template execution error. Arrays, collections, and maps are not directly
comparable; use `deepEqual`.

## Logical Functions

`and` and `or` short-circuit and return an operand, as in Go. `not` returns a
boolean.

```gotemplate
{{and .Active .Verified}}
{{or .Admin .Moderator}}
{{not .Deleted}}
```

`if`, `with`, `and`, `or`, `not`, and `default` share the same truthiness rule:
`null`, `false`, numeric zero, empty strings, empty arrays, empty collections,
empty maps, and empty `Optional` values are false; other values are true.

## Collection Functions

### len

Returns the length of a string, array, collection, or map.

```gotemplate
{{len .Items}}
{{len "hello"}}
```

### index

Indexes arrays, lists, ordered collections, strings, and maps. Additional
arguments index nested containers.

```gotemplate
{{index .Items 0}}
{{index .Matrix 1 2}}
{{index .Users 0 "Name"}}
```

Sequence indexes must be integral and within bounds. Missing map keys follow the
configured `MissingKeyPolicy`.

### slice

Accepts zero to three indexes for arrays, lists, and strings.

```gotemplate
{{slice .Items}}
{{slice .Items 1}}
{{slice "hello" 1 4}}
{{slice .Items 1 4 5}}
```

Negative, reversed, and out-of-range indexes raise an execution error. Strings
do not support the three-index form.

## Formatting

`print` uses Go `Sprint` spacing. `println` separates operands with spaces and
appends a newline. `printf` supports `%v`, `%T`, `%t`, `%q`, `%d`, `%s`, `%x`,
`%X`, `%f`, and `%%`, plus common flags, decimal width, and precision.

```gotemplate
{{print "hello" "world"}}
{{println 1 2 3}}
{{printf "%.2f" .Price}}
{{printf "%05d" 42}}
```

`%T` reports the Java fully qualified class name, which is an intentional Java
difference. Dynamic width/precision and argument indexing are unsupported and
produce explicit execution errors.

## Escaping

`html`, `js`, and `urlquery` accept zero or more arguments and join them using
Go `Sprint` spacing rules.

```gotemplate
{{html "<b>"}}
{{js "</script>"}}
{{urlquery "a b"}}
```

`js` is a string escaper, not context-aware JavaScript auto-escaping. Keep the
result inside an appropriately quoted JavaScript string.

## Introspection and Extensions

### deepEqual

Recursively compares arrays, maps, collections, and nested containers. The same
concrete runtime type is required at each level. Lists and arrays preserve
order, Sets use element matching, and Maps compare keys and values. Cyclic
containers are supported. Ordinary non-container objects use `equals`.

```gotemplate
{{if deepEqual .Expected .Actual}}equal{{else}}different{{end}}
```

### typeof and kindOf

`typeof` returns a Java class name or `nil`. `kindOf` returns a broad template
kind such as `array`, `slice`, `map`, `struct`, `bool`, `int`, `string`, or
`invalid`.

```gotemplate
{{typeof .Value}}
{{kindOf .Value}}
```

### default

Returns a fallback when the checked value is false according to template
truthiness.

```gotemplate
{{default .Name "Anonymous"}}
{{.Name | default "Anonymous"}}
```

### call

Invokes a `Function` value supplied in the template data.

```gotemplate
{{call .Formatter .Value}}
```

## Custom Functions

Register functions with the `Template` constructor. Functions receive an
`Object[]` containing the evaluated arguments and may return any object.

```java
import io.github.verils.gotemplate.Function;
import io.github.verils.gotemplate.Template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

Map<String, Function> functions = new HashMap<>();
functions.put("double", args -> ((Number) args[0]).longValue() * 2);
functions.put("greet", args -> "Hello, " + args[0] + "!");

Template template = new Template("demo", functions);
template.parse("{{greet (.Name | default \"world\")}}; double={{double 5}}");

StringWriter writer = new StringWriter();
template.execute(writer, new HashMap<String, Object>());
```

Custom functions may override built-ins for that template:

```java
Map<String, Function> functions = new HashMap<>();
functions.put("len", args -> 42);
Template template = new Template("custom-len", functions);
```

Functions should validate argument counts and types, keep error messages
actionable, and avoid returning sensitive values in exception messages.

## Troubleshooting

- `undefined function` means the name is misspelled or was not supplied to the
  template constructor.
- Argument count and incompatible type failures are wrapped in
  `TemplateExecutionException` and include a compact argument type summary.
- Missing named templates use `TemplateNotFoundException`.
- Complex number literals are not supported and fail during parsing.
