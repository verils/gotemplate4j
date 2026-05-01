package io.github.verils.gotemplate;

/**
 * Represents a custom function that can be called from within templates.
 * <p>
 * Functions are invoked using the syntax: {{funcName arg1 arg2 ...}} or in pipelines: {{arg | funcName}}
 * <p>
 * Example implementation:
 * <pre>{@code
 * Function upperCase = new Function() {
 *     public Object invoke(Object... args) {
 *         if (args.length != 1 || !(args[0] instanceof String)) {
 *             throw new IllegalArgumentException("upper requires one string argument");
 *         }
 *         return ((String) args[0]).toUpperCase();
 *     }
 * };
 * 
 * Map&lt;String, Function&gt; functions = new HashMap&lt;&gt;();
 * functions.put("upper", upperCase);
 * Template template = new Template("test", functions);
 * template.parse("{{.Name | upper}}"); // Outputs: JOHN
 * }</pre>
 * <p>
 * Built-in functions available by default:
 * <ul>
 *   <li>print, printf, println - Output formatting</li>
 *   <li>eq, ne, lt, le, gt, ge - Comparison operators</li>
 *   <li>and, or, not - Logical operators</li>
 *   <li>len - Length of collections/strings</li>
 *   <li>index - Access array/map elements</li>
 *   <li>slice - Slice arrays/strings</li>
 *   <li>call - Call functions dynamically</li>
 *   <li>html, js, urlquery - Escaping functions</li>
 *   <li>deepEqual, typeof, kindOf - Type inspection</li>
 * </ul>
 *
 * @see Template#Template(String, java.util.Map) for registering custom functions
 */

public interface Function {

    /**
     * Invokes the function with the provided arguments.
     * <p>
     * The function should validate the number and types of arguments and throw
     * an appropriate exception if they are invalid.
     *
     * @param args The arguments passed to the function from the template.
     *             Can be zero or more arguments of any type.
     * @return The result value, which will be converted to a string for output
     *         or used as input to the next stage in a pipeline.
     * @throws IllegalArgumentException if arguments are invalid
     * @throws RuntimeException         if function execution fails
     */
    Object invoke(Object... args);

}
