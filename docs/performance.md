# Performance Optimization Guide

CleanConfig provides several performance optimization features to handle large-scale property configurations efficiently. This guide covers caching strategies, benchmarking, and performance best practices.

## Performance Features

### 1. Validation Caching

**CachingPropertyValidator** wraps any `PropertyValidator` implementation to add result caching:

```java
PropertyValidator baseValidator = new DefaultPropertyValidator(registry);
PropertyValidator cachedValidator = new CachingPropertyValidator(
    baseValidator,
    1000,                      // Max 1000 cached results
    Duration.ofMinutes(5)       // 5-minute TTL
);

// First validation - cache miss
ValidationResult result1 = cachedValidator.validate(properties);

// Subsequent validations - cache hit (instant)
ValidationResult result2 = cachedValidator.validate(properties);
```

**When to use**:
- Validating the same property set repeatedly
- Web services handling multiple requests with similar configurations
- CI/CD pipelines validating configs multiple times
- Applications with stable configuration that changes infrequently

**Trade-offs**:
- Memory usage increases with cache size
- Stale results possible if properties change frequently
- Best for read-heavy workloads

### 2. Computed Default Caching

**Memoization for expensive default computations**:

```java
PropertyDefinition<Integer> threadsDef = PropertyDefinition.builder(Integer.class)
    .name("worker.threads")
    .description("Worker thread count")
    // Expensive: system call every time
    .defaultValue(ConditionalDefaultValue.computed(ctx ->
        Optional.of(Runtime.getRuntime().availableProcessors() * 2)
    ))
    .build();

// Optimized: cached result
PropertyDefinition<Integer> optimizedDef = PropertyDefinition.builder(Integer.class)
    .name("worker.threads")
    .description("Worker thread count")
    .defaultValue(ConditionalDefaultValue.computedCached(
        ctx -> Optional.of(Runtime.getRuntime().availableProcessors() * 2),
        1  // Cache size = 1 (result never changes)
    ))
    .build();
```

**When to use**:
- System calls (CPU count, memory, disk space)
- File I/O operations
- Network calls
- Expensive calculations

**Cache sizing**:
- `cacheSize = 1`: For deterministic computations (CPU count, constants)
- `cacheSize > 1`: For context-dependent computations (different envs/modes)

### 3. Manual Cache Control

```java
CachingPropertyValidator validator = new CachingPropertyValidator(baseValidator);

// Check cache status
int size = validator.getCacheSize();

// Clear cache when properties change
validator.clearCache();
```

## Benchmarking

### Running Benchmarks

```bash
# Run all benchmarks
./gradlew :cleanconfig-benchmarks:jmh

# Run specific benchmark
./gradlew :cleanconfig-benchmarks:jmh -Pjmh.includes='.*ValidationBenchmark.*'

# Quick benchmark (fewer iterations)
./gradlew :cleanconfig-benchmarks:jmh \
  -Pjmh.warmupIterations=2 \
  -Pjmh.iterations=3
```

### Available Benchmarks

1. **ValidationBenchmark**: Property validation performance across config sizes
   - Small (10 properties)
   - Medium (50 properties)
   - Large (200 properties)

2. **CachedValidationBenchmark**: Cache effectiveness comparison
   - Non-cached baseline
   - Cached first access (miss)
   - Cached repeated access (hit)

3. **DefaultApplicationBenchmark**: Default value application
   - Static defaults
   - Computed defaults

4. **SerializationBenchmark**: Format comparison
   - Properties format
   - JSON format
   - YAML format
   - Round-trip serialization

### Understanding Results

JMH reports metrics in two modes:

**Throughput (ops/sec)**: Higher is better
```
Benchmark                           Mode  Cnt    Score    Error  Units
cached_repeatedAccess              thrpt   10  25000.0 ± 500.0  ops/s
nonCached                          thrpt   10   5000.0 ± 100.0  ops/s
```
*Cached validation is 5x faster*

**Average Time (μs/op)**: Lower is better
```
Benchmark                           Mode  Cnt  Score   Error  Units
cached_repeatedAccess               avgt   10    0.04 ± 0.001  ms/op
nonCached                           avgt   10    0.20 ± 0.005  ms/op
```
*Cached validation takes 80% less time*

## Performance Best Practices

### 1. Choose the Right Caching Strategy

**Opt-in by default**: CleanConfig uses explicit caching. Don't enable caching unless you have a performance need.

```java
// Good: Explicit caching for high-frequency validation
PropertyValidator validator = new CachingPropertyValidator(baseValidator);

// Bad: Caching low-frequency validations adds overhead
PropertyValidator validator = new CachingPropertyValidator(
    baseValidator,
    1000,
    Duration.ofDays(1)  // Properties validated once per day
);
```

### 2. Size Caches Appropriately

```java
// Good: Match cache size to actual usage
// Small service: 2-3 config variations
new CachingPropertyValidator(validator, 10, Duration.ofMinutes(5));

// Good: Large service: hundreds of tenant configs
new CachingPropertyValidator(validator, 500, Duration.ofHours(1));

// Bad: Oversized cache wastes memory
new CachingPropertyValidator(validator, 10000, Duration.ofDays(7));
```

### 3. Set Appropriate TTL

```java
// Good: Short TTL for dynamic configs
Duration.ofMinutes(5)   // Config can change

// Good: Long TTL for stable configs
Duration.ofHours(1)     // Infrequent updates

// Bad: Very long TTL risks stale data
Duration.ofDays(30)     // May miss updates
```

