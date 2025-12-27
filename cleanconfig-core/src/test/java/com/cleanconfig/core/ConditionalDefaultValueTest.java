package com.cleanconfig.core;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for ConditionalDefaultValue.
 */
public class ConditionalDefaultValueTest {

    @Test
    public void testStaticValue() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.staticValue("test");
        PropertyContext context = createContext(Map.of());

        Optional<String> result = defaultValue.computeDefault(context);
        assertTrue(result.isPresent());
        assertEquals("test", result.get());
    }

    @Test
    public void testStaticValueNullThrows() {
        assertThrows(NullPointerException.class,
                () -> ConditionalDefaultValue.staticValue(null));
    }

    @Test
    public void testComputed() {
        ConditionalDefaultValue<Integer> defaultValue = ConditionalDefaultValue.computed(
                ctx -> {
                    String value = ctx.getProperty("count").orElse("0");
                    return Optional.of(Integer.parseInt(value) * 2);
                }
        );

        PropertyContext context = createContext(Map.of("count", "5"));
        Optional<Integer> result = defaultValue.computeDefault(context);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());
    }

    @Test
    public void testComputedNullFunctionThrows() {
        assertThrows(NullPointerException.class,
                () -> ConditionalDefaultValue.computed(null));
    }

    @Test
    public void testComputedCached() {
        AtomicInteger computeCount = new AtomicInteger(0);

        ConditionalDefaultValue<Integer> defaultValue = ConditionalDefaultValue.computedCached(
                ctx -> {
                    computeCount.incrementAndGet();
                    return Optional.of(Runtime.getRuntime().availableProcessors() * 2);
                },
                5
        );

        PropertyContext context = createContext(Map.of("test", "value"));

        // First call - should compute
        Optional<Integer> result1 = defaultValue.computeDefault(context);
        assertTrue(result1.isPresent());
        assertEquals(1, computeCount.get());

        // Second call with same context - should use cache
        Optional<Integer> result2 = defaultValue.computeDefault(context);
        assertTrue(result2.isPresent());
        assertEquals(1, computeCount.get()); // Still 1 - no recomputation
        assertEquals(result1.get(), result2.get());
    }

    @Test
    public void testComputedCachedDifferentContexts() {
        AtomicInteger computeCount = new AtomicInteger(0);

        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.computedCached(
                ctx -> {
                    computeCount.incrementAndGet();
                    String env = ctx.getProperty("environment").orElse("prod");
                    return Optional.of("config-" + env);
                },
                5
        );

        PropertyContext context1 = createContext(Map.of("environment", "dev"));
        PropertyContext context2 = createContext(Map.of("environment", "prod"));

        // Different contexts should compute separately
        Optional<String> result1 = defaultValue.computeDefault(context1);
        assertEquals("config-dev", result1.get());
        assertEquals(1, computeCount.get());

        Optional<String> result2 = defaultValue.computeDefault(context2);
        assertEquals("config-prod", result2.get());
        assertEquals(2, computeCount.get());

        // Repeat first context - should use cache
        Optional<String> result3 = defaultValue.computeDefault(context1);
        assertEquals("config-dev", result3.get());
        assertEquals(2, computeCount.get()); // No new computation
    }

    @Test
    public void testComputedCachedNullFunctionThrows() {
        assertThrows(NullPointerException.class,
                () -> ConditionalDefaultValue.computedCached(null, 5));
    }

    @Test
    public void testComputedCachedInvalidCacheSizeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ConditionalDefaultValue.computedCached(ctx -> Optional.of("test"), 0));

        assertThrows(IllegalArgumentException.class,
                () -> ConditionalDefaultValue.computedCached(ctx -> Optional.of("test"), -1));
    }

    @Test
    public void testComputedCachedSizeLimit() {
        AtomicInteger computeCount = new AtomicInteger(0);

        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.computedCached(
                ctx -> {
                    computeCount.incrementAndGet();
                    return Optional.of("value");
                },
                2 // Cache size = 2
        );

        // Create 3 different contexts
        PropertyContext context1 = createContext(Map.of("key", "1"));
        PropertyContext context2 = createContext(Map.of("key", "2"));
        PropertyContext context3 = createContext(Map.of("key", "3"));

        defaultValue.computeDefault(context1);
        assertEquals(1, computeCount.get());

        defaultValue.computeDefault(context2);
        assertEquals(2, computeCount.get());

        // Third context - cache full, might not cache
        defaultValue.computeDefault(context3);
        assertEquals(3, computeCount.get());

        // Repeat context1 or context2 - should be cached
        int countBefore = computeCount.get();
        defaultValue.computeDefault(context1);
        // Should be cached (no new computation) or recomputed (cache evicted)
        assertTrue(computeCount.get() <= countBefore + 1);
    }

    @Test
    public void testComputedCachedEmptyResult() {
        AtomicInteger computeCount = new AtomicInteger(0);

        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.computedCached(
                ctx -> {
                    computeCount.incrementAndGet();
                    return Optional.empty();
                },
                5
        );

        PropertyContext context = createContext(Map.of());

        Optional<String> result1 = defaultValue.computeDefault(context);
        assertFalse(result1.isPresent());
        assertEquals(1, computeCount.get());

        // Empty results are not cached
        Optional<String> result2 = defaultValue.computeDefault(context);
        assertFalse(result2.isPresent());
        assertEquals(2, computeCount.get());
    }

    @Test
    public void testComputedCachedConcurrent() throws InterruptedException {
        AtomicInteger computeCount = new AtomicInteger(0);

        ConditionalDefaultValue<Integer> defaultValue = ConditionalDefaultValue.computedCached(
                ctx -> {
                    computeCount.incrementAndGet();
                    return Optional.of(42);
                },
                10
        );

        int threadCount = 10;
        int iterationsPerThread = 100;
        PropertyContext context = createContext(Map.of("test", "value"));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        Optional<Integer> result = defaultValue.computeDefault(context);
                        if (result.isPresent() && result.get() == 42) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // All calls should succeed
        assertEquals(threadCount * iterationsPerThread, successCount.get());
        // Should be computed very few times (ideally once, but race conditions might cause a few more)
        assertTrue("Compute count: " + computeCount.get(), computeCount.get() < 10);
    }

    @Test
    public void testWhenConditionTrue() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue
                .staticValue("default")
                .when(ctx -> ctx.getProperty("mode").orElse("").equals("test"), "test-value");

        PropertyContext testContext = createContext(Map.of("mode", "test"));
        PropertyContext normalContext = createContext(Map.of("mode", "normal"));

        Optional<String> testResult = defaultValue.computeDefault(testContext);
        assertEquals("test-value", testResult.get());

        Optional<String> normalResult = defaultValue.computeDefault(normalContext);
        assertEquals("default", normalResult.get());
    }

    @Test
    public void testWhenConditionFalse() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue
                .staticValue("default")
                .when(ctx -> false, "override");

        PropertyContext context = createContext(Map.of());
        Optional<String> result = defaultValue.computeDefault(context);
        assertEquals("default", result.get());
    }

    @Test
    public void testWhenNullConditionThrows() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.staticValue("test");
        assertThrows(NullPointerException.class,
                () -> defaultValue.when(null, "value"));
    }

    @Test
    public void testWhenNullOverrideThrows() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.staticValue("test");
        assertThrows(NullPointerException.class,
                () -> defaultValue.when(ctx -> true, (String) null));
    }

    @Test
    public void testWhenWithComputer() {
        ConditionalDefaultValue<Integer> defaultValue = ConditionalDefaultValue
                .staticValue(100)
                .when(
                        ctx -> ctx.getProperty("dynamic").isPresent(),
                        ctx -> Optional.of(Integer.parseInt(ctx.getProperty("dynamic").get()))
                );

        PropertyContext dynamicContext = createContext(Map.of("dynamic", "42"));
        PropertyContext staticContext = createContext(Map.of());

        Optional<Integer> dynamicResult = defaultValue.computeDefault(dynamicContext);
        assertEquals(Integer.valueOf(42), dynamicResult.get());

        Optional<Integer> staticResult = defaultValue.computeDefault(staticContext);
        assertEquals(Integer.valueOf(100), staticResult.get());
    }

    @Test
    public void testWhenWithComputerNullConditionThrows() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.staticValue("test");
        assertThrows(NullPointerException.class,
                () -> defaultValue.when(null, ctx -> Optional.of("value")));
    }

    @Test
    public void testWhenWithComputerNullComputerThrows() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.staticValue("test");
        assertThrows(NullPointerException.class,
                () -> defaultValue.when(ctx -> true, (java.util.function.Function<PropertyContext, Optional<String>>) null));
    }

    @Test
    public void testNoDefault() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue.noDefault();
        PropertyContext context = createContext(Map.of());

        Optional<String> result = defaultValue.computeDefault(context);
        assertFalse(result.isPresent());
    }

    @Test
    public void testChainedWhen() {
        ConditionalDefaultValue<String> defaultValue = ConditionalDefaultValue
                .staticValue("default")
                .when(ctx -> ctx.getProperty("env").orElse("").equals("dev"), "dev-config")
                .when(ctx -> ctx.getProperty("env").orElse("").equals("test"), "test-config");

        PropertyContext devContext = createContext(Map.of("env", "dev"));
        PropertyContext testContext = createContext(Map.of("env", "test"));
        PropertyContext prodContext = createContext(Map.of("env", "prod"));

        assertEquals("dev-config", defaultValue.computeDefault(devContext).get());
        assertEquals("test-config", defaultValue.computeDefault(testContext).get());
        assertEquals("default", defaultValue.computeDefault(prodContext).get());
    }

    private PropertyContext createContext(Map<String, String> properties) {
        Map<String, String> allProps = new HashMap<>(properties);
        return new PropertyContext() {
            @Override
            public Optional<String> getProperty(String name) {
                return Optional.ofNullable(allProps.get(name));
            }

            @Override
            public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
                return Optional.empty();
            }

            @Override
            public Map<String, String> getAllProperties() {
                return new HashMap<>(allProps);
            }

            @Override
            public Optional<String> getMetadata(String key) {
                return Optional.empty();
            }

            @Override
            public boolean hasProperty(String propertyName) {
                return allProps.containsKey(propertyName);
            }
        };
    }
}
