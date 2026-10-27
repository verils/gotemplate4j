package io.github.verils.gotemplate;

/**
 * Control the behavior during execution if a map is indexed with a key that is not present in the map.
 *
 * @since 0.6.0
 */
public enum MissingKeyPolicy {
    /**
     * The default behavior: Do nothing and continue execution.
     */
    INVALID,

    /**
     * Requests the element type's zero value for a missing map key.
     * <p>
     * Java maps do not retain the compile-time element type. gotemplate4j therefore cannot
     * infer a meaningful zero value and resolves a missing map key to {@code null}; direct
     * rendering consequently produces {@code <no value>}. This differs from Go only where the
     * map element type would provide a concrete zero value.
     */
    ZERO,

    /**
     * Execution stops immediately with an {@link TemplateExecutionException}.
     */
    ERROR
}
