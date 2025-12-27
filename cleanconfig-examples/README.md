# CleanConfig Examples

This module contains runnable examples demonstrating CleanConfig's core features.

## Running the Examples

```bash
# Build the project
./gradlew cleanconfig-examples:build

# Run an example
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.BasicTypeSafetyExample
```

Or use your IDE to run the `main()` method in any example class.

## Core Examples

### 1. BasicTypeSafetyExample

Demonstrates type-safe property definitions with compile-time type checking.

**Key Concepts:**
- Type-safe `PropertyDefinition<T>` instead of untyped strings
- Automatic type conversion
- Type validation errors
- Property registry

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.BasicTypeSafetyExample
```

### 2. ComposableValidationExample

Shows how simple validation rules compose into complex validation logic.

**Key Concepts:**
- `.and()` - Combine rules with AND logic
- `.or()` - Combine rules with OR logic
- Reusable validation rules
- Domain-specific validation (email, alphanumeric, length)

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.ComposableValidationExample
```

### 3. ConditionalDefaultsExample

Demonstrates default values that depend on other properties or context.

**Key Concepts:**
- Static defaults
- Computed defaults based on other properties
- Environment-aware defaults
- Multi-dependency defaults

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.ConditionalDefaultsExample
```

### 4. DependencyAwareValidationExample

Shows how properties can depend on each other for validation.

**Key Concepts:**
- `dependsOnForValidation()` - Explicit dependencies
- Validation order (topological sort)
- Cross-property validation
- Circular dependency detection

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.DependencyAwareValidationExample
```

### 5. ContextBasedConfigExample

Demonstrates validation that adapts to different contexts (startup/runtime, production/development).

**Key Concepts:**
- `ValidationContextType` - Different validation contexts
- `.onlyIf()` - Conditional validation
- Context-aware rules
- Environment-specific validation

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.ContextBasedConfigExample
```

### 6. SerializationExample

Shows how to serialize and deserialize properties in multiple formats.

**Key Concepts:**
- Properties format (no dependencies)
- JSON serialization with Jackson
- YAML serialization
- Metadata inclusion
- Round-trip serialization
- Including default values

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.SerializationExample
```

### 7. CachingExample

Demonstrates performance optimization through validation caching and computed default memoization.

**Key Concepts:**
- `CachingPropertyValidator` - Cache validation results (14.5x speedup)
- `computedCached()` - Memoize expensive default computations
- Manual cache control (clear, size)
- Performance measurement and comparison
- When to use caching strategies

**Run:**
```bash
./gradlew :cleanconfig-examples:run -PmainClass=com.cleanconfig.examples.CachingExample
```

**Expected Output:**
```
Validated 1000 times:
  Non-cached: 2.05 ms (487 ops/ms)
  Cached:     0.14 ms (7142 ops/ms)
  Speedup:    14.7x faster with caching!
```

## Example Output

Each example prints detailed output showing:
- ✓ Successful validation scenarios
- ✗ Failed validation with error messages
- Applied defaults and their values
- Context-specific behavior

## Key Takeaways

1. **Type Safety**: Catch configuration errors at compile time
2. **Composability**: Build complex rules from simple building blocks
3. **Flexibility**: Defaults adapt to your configuration
4. **Dependencies**: Properties validate in correct order
5. **Context-Aware**: Different rules for different environments
6. **Serialization**: Export/import properties in multiple formats
7. **Performance**: 14.5x faster validation with optional caching

## See Also

- [Core Module README](../cleanconfig-core/README.md) - Complete API documentation
- [Validation Rules](../docs/validation-rules.md) - All available validation rules
- [Performance Guide](../docs/performance.md) - Benchmarks and optimization strategies
- [Developer Guide](../docs/developer-guide.md) - How to extend CleanConfig

## License

Apache License 2.0