### 4. Clear Cache on Updates

```java
public class ConfigService {
    private final CachingPropertyValidator validator;

    public void updateConfig(Map<String, String> newConfig) {
        // Save config
        configStore.save(newConfig);

        // Clear cache to reflect changes
        validator.clearCache();
    }
}
```

### 5. Profile Before Optimizing

```java
// Bad: Premature optimization
PropertyDefinition<String> simple = PropertyDefinition.builder(String.class)
    .name("app.name")
    .defaultValue(ConditionalDefaultValue.computedCached(
        ctx -> Optional.of("MyApp"),  // Already fast!
        1
    ))
    .build();

// Good: Optimize expensive operations only
PropertyDefinition<List<String>> expensive = PropertyDefinition.builder(List.class)
    .name("available.plugins")
    .defaultValue(ConditionalDefaultValue.computedCached(
        ctx -> {
            // Expensive: File I/O + parsing
            return Optional.of(scanPluginsDirectory());
        },
        1
    ))
    .build();
```

## Benchmark Results

Real-world performance measurements from JMH benchmarks:

### Validation Performance

| Configuration Size | Throughput | Average Time | Properties |
|-------------------|-----------|--------------|------------|
| Small (10 props) | 1,767 ops/ms | 0.001 ms/op | Simple validation rules |
| Medium (50 props) | 498 ops/ms | 0.002 ms/op | Mixed validation rules |
| Large (200 props) | 133 ops/ms | 0.008 ms/op | Complex validation rules |

### Cache Effectiveness

| Operation | Throughput | Average Time | Speedup |
|-----------|-----------|--------------|---------|
| Non-cached validation | 492 ops/ms | 0.002 ms/op | Baseline |
| Cached (first access) | 464 ops/ms | 0.002 ms/op | ~1x (cache miss) |
| **Cached (repeated)** | **7,118 ops/ms** | **≈10⁻⁴ ms/op** | **14.5x faster** |

### Serialization Performance

| Format | Throughput | Average Time | Notes |
|--------|-----------|--------------|-------|
| Properties | 565 ops/ms | 0.002 ms/op | Fastest, zero dependencies |
| JSON | 397 ops/ms | 0.002 ms/op | With metadata |
| JSON (verbose) | 32 ops/ms | 0.031 ms/op | Full schema info |
| YAML | 74 ops/ms | 0.013 ms/op | Human-readable |
| YAML (verbose) | 8 ops/ms | 0.115 ms/op | Full schema info |

### Default Application

| Type | Throughput | Average Time |
|------|-----------|--------------|
| Static defaults | 532 ops/ms | 0.002 ms/op |
| Computed defaults | 494 ops/ms | 0.002 ms/op |
| Repeated application | 539 ops/ms | 0.002 ms/op |

**Key Findings:**
- ✅ Validation caching provides **14.5x speedup** for repeated validations
- ✅ Small configs process at **1,767 ops/ms** - extremely fast
- ✅ Properties format is **7.5x faster** than YAML for serialization
- ✅ Computed defaults have negligible overhead vs static defaults
- ✅ Cache warmup cost is minimal (464 vs 492 ops/ms)

*Benchmarks run on JMH 1.37 with 3 warmup iterations, 5 measurement iterations, 2 forks.*

**Test Environment:**
- **Hardware**: MacBook Pro (2024), Apple M4 Max (16 cores: 12 performance + 4 efficiency), 64 GB RAM
- **Software**: OpenJDK 11.0.28, macOS Sequoia 15.1
- **JMH Version**: 1.37

*Your results may vary based on hardware, JVM version, and system load.*

---

## Understanding the Results

### What These Numbers Mean

**Throughput (ops/ms)**: Operations per millisecond - **higher is better**
- 1,767 ops/ms = Can validate 1,767 property sets per millisecond
- Or ~1.77 million validations per second

**Average Time (ms/op)**: Time per operation - **lower is better**
- 0.001 ms/op = Each validation takes 1 microsecond
- ≈10⁻⁴ ms/op = Sub-microsecond (extremely fast)

### Performance Characteristics

**Validation scales with:**
- Number of properties (10 → 200 properties: 1767 → 133 ops/ms)
- Validation rule complexity
- Cross-property dependencies

**Caching is most effective when:**
- Same properties validated repeatedly (14.5x speedup)
- Working with medium/large configurations
- Properties change infrequently

**Serialization performance:**
- Properties format: Fastest, simplest (565 ops/ms)
- JSON: Good balance of speed and features (397 ops/ms)
- YAML: Slowest but most readable (74 ops/ms)
- Verbose modes add ~10-12x overhead for schema metadata

**Note**: Results vary based on:
- Validation rule complexity
- Number of cross-property dependencies
- System resources
- JVM warmup state

## Thread Safety

All caching features are **thread-safe**:
- `CachingPropertyValidator` uses `ConcurrentHashMap`
- `computedCached()` uses `ConcurrentHashMap`
- No external synchronization needed
- Safe for concurrent reads and writes

```java
// Safe: Multiple threads can share validator
CachingPropertyValidator validator = new CachingPropertyValidator(baseValidator);

// Thread 1
executor.submit(() -> validator.validate(props1));

// Thread 2
executor.submit(() -> validator.validate(props2));

// Thread 3
executor.submit(() -> validator.clearCache());
```

## Related Documentation

- [Validation Rules](validation.md) - Validation system overview
- [Property Definitions](../README.md) - Property system basics
- [Serialization](serialization.md) - Config serialization formats
