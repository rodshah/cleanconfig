package com.cleanconfig.core.validation.multiproperty;

import com.cleanconfig.core.validation.MultiPropertyValidationRule;

import java.util.Objects;

/**
 * Factory for creating resource constraint validation rules.
 *
 * <p>Provides convenient validation rules for common resource constraint patterns
 * found in cloud-native and container orchestration systems:
 * <ul>
 *   <li>{@link #cpuRequestLimit(String, String)} - CPU request ≤ CPU limit</li>
 *   <li>{@link #memoryRequestLimit(String, String)} - Memory request ≤ Memory limit</li>
 *   <li>{@link #validRange(String, String, Class)} - min &lt; max for any comparable type</li>
 * </ul>
 *
 * <p>These are convenience wrappers around {@link NumericRelationshipRules} for
 * commonly used patterns in Kubernetes, Docker, and cloud resource management.
 *
 * <p>Example usage:
 * <pre>
 * // Kubernetes-style resource constraints
 * MultiPropertyValidationRule cpuRule = ResourceConstraintRules.cpuRequestLimit(
 *     "resources.cpu.request", "resources.cpu.limit"
 * );
 *
 * MultiPropertyValidationRule memoryRule = ResourceConstraintRules.memoryRequestLimit(
 *     "resources.memory.request", "resources.memory.limit"
 * );
 *
 * // Generic range validation
 * MultiPropertyValidationRule portRange = ResourceConstraintRules.validRange(
 *     "port.min", "port.max", Integer.class
 * );
 * </pre>
 *
 * @since 0.2.0
 */
public final class ResourceConstraintRules {

    private ResourceConstraintRules() {
        // Utility class
    }

    /**
     * Validates CPU resource constraints: request ≤ limit.
     *
     * <p>Common in Kubernetes and cloud resource management where CPU request
     * (guaranteed resources) must not exceed CPU limit (maximum resources).
     *
     * <p>If either property is missing, validation passes.
     *
     * @param requestProperty the CPU request property name
     * @param limitProperty the CPU limit property name
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static MultiPropertyValidationRule cpuRequestLimit(String requestProperty, String limitProperty) {
        Objects.requireNonNull(requestProperty, "Request property name cannot be null");
        Objects.requireNonNull(limitProperty, "Limit property name cannot be null");
        return NumericRelationshipRules.lessThanOrEqual(requestProperty, limitProperty, Integer.class);
    }

    /**
     * Validates memory resource constraints: request ≤ limit.
     *
     * <p>Common in Kubernetes and cloud resource management where memory request
     * (guaranteed resources) must not exceed memory limit (maximum resources).
     *
     * <p>If either property is missing, validation passes.
     *
     * @param requestProperty the memory request property name
     * @param limitProperty the memory limit property name
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static MultiPropertyValidationRule memoryRequestLimit(String requestProperty, String limitProperty) {
        Objects.requireNonNull(requestProperty, "Request property name cannot be null");
        Objects.requireNonNull(limitProperty, "Limit property name cannot be null");
        return NumericRelationshipRules.lessThanOrEqual(requestProperty, limitProperty, Long.class);
    }

    /**
     * Validates a numeric range: min &lt; max.
     *
     * <p>Generic range validation for any comparable type. Useful for:
     * <ul>
     *   <li>Port ranges (minPort &lt; maxPort)</li>
     *   <li>Price ranges (minPrice &lt; maxPrice)</li>
     *   <li>Date ranges (startDate &lt; endDate)</li>
     *   <li>Any other comparable range</li>
     * </ul>
     *
     * <p>If either property is missing, validation passes.
     *
     * @param minProperty the minimum property name
     * @param maxProperty the maximum property name
     * @param type the comparable type
     * @param <T> the type parameter
     * @return validation rule
     * @throws NullPointerException if any parameter is null
     */
    public static <T extends Comparable<T>> MultiPropertyValidationRule validRange(
            String minProperty,
            String maxProperty,
            Class<T> type) {
        Objects.requireNonNull(minProperty, "Min property name cannot be null");
        Objects.requireNonNull(maxProperty, "Max property name cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return NumericRelationshipRules.lessThan(minProperty, maxProperty, type);
    }
}
