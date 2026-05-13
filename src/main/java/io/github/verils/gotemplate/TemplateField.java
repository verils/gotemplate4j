package io.github.verils.gotemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to explicitly control field/method name mapping in templates.
 * <p>
 * This annotation allows you to specify the exact name that should be used
 * when accessing fields or methods from Go-style templates, avoiding reliance
 * on heuristic name conversion.
 * </p>
 * 
 * <h3>Usage Examples</h3>
 * 
 * <h4>On Fields:</h4>
 * <pre>{@code
 * public class User {
 *     @TemplateField("UserName")
 *     private String userName;
 *     
 *     @TemplateField("user_email")
 *     private String email;
 * }
 * }</pre>
 * 
 * <h4>On Getter Methods:</h4>
 * <pre>{@code
 * public class User {
 *     @TemplateField("FullName")
 *     public String getFullName() {
 *         return firstName + " " + lastName;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Lookup Priority</h3>
 * When resolving a template field reference (e.g., {{.UserName}}), the engine
 * checks in this order:
 * <ol>
 *   <li>Fields/methods with {@code @TemplateField} matching the template name</li>
 *   <li>Exact match on Java property/field name</li>
 *   <li>Go-style capitalization (first letter uppercase)</li>
 * </ol>
 * 
 * <h3>Precedence</h3>
 * If both a field and its getter method have {@code @TemplateField} annotations,
 * the field's annotation takes precedence.
 * 
 * @since 0.8.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateField {
    
    /**
     * The name to use in templates.
     * <p>
     * This is the exact identifier that should be used in template expressions.
     * For example, if {@code value = "UserName"}, then the template should use
     * {@code {{.UserName}}} to access this field or method.
     * </p>
     * 
     * @return the template field name
     */
    String value();
}
