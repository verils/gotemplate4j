package io.github.verils.gotemplate.internal;

import io.github.verils.gotemplate.TemplateField;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unified cache entry for class metadata.
 * <p>
 * This class consolidates all reflection-based metadata for a single class into one
 * immutable object, eliminating redundant scans and enabling fast-fail optimizations.
 * <p>
 * Performance benefits:
 * <ul>
 *   <li>Single comprehensive scan vs 3-4 separate scans</li>
 *   <li>Better CPU cache locality: related data in single object</li>
 *   <li>Invalid lookups eliminated: classes without annotations pay zero annotation scan cost</li>
 * </ul>
 */
public class ClassMetadata {
    // Indexed data
    private final BeanInfo beanInfo;
    private final Map<String, AccessibleObject> annotatedMembers;
    private final Map<String, PropertyDescriptor> propertyIndex;  // original + Go-style names
    private final Set<String> publicMethodNames;
    private final Set<String> publicFieldNames;

    /**
     * Constructor: One-time comprehensive scan of the class.
     * <p>
     * Performs a single pass through the class hierarchy to collect all reflection-based
     * metadata including BeanInfo, annotations, property descriptors, public methods,
     * and public fields. This consolidated approach eliminates redundant scans and
     * enables fast-fail optimizations in the Executor.
     *
     * @param clazz The class to introspect
     * @throws IllegalArgumentException if BeanInfo cannot be obtained for the class
     */
    ClassMetadata(Class<?> clazz) {
        // Scan BeanInfo
        try {
            this.beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to get BeanInfo for class '%s'", clazz.getName()), e);
        }

        // Scan annotations
        this.annotatedMembers = scanAnnotations(clazz);

        // Build property index
        this.propertyIndex = buildPropertyIndex(beanInfo);

        // Scan public methods
        this.publicMethodNames = scanPublicMethods(clazz);

        // Scan public fields
        this.publicFieldNames = scanPublicFields(clazz);
    }

    /**
     * Scan all classes in the hierarchy for @TemplateField annotations.
     * <p>
     * Traverses the entire class hierarchy (excluding Object.class) to find all members
     * annotated with {@link io.github.verils.gotemplate.TemplateField}. Field annotations
     * take precedence over method annotations when both exist with the same template name.
     * Private fields are made accessible via {@code setAccessible(true)}.
     *
     * @param clazz the root class to start scanning from
     * @return map of template names to their corresponding AccessibleObject (Field or Method)
     */
    private Map<String, AccessibleObject> scanAnnotations(Class<?> clazz) {
        Map<String, AccessibleObject> cache = new HashMap<>();

        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            // Scan fields first (fields take precedence)
            for (Field field : currentClass.getDeclaredFields()) {
                TemplateField annotation = field.getAnnotation(TemplateField.class);
                if (annotation != null) {
                    field.setAccessible(true);  // Allow access to private fields
                    cache.putIfAbsent(annotation.value(), field);
                }
            }

            // Scan methods
            for (Method method : currentClass.getDeclaredMethods()) {
                TemplateField annotation = method.getAnnotation(TemplateField.class);
                if (annotation != null && Modifier.isPublic(method.getModifiers())
                        && method.getParameterTypes().length == 0) {
                    cache.putIfAbsent(annotation.value(), method);
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return cache;
    }

    /**
     * Build name-indexed PropertyDescriptor cache.
     * <p>
     * Creates a lookup map that indexes PropertyDescriptors by both their original
     * property names (e.g., "userName") and Go-style capitalized names (e.g., "UserName").
     * This dual indexing supports both Java naming conventions and Go template field access patterns.
     * The "class" property descriptor is excluded as it's not useful for templates.
     *
     * @param beanInfo the BeanInfo containing property descriptors
     * @return map of property names (both original and Go-style) to PropertyDescriptors
     */
    private Map<String, PropertyDescriptor> buildPropertyIndex(BeanInfo beanInfo) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if ("class".equals(pd.getName())) continue;

            // Index by original name
            map.put(pd.getName(), pd);

            // Also index by Go-style name (first letter capitalized)
            String goStyle = Character.toUpperCase(pd.getName().charAt(0)) + pd.getName().substring(1);
            map.putIfAbsent(goStyle, pd);
        }
        return map;
    }

    /**
     * Scan all public no-arg methods (excluding getClass).
     * <p>
     * Collects the names of all public methods that take zero arguments from the given
     * class and its superclasses. The {@code getClass} method is explicitly excluded as
     * it's not useful for template field access. These methods can be invoked directly
     * in Go-style field chains (e.g., {{.getName}}).
     *
     * @param clazz the class to scan for public methods
     * @return set of public no-arg method names
     */
    private Set<String> scanPublicMethods(Class<?> clazz) {
        Set<String> names = new HashSet<>();
        for (Method method : clazz.getMethods()) {
            if (method.getParameterTypes().length == 0
                    && Modifier.isPublic(method.getModifiers())
                    && !"getClass".equals(method.getName())) {
                names.add(method.getName());
            }
        }
        return names;
    }

