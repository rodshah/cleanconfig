package com.cleanconfig.benchmarks;

import com.cleanconfig.core.*;
import com.cleanconfig.core.impl.DefaultValueApplierImpl;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for default value application performance.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class DefaultApplicationBenchmark {

    private PropertyRegistry registryWithStaticDefaults;
    private PropertyRegistry registryWithComputedDefaults;
    private DefaultValueApplier applier;
    private DefaultValueApplier computedApplier;

    private Map<String, String> partialProperties;

    @Setup
    public void setup() {
        // Registry with static defaults
        PropertyRegistryBuilder staticBuilder = PropertyRegistry.builder();
        for (int i = 0; i < 50; i++) {
            PropertyDefinition<String> def = PropertyDefinition.builder(String.class)
                    .name("property." + i)
                    .defaultValue("default" + i)
                    .build();
            staticBuilder.register(def);
        }
        registryWithStaticDefaults = staticBuilder.build();
        applier = new DefaultValueApplierImpl(registryWithStaticDefaults);

        // Registry with computed defaults
        PropertyRegistryBuilder computedBuilder = PropertyRegistry.builder();
        for (int i = 0; i < 50; i++) {
            final int index = i;
            PropertyDefinition<String> def = PropertyDefinition.builder(String.class)
                    .name("property." + i)
                    .defaultValue(ConditionalDefaultValue.computed(ctx ->
                            java.util.Optional.of("computed" + index)
                    ))
                    .build();
            computedBuilder.register(def);
        }
        registryWithComputedDefaults = computedBuilder.build();
        computedApplier = new DefaultValueApplierImpl(registryWithComputedDefaults);

        // Partial properties (only half provided, rest use defaults)
        partialProperties = new HashMap<>();
        for (int i = 0; i < 25; i++) {
            partialProperties.put("property." + i, "userValue" + i);
        }
    }

    @Benchmark
    public DefaultApplicationResult staticDefaults() {
        return applier.applyDefaults(partialProperties);
    }

    @Benchmark
    public DefaultApplicationResult computedDefaults() {
        return computedApplier.applyDefaults(partialProperties);
    }

    @Benchmark
    public DefaultApplicationResult repeatedApplication() {
        // Simulate repeated application (cache scenario)
        return applier.applyDefaults(partialProperties);
    }
}
