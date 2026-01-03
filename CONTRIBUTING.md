# Contributing to CleanConfig

Thank you for your interest in contributing to CleanConfig! This document provides guidelines for contributing to the project.

## Code of Conduct

Be respectful and professional. We want CleanConfig to be a welcoming project for all contributors.

## How to Contribute

### Reporting Issues

- Use GitHub Issues to report bugs or suggest features
- Search existing issues before creating a new one
- Provide detailed information:
  - Steps to reproduce (for bugs)
  - Expected vs actual behavior
  - Java version and OS
  - Minimal code example if applicable

### Submitting Pull Requests

1. **Fork the repository**
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Write tests** for your changes
5. **Run the test suite**:
   ```bash
   ./gradlew test
   ```
6. **Check code coverage**:
   ```bash
   ./gradlew jacocoTestReport
   # Target: >95% coverage for new code
   ```
7. **Run code quality checks**:
   ```bash
   ./gradlew checkstyleMain checkstyleTest
   ./gradlew spotbugsMain spotbugsTest
   ```
8. **Commit your changes** following our commit message format (see below)
9. **Push to your fork**
10. **Open a Pull Request** against `main`

## Development Setup

### Prerequisites

- JDK 11 or higher (we test on 11, 17, 21)
- Gradle 7.6+ (wrapper included)
- Git

### Building

```bash
# Clone your fork
git clone https://github.com/your-username/cleanconfig.git
cd cleanconfig

# Build
./gradlew build

# Run tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run code quality checks
./gradlew check
```

## Code Style

### Java Code Style

- **Java 11 compatible code** - Can use Java 11 features
- **Use builders for complex objects** with many fields
- **Prefer immutability** - final fields, no setters
- **Functional interfaces** where appropriate
- **Comprehensive Javadoc** for all public APIs
- **No external dependencies** in cleanconfig-core (except SLF4J API compile-only)
- **Optional dependencies** in cleanconfig-serialization (Jackson loaded via reflection)

### Example Code Style

```java
/**
 * Validates property values with access to context.
 *
 * @param <T> the type of value to validate
 * @since 0.1.0
 */
@FunctionalInterface
public interface ValidationRule<T> {

    /**
     * Validates a property value.
     *
     * @param propertyName the name of the property being validated
     * @param value the value to validate (may be null)
     * @param context access to all properties and validation state
     * @return validation result indicating success or failure
     */
    ValidationResult validate(String propertyName, T value, PropertyContext context);

    /**
     * Combines this rule with another using AND logic.
     *
     * @param other the other rule to combine with
     * @return a new rule that passes only if both rules pass
     */
    default ValidationRule<T> and(ValidationRule<T> other) {
        return (name, value, context) -> {
            ValidationResult first = this.validate(name, value, context);
            if (!first.isValid()) {
                return first;
            }
            return other.validate(name, value, context);
        };
    }
}
```

### Naming Conventions

- Classes: `PascalCase` (e.g., `PropertyValidator`)
- Interfaces: `PascalCase` (e.g., `ValidationRule`)
- Methods: `camelCase` (e.g., `validate()`, `applyDefaults()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `SERVER_PORT`)
- Variables: `camelCase` (e.g., `propertyName`)
- Packages: lowercase (e.g., `com.cleanconfig.core.validation`)

## Testing Guidelines

### Unit Tests

- **Write tests for all new code** (target >95% coverage)
- **Test edge cases** (null, empty, invalid input)
- **Use descriptive test names**: `testMethodName_WhenCondition_ThenExpectedBehavior()`
- **Use AssertJ** for fluent assertions
- **Mock external dependencies** with Mockito

### Test Example

```java
@Test
public void validate_WhenPropertyValueIsNull_AndPropertyIsRequired_ThenReturnsFailure() {
    // Arrange
    ValidationRule<String> rule = Rules.required();
    PropertyContext context = DefaultPropertyContext.builder()
        .properties(Collections.emptyMap())
        .build();

    // Act
    ValidationResult result = rule.validate("test.property", null, context);

    // Assert
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("test.property");
}
```

## Commit Message Format

Use conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Examples:**

```
feat(core): add multi-property validation rule

Implement MultiPropertyValidationRule interface that allows
validation across multiple property values.

Closes #42
```

```
fix(validator): correct circular dependency detection

The topological sort was not detecting all circular dependencies.
Updated Kahn's algorithm implementation to properly detect cycles.

Fixes #38
```

## Logging Guidelines

- Use the CleanConfig logging abstraction (`LoggerFactory.getLogger()`)
- **NEVER log sensitive data** (passwords, tokens, API keys)
- Use appropriate log levels:
  - **ERROR**: System errors, exceptions
  - **WARN**: Potential issues, validation failures
  - **INFO**: Important milestones (validation started/completed)
  - **DEBUG**: Detailed flow for debugging
- Include context in log messages:
  ```java
  logger.debug("Validating property: name={}, value={}", propertyName, sanitized(value));
  ```

## Documentation Guidelines

### Javadoc Requirements

- **All public classes and interfaces** must have class-level Javadoc
- **All public methods** must have Javadoc with @param, @return, @throws
- **Include code examples** in Javadoc where helpful
- **Add @since tag** for version tracking

### Documentation Files

When adding features, also update:
- Module README (e.g., `cleanconfig-core/README.md`, `cleanconfig-serialization/README.md`)
- User guide (`docs/user-guide.md`)
- Feature guides (`docs/validation-rules.md`, `docs/serialization.md`, etc.)
- Code examples (`cleanconfig-examples/`)

## Pull Request Checklist

Before submitting your PR, ensure:

- [ ] Code compiles without warnings
- [ ] All tests pass (`./gradlew test`)
- [ ] Code coverage >95% for new code
- [ ] Checkstyle passes (`./gradlew checkstyleMain`)
- [ ] SpotBugs passes (`./gradlew spotbugsMain`)
- [ ] Javadoc is complete for public APIs
- [ ] Code examples updated (if applicable)
- [ ] Documentation updated (if applicable)
- [ ] Commit messages follow format
- [ ] PR description explains what and why

## Questions?

- Open a GitHub Issue for questions
- Reference [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for project structure
- Check existing code for examples

## License

By contributing to CleanConfig, you agree that your contributions will be licensed under the Apache License 2.0.