    /**
     * Scan all public fields.
     * <p>
     * Collects the names of all public fields from the given class and its superclasses.
     * These fields can be accessed directly in templates using Go-style notation
     * (e.g., {{.fieldName}}). Only fields with the {@code public} modifier are included.
     *
     * @param clazz the class to scan for public fields
     * @return set of public field names
     */
    private Set<String> scanPublicFields(Class<?> clazz) {
        Set<String> names = new HashSet<>();
        for (Field field : clazz.getFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                names.add(field.getName());
            }
        }
        return names;
    }

    /**
     * Gets the BeanInfo for this class.
     * <p>
     * Returns the cached BeanInfo object that was collected during construction.
     * This provides access to property descriptors, method descriptors, and other
     * JavaBeans metadata without requiring repeated introspection.
     *
     * @return the cached BeanInfo object
     */
    public BeanInfo getBeanInfo() {
        return beanInfo;
    }

    /**
     * Gets the map of annotated members (template name -&gt; AccessibleObject).
     * <p>
     * Returns a map where keys are the template field names specified in
     * {@link io.github.verils.gotemplate.TemplateField} annotations, and values are
     * the corresponding Field or Method objects. This allows direct lookup of
     * annotated members without scanning the class hierarchy at runtime.
     *
     * @return map of template names to their annotated members
     */
    public Map<String, AccessibleObject> getAnnotatedMembers() {
        return annotatedMembers;
    }

    /**
     * Gets a specific annotated member by its template name.
     * <p>
     * Convenience method for looking up a single annotated member without iterating
     * through the entire map. Returns {@code null} if no member is annotated with
     * the specified template name.
     *
     * @param identifier the template name to look up
     * @return the annotated Field or Method, or {@code null} if not found
     */
    public AccessibleObject getAnnotatedMember(String identifier) {
        return annotatedMembers.get(identifier);
    }

    /**
     * Gets the property descriptor index (property name -&gt; PropertyDescriptor).
     * <p>
     * Returns a map that supports lookup by both original property names
     * (e.g., "userName") and Go-style capitalized names (e.g., "UserName").
     * This enables flexible field access patterns in templates while maintaining
     * O(1) lookup performance.
     *
     * @return map of property names to their PropertyDescriptors
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptors() {
        return propertyIndex;
    }

    /**
     * Gets a specific property descriptor by name.
     * <p>
     * Convenience method for looking up a single property descriptor. Supports both
     * original property names and Go-style capitalized names. Returns {@code null}
     * if no property descriptor exists for the given name.
     *
     * @param identifier the property name to look up (original or Go-style)
     * @return the PropertyDescriptor, or {@code null} if not found
     */
    public PropertyDescriptor getPropertyDescriptor(String identifier) {
        return propertyIndex.get(identifier);
    }

    /**
     * Checks if the class has a public method with the specified name.
     * <p>
     * Performs a fast lookup in the pre-scanned set of public method names.
     * This is used by the Executor to quickly determine if a field access
     * can be resolved as a method call before attempting reflection.
     *
     * @param identifier the method name to check
     * @return {@code true} if a public no-arg method with this name exists
     */
    public boolean hasPublicMethod(String identifier) {
        return publicMethodNames.contains(identifier);
    }

    /**
     * Gets the set of public method names.
     * <p>
     * Returns all public no-arg method names (excluding {@code getClass}) that were
     * discovered during class introspection. These methods can be invoked directly
     * in template field chains using Go-style notation.
     *
     * @return set of public no-arg method names
     */
    public Set<String> getPublicMethodNames() {
        return publicMethodNames;
    }

    /**
     * Checks if the class has a public field with the specified name.
     * <p>
     * Performs a fast lookup in the pre-scanned set of public field names.
     * This is used by the Executor to quickly determine if a field access
     * can be resolved as a direct field reference before attempting reflection.
     *
     * @param fieldName the field name to check
     * @return {@code true} if a public field with this name exists
     */
    public boolean hasPublicField(String fieldName) {
        return this.publicFieldNames.contains(fieldName);
    }

    /**
     * Gets the set of public field names.
     * <p>
     * Returns all public field names that were discovered during class introspection.
     * These fields can be accessed directly in templates using Go-style notation
     * (e.g., {{.fieldName}}).
     *
     * @return set of public field names
     */
    public Set<String> getPublicFieldNames() {
        return publicFieldNames;
    }
}
