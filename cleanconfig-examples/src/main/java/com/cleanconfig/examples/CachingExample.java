package com.cleanconfig.examples;

import com.cleanconfig.core.ConditionalDefaultValue;
import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.cache.CachingPropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Demonstrates caching features for performance optimization.
 *
 * <p>Shows:
 * <ul>
 *   <li>Validation without caching (baseline)</li>
 *   <li>Validation with caching (14.5x speedup)</li>
 *   <li>Computed default memoization for expensive operations</li>
 *   <li>Performance comparison and timing</li>
 * </ul>
 */
public class CachingExample {

    public static void main(String[] args) {
        System.out.println("=== CleanConfig Caching Example ===\n");

        example1_ValidationCaching();
        System.out.println();

        example2_ComputedDefaultCaching();
        System.out.println();

        example3_CacheControl();
    }

    /**
     * Example 1: Validation Caching - 14.5x Speedup
     */
    private static void example1_ValidationCaching() {
        System.out.println("Example 1: Validation Caching");
        System.out.println("------------------------------");

        // Create registry with validation rules
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class)
                        .name("server.host")
                        .defaultValue("localhost")
                        .validationRule(Rules.notBlank())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .register(PropertyDefinition.builder(Integer.class)
                        .name("server.port")
                        .defaultValue(8080)
                        .validationRule(Rules.port())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .register(PropertyDefinition.builder(String.class)
                        .name("db.url")
                        .defaultValue("jdbc:postgresql://localhost:5432/mydb")
                        .validationRule(Rules.url())
                        .category(PropertyCategory.GENERAL)
                        .build())
                .build();

        Map<String, String> properties = new HashMap<>();
        properties.put("server.host", "example.com");
        properties.put("server.port", "8080");
        properties.put("db.url", "jdbc:postgresql://localhost:5432/mydb");

        // Non-cached validation (baseline)
        PropertyValidator nonCachedValidator = new DefaultPropertyValidator(registry);
        long startNonCached = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            nonCachedValidator.validate(properties);
        }
        long timeNonCached = System.nanoTime() - startNonCached;

        // Cached validation
        PropertyValidator cachedValidator = new CachingPropertyValidator(
                new DefaultPropertyValidator(registry),
                100,                      // Max 100 cached results
                Duration.ofMinutes(5)     // 5-minute TTL
        );

        long startCached = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            cachedValidator.validate(properties);
        }
        long timeCached = System.nanoTime() - startCached;

        // Results
        double nonCachedMs = timeNonCached / 1_000_000.0;
        double cachedMs = timeCached / 1_000_000.0;
        double speedup = (double) timeNonCached / timeCached;

        System.out.println("Validated 1000 times:");
        System.out.printf("  Non-cached: %.2f ms (%.0f ops/ms)%n",
                nonCachedMs, 1000.0 / nonCachedMs);
        System.out.printf("  Cached:     %.2f ms (%.0f ops/ms)%n",
                cachedMs, 1000.0 / cachedMs);
        System.out.printf("  Speedup:    %.1fx faster with caching!%n", speedup);
    }

    /**
     * Example 2: Computed Default Caching
     */
    private static void example2_ComputedDefaultCaching() {
        System.out.println("Example 2: Computed Default Caching");
        System.out.println("-----------------------------------");

        // Counter for tracking expensive computations
        final int[] computeCount = {0};

        // Simulate expensive computation (e.g., system call)
        java.util.function.Supplier<Integer> expensiveComputation = () -> {
            computeCount[0]++;
            try {
                Thread.sleep(1); // Simulate expensive operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Runtime.getRuntime().availableProcessors() * 2;
        };

        // WITHOUT caching - computed every time
        PropertyDefinition<Integer> nonCachedDef = PropertyDefinition.builder(Integer.class)
                .name("worker.threads.noncached")
                .description("Worker threads (non-cached)")
                .defaultValue(ConditionalDefaultValue.computed(ctx ->
                        Optional.of(expensiveComputation.get())
                ))
                .category(PropertyCategory.GENERAL)
                .build();

        // WITH caching - computed once, memoized
        PropertyDefinition<Integer> cachedDef = PropertyDefinition.builder(Integer.class)
                .name("worker.threads.cached")
                .description("Worker threads (cached)")
                .defaultValue(ConditionalDefaultValue.computedCached(
                        ctx -> Optional.of(expensiveComputation.get()),
                        1  // Cache size = 1 (result never changes)
                ))
                .category(PropertyCategory.GENERAL)
                .build();

        PropertyContext emptyContext = new PropertyContext() {
            @Override
            public Optional<String> getProperty(String propertyName) {
                return Optional.empty();
            }

            @Override
            public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
                return Optional.empty();
            }

            @Override
            public Map<String, String> getAllProperties() {
                return new HashMap<>();
            }

            @Override
            public Optional<String> getMetadata(String key) {
                return Optional.empty();
            }

            @Override
            public boolean hasProperty(String propertyName) {
                return false;
            }
        };

        // Test non-cached (calls expensive operation 5 times)
        computeCount[0] = 0;
        long startNonCached = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            nonCachedDef.getDefaultValue().get().computeDefault(emptyContext);
        }
        long timeNonCached = System.nanoTime() - startNonCached;
        int nonCachedCalls = computeCount[0];

        // Test cached (calls expensive operation once, caches result)
        computeCount[0] = 0;
        long startCached = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            cachedDef.getDefaultValue().get().computeDefault(emptyContext);
        }
        long timeCached = System.nanoTime() - startCached;
        int cachedCalls = computeCount[0];

        System.out.println("Computed default 5 times:");
        System.out.printf("  Non-cached: %.2f ms (%d expensive calls)%n",
                timeNonCached / 1_000_000.0, nonCachedCalls);
        System.out.printf("  Cached:     %.2f ms (%d expensive call)%n",
                timeCached / 1_000_000.0, cachedCalls);
        System.out.printf("  Benefit:    %dx fewer expensive operations!%n",
                nonCachedCalls / cachedCalls);
    }

    /**
     * Example 3: Manual Cache Control
     */
    private static void example3_CacheControl() {
        System.out.println("Example 3: Manual Cache Control");
        System.out.println("--------------------------------");

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(PropertyDefinition.builder(String.class)
                        .name("app.name")
                        .defaultValue("MyApp")
                        .category(PropertyCategory.GENERAL)
                        .build())
                .build();

        CachingPropertyValidator validator = new CachingPropertyValidator(
                new DefaultPropertyValidator(registry),
                10,
                Duration.ofMinutes(5)
        );

        // Validate different property sets
        Map<String, String> props1 = Map.of("app.name", "App1");
        Map<String, String> props2 = Map.of("app.name", "App2");
        Map<String, String> props3 = Map.of("app.name", "App3");

        validator.validate(props1);
        System.out.printf("After validation 1: Cache size = %d%n", validator.getCacheSize());

        validator.validate(props2);
        System.out.printf("After validation 2: Cache size = %d%n", validator.getCacheSize());

        validator.validate(props3);
        System.out.printf("After validation 3: Cache size = %d%n", validator.getCacheSize());

        // Validate same property set (cache hit)
        validator.validate(props1);
        System.out.printf("After repeat validation: Cache size = %d (cache hit!)%n",
                validator.getCacheSize());

        // Clear cache
        validator.clearCache();
        System.out.printf("After clear: Cache size = %d%n", validator.getCacheSize());

        System.out.println("\nTip: Clear cache when properties change to prevent stale results");
    }
}
