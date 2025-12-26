# Validation Rules in PropKit

PropKit provides 40+ pre-built validation rules organized into four categories.

## Quick Start

```java
// Use the Rules facade
ValidationRule<String> emailRule = Rules.notBlank()
    .and(Rules.email())
    .and(Rules.endsWith("@company.com"));

// Or use specific category classes
ValidationRule<Integer> portRule = NumericRules.port();
ValidationRule<String> pathRule = FileRules.fileExists().and(FileRules.readable());
```

## String Rules (17 rules)

| Rule | Description | Example |
|------|-------------|---------|
| `notBlank()` | Not null, not empty after trim | `Rules.notBlank()` |
| `notEmpty()` | Not null, length > 0 | `Rules.notEmpty()` |
| `minLength(n)` | Minimum length | `Rules.minLength(5)` |
| `maxLength(n)` | Maximum length | `Rules.maxLength(100)` |
| `lengthBetween(min, max)` | Length in range | `Rules.lengthBetween(5, 100)` |
| `matchesRegex(regex)` | Matches pattern | `Rules.matchesRegex("^[a-z]+$")` |
| `email()` | Valid email address | `Rules.email()` |
| `url()` | Valid URL | `Rules.url()` |
| `startsWith(prefix)` | Starts with string | `Rules.startsWith("https://")` |
| `endsWith(suffix)` | Ends with string | `Rules.endsWith(".txt")` |
| `contains(substring)` | Contains string | `Rules.contains("example")` |
| `doesNotContain(substring)` | Does not contain | `Rules.doesNotContain("admin")` |
| `alphanumeric()` | Only letters and digits | `Rules.alphanumeric()` |
| `alphabetic()` | Only letters | `Rules.alphabetic()` |
| `numeric()` | Only digits | `Rules.numeric()` |
| `lowercase()` | All lowercase | `Rules.lowercase()` |
| `uppercase()` | All uppercase | `Rules.uppercase()` |

## Numeric Rules (17 rules)

| Rule | Description | Example |
|------|-------------|---------|
| `positive()` | Greater than zero | `Rules.positive()` |
| `negative()` | Less than zero | `Rules.negative()` |
| `nonNegative()` | Greater than or equal to zero | `Rules.nonNegative()` |
| `nonPositive()` | Less than or equal to zero | `Rules.nonPositive()` |
| `zero()` | Equals zero | `Rules.zero()` |
| `min(n)` | Minimum value | `Rules.min(10)` |
| `max(n)` | Maximum value | `Rules.max(100)` |
| `between(min, max)` | Value in range | `Rules.between(0.0, 1.0)` |
| `integerBetween(min, max)` | Integer in range | `Rules.integerBetween(1, 100)` |
| `greaterThan(n)` | Strictly greater than | `Rules.greaterThan(0)` |
| `lessThan(n)` | Strictly less than | `Rules.lessThan(100)` |
| `port()` | Valid port (1-65535) | `Rules.port()` |
| `even()` | Even number | `Rules.even()` |
| `odd()` | Odd number | `Rules.odd()` |
| `multipleOf(n)` | Multiple of number | `Rules.multipleOf(5)` |

## File Rules (12 rules)

| Rule | Description | Example |
|------|-------------|---------|
| `exists()` | File/directory exists | `Rules.exists()` |
| `fileExists()` | File exists (alias) | `Rules.fileExists()` |
| `directoryExists()` | Directory exists | `Rules.directoryExists()` |
| `readable()` | File is readable | `Rules.readable()` |
| `writable()` | File is writable | `Rules.writable()` |
| `executable()` | File is executable | `Rules.executable()` |
| `isDirectory()` | Path is directory | `Rules.isDirectory()` |
| `isFile()` | Path is regular file | `Rules.isFile()` |
| `isSymbolicLink()` | Path is symlink | `FileRules.isSymbolicLink()` |
| `isHidden()` | Path is hidden | `FileRules.isHidden()` |
| `isEmptyDirectory()` | Directory is empty | `FileRules.isEmptyDirectory()` |
| `hasExtension(ext)` | File has extension | `Rules.hasExtension(".txt")` |
| `fileSizeBetween(min, max)` | File size in range (bytes) | `FileRules.fileSizeBetween(100, 1000)` |

## General Rules (12 rules)

| Rule | Description | Example |
|------|-------------|---------|
| `required()` | Not null | `Rules.required()` |
| `notNull()` | Not null (alias) | `Rules.notNull()` |
| `oneOf(values...)` | Value in set | `Rules.oneOf("dev", "prod")` |
| `noneOf(values...)` | Value not in set | `Rules.noneOf("admin", "root")` |
| `equalTo(value)` | Equals specific value | `Rules.equalTo("expected")` |
| `notEqualTo(value)` | Not equal to value | `Rules.notEqualTo("forbidden")` |
| `custom(predicate, msg)` | Custom predicate | `Rules.custom(v -> v.length() > 5, "Too short")` |
| `customWithContext(predicate, msg)` | Custom with context | `GeneralRules.customWithContext(...)` |
| `comparableBetween(min, max)` | Comparable in range | `GeneralRules.comparableBetween(1, 100)` |
| `comparableGreaterThan(n)` | Comparable > n | `GeneralRules.comparableGreaterThan(0)` |
| `comparableLessThan(n)` | Comparable < n | `GeneralRules.comparableLessThan(100)` |

