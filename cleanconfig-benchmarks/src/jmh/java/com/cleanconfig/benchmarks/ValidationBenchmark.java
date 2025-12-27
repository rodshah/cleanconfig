package com.cleanconfig.benchmarks;

import com.cleanconfig.core.*;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for property validation performance.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ValidationBenchmark {

    private PropertyRegistry smallRegistry;
    private PropertyRegistry mediumRegistry;
    private PropertyRegistry largeRegistry;

    private PropertyValidator smallValidator;
    private PropertyValidator mediumValidator;
    private PropertyValidator largeValidator;

    private Map<String, String> smallProperties;
    private Map<String, String> mediumProperties;
    private Map<String, String> largeProperties;

    @Setup
    public void setup() {
        // Small config (10 properties)
        smallRegistry = createRegistry(10);
        smallValidator = new DefaultPropertyValidator(smallRegistry);
        smallProperties = createProperties(10);

        // Medium config (50 properties)
        mediumRegistry = createRegistry(50);
        mediumValidator = new DefaultPropertyValidator(mediumRegistry);
        mediumProperties = createProperties(50);

        // Large config (200 properties)
        largeRegistry = createRegistry(200);
        largeValidator = new DefaultPropertyValidator(largeRegistry);
        largeProperties = createProperties(200);
    }

    @Benchmark
    public ValidationResult smallConfig() {
        return smallValidator.validate(smallProperties);
    }

    @Benchmark
    public ValidationResult mediumConfig() {
        return mediumValidator.validate(mediumProperties);
    }

    @Benchmark
    public ValidationResult largeConfig() {
        return largeValidator.validate(largeProperties);
    }

    @Benchmark
    public ValidationResult repeatedValidation() {
        // Simulate repeated validation of same properties (cache scenario)
        return mediumValidator.validate(mediumProperties);
    }

    private PropertyRegistry createRegistry(int count) {
        PropertyRegistryBuilder builder = PropertyRegistry.builder();

        for (int i = 0; i < count; i++) {
            PropertyDefinition<String> def = PropertyDefinition.builder(String.class)
                    .name("property." + i)
                    .description("Test property " + i)
                    .defaultValue("default" + i)
                    .validationRule(Rules.notBlank())
                    .category(PropertyCategory.GENERAL)
                    .build();
            builder.register(def);
        }

        return builder.build();
    }

    private Map<String, String> createProperties(int count) {
        Map<String, String> props = new HashMap<>();
        for (int i = 0; i < count; i++) {
            props.put("property." + i, "value" + i);
        }
        return props;
    }
}
