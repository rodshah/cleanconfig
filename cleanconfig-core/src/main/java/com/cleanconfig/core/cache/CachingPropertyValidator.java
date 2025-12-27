package com.cleanconfig.core.cache;

import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.validation.ValidationResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching wrapper for PropertyValidator.
 *
 * <p>This class adds result caching to any PropertyValidator implementation,
 * significantly improving performance when validating the same properties repeatedly.
 *
 * <p>Example usage:
 * <pre>
 * PropertyValidator validator = new DefaultPropertyValidator(registry);
 * PropertyValidator cachedValidator = new CachingPropertyValidator(
 *     validator,
 *     1000,  // max 1000 cached results
 *     Duration.ofMinutes(5)  // expire after 5 minutes
 * );
 * </pre>
 *
 * <p>Thread-safe and suitable for concurrent use.
 *
 * @since 0.1.0
 */
public class CachingPropertyValidator implements PropertyValidator {

    private final PropertyValidator delegate;
    private final int maxSize;
    private final Duration ttl;
    private final ConcurrentHashMap<CacheKey, CacheEntry> cache;

    /**
     * Creates a caching validator with default settings.
     *
     * @param delegate the underlying validator
     */
    public CachingPropertyValidator(PropertyValidator delegate) {
        this(delegate, 100, Duration.ofMinutes(5));
    }

    /**
     * Creates a caching validator with custom settings.
     *
     * @param delegate the underlying validator
     * @param maxSize maximum number of cached results
     * @param ttl time-to-live for cached entries
     */
    public CachingPropertyValidator(PropertyValidator delegate, int maxSize, Duration ttl) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate validator cannot be null");
        this.maxSize = maxSize;
        this.ttl = Objects.requireNonNull(ttl, "TTL cannot be null");
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public ValidationResult validate(Map<String, String> properties) {
        Objects.requireNonNull(properties, "Properties cannot be null");

        CacheKey key = CacheKey.from(properties);

        // Check cache
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired(ttl)) {
            return entry.result;
        }

        // Cache miss or expired - perform validation
        ValidationResult result = delegate.validate(properties);

        // Store in cache (with size limit)
        if (cache.size() < maxSize) {
            cache.put(key, new CacheEntry(result));
        } else {
            // Cache full - evict expired entries
            evictExpired();
            if (cache.size() < maxSize) {
                cache.put(key, new CacheEntry(result));
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateProperty(String propertyName, String value, Map<String, String> properties) {
        // Single property validation doesn't benefit from caching as much
        // Delegate directly to avoid cache pollution
        return delegate.validateProperty(propertyName, value, properties);
    }

    @Override
    public ValidationResult validatePropertyGroup(com.cleanconfig.core.validation.PropertyGroup group, Map<String, String> properties) {
        // Property group validation doesn't benefit from caching as much
        // Delegate directly to avoid cache pollution
        return delegate.validatePropertyGroup(group, properties);
    }

    /**
     * Clears the validation cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Gets the current cache size.
     *
     * @return number of cached entries
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Evicts expired entries from the cache.
     */
    private void evictExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(ttl));
    }

    /**
     * Cache key based on property map hash code.
     */
    private static class CacheKey {
        private final int hashCode;

        private CacheKey(int hashCode) {
            this.hashCode = hashCode;
        }

        static CacheKey from(Map<String, String> properties) {
            return new CacheKey(properties.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return hashCode == cacheKey.hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Cache entry with timestamp.
     */
    private static class CacheEntry {
        private final ValidationResult result;
        private final Instant timestamp;

        CacheEntry(ValidationResult result) {
            this.result = result;
            this.timestamp = Instant.now();
        }

        boolean isExpired(Duration ttl) {
            return Duration.between(timestamp, Instant.now()).compareTo(ttl) > 0;
        }
    }
}
