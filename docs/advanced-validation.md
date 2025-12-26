# Advanced Validation

This guide covers multi-property validation rules and property grouping in CleanConfig.

## Overview

Multi-property validation rules validate relationships between multiple properties. Common scenarios include:
- Numeric relationships (min < max)
- Mutual exclusivity (only one authentication method)
- Conditional requirements (if SSL enabled, require certificate path)
- Resource constraints (request ≤ limit)

## Core Interface

```java
@FunctionalInterface
public interface MultiPropertyValidationRule {
    ValidationResult validate(String[] propertyNames, PropertyContext context);
}
```

Rules can be composed using `.and()`, `.or()`, and `.onlyIf()` methods.

## Rule Types

### 1. Numeric Relationship Rules

Validate comparisons between numeric properties.

```java
PropertyDefinition minDef = PropertyDefinition.builder(Integer.class)
    .name("pool.min")
    .addRule(NumericRelationshipRules.lessThanOrEqual("pool.min", "pool.max", Integer.class))
    .build();

PropertyDefinition maxDef = PropertyDefinition.builder(Integer.class)
    .name("pool.max")
    .build();
```

Available rules:
- `lessThan(property1, property2, type)`
- `lessThanOrEqual(property1, property2, type)`
- `greaterThan(property1, property2, type)`
- `greaterThanOrEqual(property1, property2, type)`

### 2. Exclusivity Rules

Ensure properties are mutually exclusive or require at least one.

```java
PropertyDefinition passwordDef = PropertyDefinition.builder(String.class)
    .name("auth.password")
    .addRule(ExclusivityRules.exactlyOneRequired("auth.password", "auth.apiKey", "auth.certificate"))
    .build();
```

Available rules:
- `mutuallyExclusive(properties...)` - At most one can be set
- `atLeastOneRequired(properties...)` - At least one must be set
- `exactlyOneRequired(properties...)` - Exactly one must be set

### 3. Conditional Requirement Rules

Model dependencies between properties.

```java
PropertyDefinition sslEnabledDef = PropertyDefinition.builder(Boolean.class)
    .name("ssl.enabled")
    .addRule(ConditionalRequirementRules.ifThen("ssl.enabled", "ssl.certPath"))
    .build();
```

Available rules:
- `ifThen(ifProperty, thenProperty)` - If first is set, second is required
- `allOrNothing(properties...)` - All must be set together, or none at all

### 4. Resource Constraint Rules

Convenience wrappers for Kubernetes-style resource constraints.

```java
PropertyDefinition cpuRequestDef = PropertyDefinition.builder(Integer.class)
    .name("cpu.request")
    .addRule(ResourceConstraintRules.cpuRequestLimit("cpu.request", "cpu.limit"))
    .build();
```

Available rules:
- `cpuRequestLimit(requestProperty, limitProperty)` - request ≤ limit
- `memoryRequestLimit(requestProperty, limitProperty)` - request ≤ limit
- `validRange(minProperty, valueProperty, maxProperty, type)` - min ≤ value ≤ max

## Property Groups

Group related properties and apply group-level validation rules.

```java
PropertyGroup dbGroup = PropertyGroup.builder("database")
    .addProperties("db.host", "db.port", "db.username", "db.password")
    .addRule(ConditionalRequirementRules.allOrNothing("db.username", "db.password"))
    .description("Database connection configuration")
    .build();

PropertyRegistry registry = PropertyRegistry.builder()
    .register(PropertyDefinition.builder(String.class).name("db.host").build())
    .register(PropertyDefinition.builder(Integer.class).name("db.port").build())
    .register(PropertyDefinition.builder(String.class).name("db.username").build())
    .register(PropertyDefinition.builder(String.class).name("db.password").build())
    .registerGroup(dbGroup)
    .build();
```

Groups are automatically validated when calling `validator.validate(properties)`.

## Rule Composition

Combine rules using logical operators:

```java
MultiPropertyValidationRule rule = NumericRelationshipRules
    .lessThan("min", "max", Integer.class)
    .and(NumericRelationshipRules.greaterThan("max", "zero", Integer.class))
    .onlyIf(ctx -> ctx.getProperty("enabled").orElse("false").equals("true"));
```

## Complex Example

Kubernetes-style resource configuration:

```java
PropertyGroup k8sResources = PropertyGroup.builder("kubernetes-resources")
    .addProperties(
        "resources.cpu.request",
        "resources.cpu.limit",
        "resources.memory.request",
        "resources.memory.limit"
    )
    .addRule(NumericRelationshipRules.lessThanOrEqual(
        "resources.cpu.request", "resources.cpu.limit", Integer.class))
    .addRule(NumericRelationshipRules.lessThanOrEqual(
        "resources.memory.request", "resources.memory.limit", Long.class))
    .description("Kubernetes resource constraints")
    .build();

PropertyRegistry registry = PropertyRegistry.builder()
    .register(PropertyDefinition.builder(Integer.class).name("resources.cpu.request").build())
    .register(PropertyDefinition.builder(Integer.class).name("resources.cpu.limit").build())
    .register(PropertyDefinition.builder(Long.class).name("resources.memory.request").build())
    .register(PropertyDefinition.builder(Long.class).name("resources.memory.limit").build())
    .registerGroup(k8sResources)
    .build();
```

## Best Practices

1. **Use Property Groups** - Organize related properties together for better maintainability
2. **Choose the Right Rule Type** - Use specialized rules (NumericRelationshipRules, ExclusivityRules) instead of custom validation
3. **Add Validation to Property Definitions** - Attach rules to property definitions when they involve that property
4. **Keep Rules Simple** - Break complex validation into multiple simple rules using composition
5. **Add Descriptions** - Document property groups to help users understand their purpose

## Related Documentation

- [Core Concepts](core-concepts.md) - Basic property definitions and validation
- [Type Conversion](type-conversion.md) - Working with typed properties
