package io.github.verils.gotemplate;

/**
 * Controls how template execution handles missing map keys and field-chain segments.
 *
 * @since 0.6.0
 */
public enum MissingKeyPolicy {
    /**
     * Preserves the v0.5.0 behavior: missing values evaluate to {@code null} and print as empty output.
     */
    DEFAULT,

    /**
     * Returns a Java zero-like value when the target type is knowable.
     * <p>
     * For missing map keys and absent field-chain segments the target type is usually not knowable in Java,
     * so this policy currently falls back to {@code null} and empty output for those cases.
     */
    ZERO,

    /**
     * Throws {@link TemplateExecutionException} when a map key or field-chain segment is missing.
     */
    ERROR
}
