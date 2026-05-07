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
     * The operation returns the zero value for the map type's element.
     */
    ZERO,

    /**
     * Execution stops immediately with an {@link TemplateExecutionException}.
     */
    ERROR
}
