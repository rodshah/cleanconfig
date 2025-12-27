package com.cleanconfig.examples;

import com.cleanconfig.core.ConditionalDefaultValue;
import com.cleanconfig.core.DefaultApplicationResult;
import com.cleanconfig.core.DefaultValueApplier;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.impl.DefaultValueApplierImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Example demonstrating conditional and computed default values.
 *
 * <p>This example shows how defaults can depend on other property values
 * or be computed based on context, enabling sophisticated configuration patterns.
 */
public class ConditionalDefaultsExample {

    public static void main(String[] args) {
        System.out.println("=== Conditional Defaults Example ===\n");

        // Example 1: Static default
        PropertyDefinition<String> environment = PropertyDefinition.builder(String.class)
                .name("environment")
                .description("Deployment environment")
                .defaultValue("development")
                .build();

        // Example 2: Computed default based on another property
        PropertyDefinition<Integer> retryCount = PropertyDefinition.builder(Integer.class)
                .name("retry.count")
                .description("Number of retries (0 if retry disabled)")
                .defaultValue(ConditionalDefaultValue.computed(ctx ->
                    ctx.getTypedProperty("retry.enabled", Boolean.class).orElse(false)
                        ? Optional.of(3)
                        : Optional.of(0)
                ))
                .build();

        PropertyDefinition<Boolean> retryEnabled = PropertyDefinition.builder(Boolean.class)
                .name("retry.enabled")
                .description("Enable retry mechanism")
                .defaultValue(true)
                .build();

        // Example 3: Conditional default based on environment
        PropertyDefinition<Integer> connectionTimeout = PropertyDefinition.builder(Integer.class)
                .name("connection.timeout")
                .description("Connection timeout in seconds")
                .defaultValue(ConditionalDefaultValue.computed(ctx -> {
                    String env = ctx.getProperty("environment").orElse("development");
                    return env.equals("production") ? Optional.of(30) : Optional.of(5);
                }))
                .build();

        // Example 4: Computed default with multiple dependencies
        PropertyDefinition<Integer> threadPoolSize = PropertyDefinition.builder(Integer.class)
                .name("threadpool.size")
                .description("Thread pool size based on environment and cores")
                .defaultValue(ConditionalDefaultValue.computed(ctx -> {
                    String env = ctx.getProperty("environment").orElse("development");
                    Boolean highLoad = ctx.getTypedProperty("highLoad.expected", Boolean.class).orElse(false);

                    int cores = Runtime.getRuntime().availableProcessors();
                    if (env.equals("production") && highLoad) {
                        return Optional.of(cores * 4);
                    } else if (env.equals("production")) {
                        return Optional.of(cores * 2);
                    } else {
                        return Optional.of(cores);
                    }
                }))
                .build();

        PropertyDefinition<Boolean> highLoad = PropertyDefinition.builder(Boolean.class)
                .name("highLoad.expected")
                .description("Expect high load")
                .defaultValue(false)
                .build();

        // Build registry
        PropertyRegistry registry = PropertyRegistry.builder()
                .register(environment)
                .register(retryEnabled)
                .register(retryCount)
                .register(connectionTimeout)
                .register(highLoad)
                .register(threadPoolSize)
                .build();

        DefaultValueApplier applier = new DefaultValueApplierImpl(registry);

        // Scenario 1: Development environment with defaults
        System.out.println("Scenario 1: Development environment (all defaults)");
        Map<String, String> devProps = new HashMap<>();
        DefaultApplicationResult devResult = applier.applyDefaults(devProps);
        printProperties(devResult);

        System.out.println();

        // Scenario 2: Production environment
        System.out.println("Scenario 2: Production environment");
        Map<String, String> prodProps = new HashMap<>();
        prodProps.put("environment", "production");
        DefaultApplicationResult prodResult = applier.applyDefaults(prodProps);
        printProperties(prodResult);

        System.out.println();

        // Scenario 3: Retry disabled
        System.out.println("Scenario 3: Retry disabled");
        Map<String, String> noRetryProps = new HashMap<>();
        noRetryProps.put("retry.enabled", "false");
        DefaultApplicationResult noRetryResult = applier.applyDefaults(noRetryProps);
        printProperties(noRetryResult);

        System.out.println();

        // Scenario 4: Production with high load
        System.out.println("Scenario 4: Production with high load expected");
        Map<String, String> highLoadProps = new HashMap<>();
        highLoadProps.put("environment", "production");
        highLoadProps.put("highLoad.expected", "true");
        DefaultApplicationResult highLoadResult = applier.applyDefaults(highLoadProps);
        printProperties(highLoadResult);

        System.out.println("\n=== Conditional defaults adapt to your configuration! ===");
    }

    private static void printProperties(DefaultApplicationResult result) {
        Map<String, String> props = result.getPropertiesWithDefaults();
        System.out.println("  environment: " + props.get("environment"));
        System.out.println("  retry.enabled: " + props.get("retry.enabled"));
        System.out.println("  retry.count: " + props.get("retry.count"));
        System.out.println("  connection.timeout: " + props.get("connection.timeout"));
        System.out.println("  highLoad.expected: " + props.get("highLoad.expected"));
        System.out.println("  threadpool.size: " + props.get("threadpool.size"));

        int appliedCount = result.getApplicationInfo().getAppliedDefaultsCount();
        System.out.println("  → " + appliedCount + " defaults applied");
    }
}
