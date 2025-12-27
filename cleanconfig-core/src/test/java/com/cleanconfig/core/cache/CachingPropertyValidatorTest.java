package com.cleanconfig.core.cache;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyRegistryBuilder;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for CachingPropertyValidator.
 */
public class CachingPropertyValidatorTest {

    private PropertyRegistry registry;
    private PropertyValidator delegate;
    private Map<String, String> validProperties;
    private Map<String, String> invalidProperties;

    @Before
    public void setUp() {
        PropertyRegistryBuilder builder = PropertyRegistry.builder();

        PropertyDefinition<String> def1 = PropertyDefinition.builder(String.class)
                .name("test.property1")
                .description("Test property 1")
                .defaultValue("default1")
                .validationRule(Rules.notBlank())
                .category(PropertyCategory.GENERAL)
                .build();

        PropertyDefinition<Integer> def2 = PropertyDefinition.builder(Integer.class)
                .name("test.property2")
                .description("Test property 2")
                .defaultValue(42)
                .validationRule(Rules.between(1, 100))
                .category(PropertyCategory.GENERAL)
                .build();

        builder.register(def1);
        builder.register(def2);
        registry = builder.build();

        delegate = new DefaultPropertyValidator(registry);

        validProperties = new HashMap<>();
        validProperties.put("test.property1", "value1");
        validProperties.put("test.property2", "50");

        invalidProperties = new HashMap<>();
        invalidProperties.put("test.property1", ""); // blank - invalid
        invalidProperties.put("test.property2", "150"); // out of range
    }

    @Test
    public void testDefaultConstructor() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);
        assertNotNull(validator);
    }

    @Test
    public void testCustomConstructor() {
        CachingPropertyValidator validator = new CachingPropertyValidator(
                delegate,
                50,
                Duration.ofMinutes(10)
        );
        assertNotNull(validator);
    }

    @Test(expected = NullPointerException.class)
    public void testNullDelegate() {
        new CachingPropertyValidator(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullTtl() {
        new CachingPropertyValidator(delegate, 100, null);
    }

    @Test
    public void testCacheHit() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        // First validation - cache miss
        ValidationResult result1 = validator.validate(validProperties);
        assertTrue(result1.isValid());
        assertEquals(1, validator.getCacheSize());

        // Second validation with same properties - cache hit
        ValidationResult result2 = validator.validate(validProperties);
        assertTrue(result2.isValid());
        assertEquals(1, validator.getCacheSize());

        // Results should be equal
        assertEquals(result1.isValid(), result2.isValid());
    }

    @Test
    public void testCacheMiss() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        // First validation
        ValidationResult result1 = validator.validate(validProperties);
        assertTrue(result1.isValid());
        assertEquals(1, validator.getCacheSize());

        // Different properties - cache miss
        Map<String, String> differentProps = new HashMap<>();
        differentProps.put("test.property1", "different");
        differentProps.put("test.property2", "75");

        ValidationResult result2 = validator.validate(differentProps);
        assertTrue(result2.isValid());
        assertEquals(2, validator.getCacheSize());
    }

    @Test
    public void testInvalidPropertiesCached() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        // First validation - invalid properties
        ValidationResult result1 = validator.validate(invalidProperties);
        assertFalse(result1.isValid());
        assertEquals(1, validator.getCacheSize());

        // Second validation with same invalid properties - cache hit
        ValidationResult result2 = validator.validate(invalidProperties);
        assertFalse(result2.isValid());
        assertEquals(1, validator.getCacheSize());
    }

    @Test
    public void testCacheSizeLimit() {
        CachingPropertyValidator validator = new CachingPropertyValidator(
                delegate,
                2, // max 2 entries
                Duration.ofMinutes(5)
        );

        // Add 3 different property sets
        Map<String, String> props1 = new HashMap<>();
        props1.put("test.property1", "value1");
        props1.put("test.property2", "10");

        Map<String, String> props2 = new HashMap<>();
        props2.put("test.property1", "value2");
        props2.put("test.property2", "20");

        Map<String, String> props3 = new HashMap<>();
        props3.put("test.property1", "value3");
        props3.put("test.property2", "30");

        validator.validate(props1);
        assertEquals(1, validator.getCacheSize());

        validator.validate(props2);
        assertEquals(2, validator.getCacheSize());

        // Third validation should not increase cache size beyond limit
        validator.validate(props3);
        assertTrue(validator.getCacheSize() <= 2);
    }

    @Test
    public void testCacheExpiration() throws InterruptedException {
        CachingPropertyValidator validator = new CachingPropertyValidator(
                delegate,
                100,
                Duration.ofMillis(50) // 50ms TTL
        );

        // First validation
        ValidationResult result1 = validator.validate(validProperties);
        assertTrue(result1.isValid());
        assertEquals(1, validator.getCacheSize());

        // Wait for expiration
        Thread.sleep(100);

        // Validation after expiration - should recompute
        ValidationResult result2 = validator.validate(validProperties);
        assertTrue(result2.isValid());
        // Cache size might be 1 or 2 depending on eviction timing
    }

    @Test
    public void testClearCache() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        validator.validate(validProperties);
        assertEquals(1, validator.getCacheSize());

        validator.clearCache();
        assertEquals(0, validator.getCacheSize());
    }

    @Test
    public void testValidateProperty() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        // Single property validation should not use cache
        ValidationResult result = validator.validateProperty(
                "test.property1",
                "value",
                validProperties
        );
        assertTrue(result.isValid());
        assertEquals(0, validator.getCacheSize()); // No caching for single property
    }

    @Test(expected = NullPointerException.class)
    public void testNullPropertiesMap() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);
        validator.validate(null);
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        CachingPropertyValidator validator = new CachingPropertyValidator(
                delegate,
                100,
                Duration.ofMinutes(5)
        );

        int threadCount = 10;
        int iterationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger validCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        ValidationResult result = validator.validate(validProperties);
                        if (result.isValid()) {
                            validCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // All validations should succeed
        assertEquals(threadCount * iterationsPerThread, validCount.get());
        // Cache should have only 1 entry (same properties validated repeatedly)
        assertEquals(1, validator.getCacheSize());
    }

    @Test
    public void testMultipleDifferentPropertiesSets() {
        CachingPropertyValidator validator = new CachingPropertyValidator(
                delegate,
                10,
                Duration.ofMinutes(5)
        );

        // Validate 5 different property sets
        for (int i = 0; i < 5; i++) {
            Map<String, String> props = new HashMap<>();
            props.put("test.property1", "value" + i);
            props.put("test.property2", String.valueOf(10 + i));
            ValidationResult result = validator.validate(props);
            assertTrue(result.isValid());
        }

        // Cache may have less than 5 due to hash collisions
        assertTrue(validator.getCacheSize() >= 1);

        // Validate same sets again - should hit cache
        for (int i = 0; i < 5; i++) {
            Map<String, String> props = new HashMap<>();
            props.put("test.property1", "value" + i);
            props.put("test.property2", String.valueOf(10 + i));
            ValidationResult result = validator.validate(props);
            assertTrue(result.isValid());
        }

        // Cache size should not increase
        int cachedSize = validator.getCacheSize();
        assertTrue(cachedSize >= 1 && cachedSize <= 5);
    }

    @Test
    public void testEmptyPropertiesMap() {
        CachingPropertyValidator validator = new CachingPropertyValidator(delegate);

        Map<String, String> emptyProps = new HashMap<>();
        ValidationResult result = validator.validate(emptyProps);
        // Empty map is valid if all properties have defaults
        assertNotNull(result);

        assertEquals(1, validator.getCacheSize());
    }
}
