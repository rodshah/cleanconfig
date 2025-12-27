# CleanConfig Benchmarks

JMH (Java Microbenchmark Harness) performance benchmarks for CleanConfig.

## Purpose

This module contains performance benchmarks to:
- **Validate performance claims** (caching improvements, validation speed)
- **Prevent regressions** (detect performance degradation in CI/CD)
- **Guide optimizations** (identify bottlenecks and measure improvements)
- **Document performance** (provide real data for users)

## Running Benchmarks

### Full Benchmark Suite

```bash
# Run all benchmarks (~10 minutes)
./gradlew :cleanconfig-benchmarks:jmh
```

Results are saved to: `cleanconfig-benchmarks/build/results/jmh/results.txt`

### Quick Benchmarks

```bash
# Faster run with fewer iterations (~3 minutes)
./gradlew :cleanconfig-benchmarks:jmh \
  -Pjmh.warmupIterations=2 \
  -Pjmh.iterations=3 \
  -Pjmh.fork=1
```

### Specific Benchmarks

```bash
# Run only validation benchmarks
./gradlew :cleanconfig-benchmarks:jmh -Pjmh.includes='.*ValidationBenchmark.*'

# Run only cache comparison
./gradlew :cleanconfig-benchmarks:jmh -Pjmh.includes='.*CachedValidationBenchmark.*'

# Run only serialization benchmarks
./gradlew :cleanconfig-benchmarks:jmh -Pjmh.includes='.*SerializationBenchmark.*'
```

## Available Benchmarks

| Benchmark | Purpose |
|-----------|---------|
| `ValidationBenchmark` | Validation performance across config sizes (10, 50, 200 properties) |
| `CachedValidationBenchmark` | Cache effectiveness (non-cached vs cached validation) |
| `DefaultApplicationBenchmark` | Default value application (static vs computed) |
| `SerializationBenchmark` | Serialization formats (Properties, JSON, YAML) |

## Understanding Results

JMH reports two key metrics:

**Throughput (ops/ms)**: Operations per millisecond - **higher is better**
- Shows how many operations can be performed per millisecond
- Example: `7118.442 ops/ms` = ~7.1 million operations per second

**Average Time (ms/op)**: Time per operation - **lower is better**
- Shows how long each operation takes
- Example: `0.002 ms/op` = 2 microseconds per operation

## Current Results (2025-12-26)

| Benchmark | Throughput | Key Finding |
|-----------|-----------|-------------|
| Validation (small) | 1,767 ops/ms | Extremely fast for small configs |
| Validation (medium) | 498 ops/ms | Good performance at scale |
| **Cached validation** | **7,118 ops/ms** | **14.5x faster than non-cached** |
| Properties serialization | 565 ops/ms | Fastest format |
| JSON serialization | 397 ops/ms | Good balance |
| YAML serialization | 74 ops/ms | Slowest but readable |

**Test Environment:**
- Hardware: MacBook Pro (2024), Apple M4 Max (16 cores), 64 GB RAM
- Software: OpenJDK 11.0.28, macOS Sequoia 15.1
- JMH: 1.37 (3 warmup, 5 measurement, 2 forks)

See [docs/performance.md](../docs/performance.md) for:
- Complete benchmark results
- Performance optimization guide
- Caching strategies
- Best practices

## JMH Configuration

Configured in `build.gradle`:
- **Warmup**: 3 iterations, 1 second each
- **Measurement**: 5 iterations, 1 second each
- **Forks**: 2 (separate JVM processes for reliability)
- **Modes**: Throughput (`thrpt`) and Average Time (`avgt`)

## Development

### Adding New Benchmarks

1. Create benchmark class in `src/jmh/java/com/cleanconfig/benchmarks/`
2. Use JMH annotations: `@Benchmark`, `@State`, `@Setup`, `@Warmup`, `@Measurement`
3. Follow existing patterns in `ValidationBenchmark.java`
4. Run to verify: `./gradlew :cleanconfig-benchmarks:jmh -Pjmh.includes='.*YourBenchmark.*'`

### Example Benchmark

```java
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class MyBenchmark {

    @Setup
    public void setup() {
        // Initialize test data
    }

    @Benchmark
    public void myOperation() {
        // Code to benchmark
    }
}
```

## Notes

- Benchmarks use `src/jmh/java` (JMH convention, not `src/main/java`)
- SpotBugs excludes JMH-generated code (configured in `build.gradle`)
- Results vary by hardware, JVM, and system load
- Always run benchmarks on a quiet system for reliable results

## Links

- [JMH Homepage](https://openjdk.java.net/projects/code-tools/jmh/)
- [JMH Gradle Plugin](https://github.com/melix/jmh-gradle-plugin)
- [Performance Guide](../docs/performance.md)
