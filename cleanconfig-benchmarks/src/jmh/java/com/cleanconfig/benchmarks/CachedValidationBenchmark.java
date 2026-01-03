package com.cleanconfig.benchmarks;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyRegistryBuilder;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.cache.CachingPropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparing cached vs non-cached validation.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class CachedValidationBenchmark {

    private PropertyRegistry registry;
    private Map<String, String> properties;

    private PropertyValidator nonCachedValidator;
    private PropertyValidator cachedValidator;

    @Setup
    public void setup() {
        // Create registry with 50 properties
        PropertyRegistryBuilder builder = PropertyRegistry.builder();
        for (int i = 0; i < 50; i++) {
            PropertyDefinition<String> def = PropertyDefinition.builder(String.class)
                    .name("property." + i)
                    .description("Test property " + i)
                    .defaultValue("default" + i)
                    .validationRule(Rules.notBlank().and(Rules.minLength(3)))
                    .category(PropertyCategory.GENERAL)
                    .build();
            builder.register(def);
        }
        registry = builder.build();

        // Create properties
        properties = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            properties.put("property." + i, "value" + i);
        }

        // Create validators
        nonCachedValidator = new DefaultPropertyValidator(registry);
        cachedValidator = new CachingPropertyValidator(
                new DefaultPropertyValidator(registry),
                1000,
                Duration.ofMinutes(5)
        );
    }

    @Benchmark
    public ValidationResult nonCached() {
        return nonCachedValidator.validate(properties);
    }

    @Benchmark
    public ValidationResult cached_firstAccess() {
        // Clear cache to simulate first access
        ((CachingPropertyValidator) cachedValidator).clearCache();
        return cachedValidator.validate(properties);
    }

    @Benchmark
    public ValidationResult cached_repeatedAccess() {
        // Repeated validation with cache hit
        return cachedValidator.validate(properties);
    }
}