## Conditions (20+ conditions)

Conditions are predicates used with `.onlyIf()` for conditional validation.

| Condition | Description | Example |
|-----------|-------------|---------|
| `propertyEquals(name, value)` | Property equals value | `Conditions.propertyEquals("ssl.enabled", "true")` |
| `propertyNotEquals(name, value)` | Property not equals | `Conditions.propertyNotEquals("mode", "test")` |
| `propertyIsPresent(name)` | Property exists | `Conditions.propertyIsPresent("api.key")` |
| `propertyIsAbsent(name)` | Property missing | `Conditions.propertyIsAbsent("debug")` |
| `propertyIsTrue(name)` | Property is true | `Conditions.propertyIsTrue("enabled")` |
| `propertyIsFalse(name)` | Property is false | `Conditions.propertyIsFalse("disabled")` |
| `contextTypeIs(type)` | Context type matches | `Conditions.contextTypeIs(STARTUP)` |
| `contextTypeIsNot(type)` | Context type differs | `Conditions.contextTypeIsNot(TESTING)` |
| `and(conditions...)` | All conditions true | `Conditions.and(c1, c2, c3)` |
| `or(conditions...)` | Any condition true | `Conditions.or(c1, c2, c3)` |
| `not(condition)` | Negate condition | `Conditions.not(c)` |

See [Conditions.java](../propkit-core/src/main/java/com/propkit/core/validation/Conditions.java) for complete list.

## Rule Composition

Combine rules using `.and()`, `.or()`, and `.onlyIf()`:

```java
// AND: All rules must pass
ValidationRule<String> strictEmail = Rules.notBlank()
    .and(Rules.email())
    .and(Rules.endsWith("@company.com"));

// OR: Either rule must pass
ValidationRule<Integer> portOrDisabled = Rules.port()
    .or(Rules.equalTo(-1));  // -1 means disabled

// Conditional: Only validate if condition is true
ValidationRule<String> sslCert = Rules.fileExists()
    .onlyIf(Conditions.propertyIsTrue("ssl.enabled"));

// Complex: Combine multiple patterns
ValidationRule<String> apiKey = Rules.required()
    .and(Rules.minLength(32))
    .and(Rules.alphanumeric())
    .onlyIf(Conditions.contextTypeIs(ValidationContextType.STARTUP));
```

## Custom Rules

Create custom rules for specialized logic:

```java
// Inline
ValidationRule<String> custom = (name, value, context) -> {
    if (value != null && value.contains("forbidden")) {
        return ValidationResult.failure(
            ValidationError.builder()
                .propertyName(name)
                .errorMessage("Value contains forbidden word")
                .actualValue(value)
                .build()
        );
    }
    return ValidationResult.success();
};

// Using helper
ValidationRule<String> custom = Rules.custom(
    value -> !value.contains("forbidden"),
    "Value contains forbidden word"
);

// Cross-property validation
ValidationRule<String> cpuLimit = (name, value, context) -> {
    return context.getProperty("cpu.request")
        .map(request -> {
            if (Double.parseDouble(value) < Double.parseDouble(request)) {
                return ValidationResult.failure(...);
            }
            return ValidationResult.success();
        })
        .orElse(ValidationResult.success());
};
```

## Null Handling

**Important**: Most rules pass validation for null values. Use `required()` or `notNull()` explicitly if the value must be present.

```java
// This passes for null
Rules.minLength(5).validate("name", null, context);  // ✓

// This fails for null
Rules.required()
    .and(Rules.minLength(5))
    .validate("name", null, context);  // ✗ Value is required
```

## Best Practices

1. **Use specific rules**: Prefer `port()` over `integerBetween(1, 65535)`
2. **Compose simple rules**: Build complex validation from simple, reusable rules
3. **Order matters**: Put most restrictive rules first for early failure
4. **Use category classes**: Better IDE autocomplete with `StringRules`, `NumericRules`, etc.
5. **Be explicit about null**: Add `required()` if null is not allowed
6. **Context-aware**: Use `.onlyIf()` for environment-specific validation

## See Also

- [Core Concepts](core-concepts.md) - Property definitions and validation pipeline
- [Conditional Defaults](conditional-defaults.md) - Dynamic default values
- [Dependency-Aware Validation](dependency-aware-validation.md) - Cross-property validation order
