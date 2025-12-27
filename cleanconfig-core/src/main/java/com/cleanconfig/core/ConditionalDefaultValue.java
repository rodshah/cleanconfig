package com.cleanconfig.core;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for computing default values conditionally based on context.
 *
 * <p>Default values can be:
 * <ul>
 *   <li>Static: Always the same value</li>
 *   <li>Computed: Calculated from other property values</li>
 *   <li>Conditional: Different defaults for different contexts</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Static default
 * ConditionalDefaultValue&lt;Integer&gt; staticPort =
 *     ConditionalDefaultValue.staticValue(8080);
 *
 * // Computed default
 * ConditionalDefaultValue&lt;Integer&gt; computedThreads =
 *     ConditionalDefaultValue.computed(ctx -&gt;
 *         Optional.of(Runtime.getRuntime().availableProcessors() * 2)
 *     );
 *
 * // Conditional default
 * ConditionalDefaultValue&lt;String&gt; logLevel =
 *     ConditionalDefaultValue.staticValue("INFO")
 *         .when(ctx -&gt; ctx.getProperty("environment").orElse("").equals("test"), "DEBUG");
 * </pre>
 *
 * @param <T> the type of the default value
 * @since 0.1.0
 */
@FunctionalInterface
public interface ConditionalDefaultValue<T> {

    /**
     * Computes the default value based on context.
     *
     * @param context the property context
     * @return optional containing the default value, or empty if no default applies
     */
    Optional<T> computeDefault(PropertyContext context);

    /**
     * Creates a static default value.
     *
     * @param value the static value
     * @param <T> the value type
     * @return a conditional default that always returns the given value
     */
    static <T> ConditionalDefaultValue<T> staticValue(T value) {
        Objects.requireNonNull(value, "static value cannot be null");
        return context -> Optional.of(value);
    }

    /**
     * Creates a computed default value.
     *
     * @param computer function to compute the default from context
     * @param <T> the value type
     * @return a conditional default that computes the value
     */
    static <T> ConditionalDefaultValue<T> computed(Function<PropertyContext, Optional<T>> computer) {
        Objects.requireNonNull(computer, "computer function cannot be null");
        return computer::apply;
    }

    /**
     * Creates a cached computed default value.
     *
     * <p>The computation result is memoized based on the property context fingerprint.
     * Use this for expensive computations (I/O, system calls, calculations) that
     * produce the same result for the same context.
     *
     * <p>Example usage:
     * <pre>
     * // Expensive: system call every time
     * .defaultValue(ConditionalDefaultValue.computed(ctx -&gt;
     *     Optional.of(Runtime.getRuntime().availableProcessors() * 2)
     * ))
     *
     * // Optimized: cached result
     * .defaultValue(ConditionalDefaultValue.computedCached(ctx -&gt;
     *     Optional.of(Runtime.getRuntime().availableProcessors() * 2),
     *     1  // cache size = 1 (result never changes)
     * ))
     * </pre>
     *
     * @param computer function to compute the default from context
     * @param cacheSize maximum number of cached results
     * @param <T> the value type
     * @return a memoized conditional default
     * @since 0.1.0
     */
    static <T> ConditionalDefaultValue<T> computedCached(
            Function<PropertyContext, Optional<T>> computer,
            int cacheSize) {
        Objects.requireNonNull(computer, "computer function cannot be null");
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }

        java.util.concurrent.ConcurrentHashMap<Integer, T> cache =
                new java.util.concurrent.ConcurrentHashMap<>(cacheSize);

        return context -> {
            // Create cache key from context properties hash
            int cacheKey = context.getAllProperties().hashCode();

            // Try to get from cache
            T cachedValue = cache.get(cacheKey);
            if (cachedValue != null) {
                return Optional.of(cachedValue);
            }

            // Cache miss - compute the value
            Optional<T> computed = computer.apply(context);

            // Store in cache if present and space available
            computed.ifPresent(value -> {
                if (cache.size() < cacheSize) {
                    cache.put(cacheKey, value);
                }
            });

            return computed;
        };
    }

    /**
     * Adds a conditional override to this default value.
     *
     * <p>If the condition is true, the override value is used instead of this default.
     *
     * @param condition the condition to check
     * @param overrideValue the value to use when condition is true
     * @return a new conditional default with the override
     */
    default ConditionalDefaultValue<T> when(Predicate<PropertyContext> condition, T overrideValue) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(overrideValue, "overrideValue cannot be null");

        return context -> {
            if (condition.test(context)) {
                return Optional.of(overrideValue);
            }
            return this.computeDefault(context);
        };
    }

    /**
     * Adds a conditional computed override to this default value.
     *
     * <p>If the condition is true, the override computer is used instead of this default.
     *
     * @param condition the condition to check
     * @param overrideComputer function to compute the override value
     * @return a new conditional default with the override
     */
    default ConditionalDefaultValue<T> when(Predicate<PropertyContext> condition,
                                             Function<PropertyContext, Optional<T>> overrideComputer) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(overrideComputer, "overrideComputer cannot be null");

        return context -> {
            if (condition.test(context)) {
                return overrideComputer.apply(context);
            }
            return this.computeDefault(context);
        };
    }

    /**
     * Creates a default that returns empty (no default value).
     *
     * @param <T> the value type
     * @return a conditional default that never provides a value
     */
    static <T> ConditionalDefaultValue<T> noDefault() {
        return context -> Optional.empty();
    }
}
