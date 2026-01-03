# Spring Boot Integration

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                Spring Boot Application                      │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │      Your @Configuration Classes                   │    │
│  │                                                     │    │
│  │  @Bean PropertyDefinition<String> appName() {      │    │
│  │    return PropertyDefinition.builder(String.class) │    │
│  │      .name("app.name")                             │    │
│  │      .validationRule(Rules.notBlank())             │    │
│  │      .build();                                     │    │
│  │  }                                                  │    │
│  └──────────────────┬──────────────────────────────────┘    │
│                     │ Beans discovered                      │
│                     ▼                                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │   CleanConfigAutoConfiguration (Spring Boot)       │    │
│  │                                                     │    │
│  │  1. Discovers PropertyDefinition beans             │    │
│  │  2. Builds PropertyRegistry                        │    │
│  │  3. Creates PropertyValidator                      │    │
│  │  4. Listens to ContextRefreshedEvent               │    │
│  └──────────────────┬──────────────────────────────────┘    │
│                     │ Validation triggers on startup        │
│                     ▼                                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │      Validation Process (onApplicationEvent)       │    │
│  │                                                     │    │
│  │  For each PropertyDefinition:                      │    │
│  │    1. Get value from Spring Environment           │    │
│  │    2. Validate against rules                       │    │
│  │    3. Collect errors                               │    │
│  │                                                     │    │
│  │  If invalid & fail-on-error = true:                │    │
│  │    → Throw IllegalStateException                   │    │
│  │    → Application fails to start ❌                  │    │
│  │                                                     │    │
│  │  If invalid & fail-on-error = false:               │    │
│  │    → Log warnings                                   │    │
│  │    → Application starts ✅                          │    │
│  └──────────────────┬──────────────────────────────────┘    │
│                     │ Gets values from                      │
│                     ▼                                       │
│  ┌────────────────────────────────────────────────────┐    │
│  │            Spring Environment                      │    │
│  │                                                     │    │
│  │  Property Source Priority (highest to lowest):     │    │
│  │  1. Command line arguments                         │    │
│  │  2. JVM system properties                          │    │
│  │  3. OS environment variables                       │    │
│  │  4. Profile-specific configs                       │    │
│  │  5. application.properties                         │    │
│  │  6. External config (Vault, Config Server)         │    │
│  └────────────────────────────────────────────────────┘    │
│                                                             │
│  After successful validation:                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │     Your Application Components                    │    │
│  │                                                     │    │
│  │  @RestController, @Service, etc.                   │    │
│  │  Use properties via:                               │    │
│  │    - @Value("${app.name}")                         │    │
│  │    - Environment.getProperty("app.name")           │    │
│  │    - @ConfigurationProperties                      │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Component Flow

### 1. Bean Discovery Phase (Spring Boot Startup)

```
Application Start
     │
     ├──> CleanConfigAutoConfiguration enabled?
     │    (via @ConditionalOnProperty)
     │
     ├──> Scan for PropertyDefinition beans
     │    (via applicationContext.getBeansOfType)
     │
     └──> Register in PropertyRegistry
```

### 2. Validation Phase (Context Refresh Event)

```
ContextRefreshedEvent Fired
     │
     ├──> Get PropertyRegistry, Validator, Formatter beans
     │
     ├──> For each PropertyDefinition:
     │    ├──> Get value from Spring Environment
     │    ├──> Apply validation rules
     │    └──> Collect results
     │
     ├──> All valid?
     │    ├──> YES: Log success → Application continues
     │    └──> NO:  Check fail-on-error setting
     │              ├──> TRUE:  Throw exception → Startup fails
     │              └──> FALSE: Log warnings → Application continues
```

### 3. Property Access (Runtime)

```
Application Component needs property
     │
     ├──> @Value("${prop.name}")
     │    └──> Spring injects from Environment
     │
     ├──> environment.getProperty("prop.name")
     │    └──> Returns String value
     │
     └──> environment.getProperty("prop.name", Integer.class)
          └──> Returns typed value (Spring converts)

Note: CleanConfig validated these during startup,
      so you know they're valid ✅
```

## Key Integration Points

### 1. Auto-Configuration

`CleanConfigAutoConfiguration` implements `ApplicationListener<ContextRefreshedEvent>`:
- Runs after all beans are created
- Before application is fully started
- Exceptions properly fail startup

### 2. Bean Discovery

```java
Map<String, PropertyDefinition> definitions =
    applicationContext.getBeansOfType(PropertyDefinition.class);
```

Automatically finds all `PropertyDefinition` beans in your `@Configuration` classes.

### 3. Spring Environment Integration

```java
String value = environment.getProperty(def.getName());
```

Gets actual property values from Spring's unified Environment abstraction.

### 4. Validation Execution

```java
ValidationResult result = validator.validate(propertyMap);
if (!result.isValid()) {
    throw new IllegalStateException("Validation failed");
}
```

Validates all properties against their defined rules.

## Configuration Flow Examples

### Example 1: Valid Configuration

```
1. Define: serverPort requires valid port (1-65535)
2. Provide: server.port=8080 in application.properties
3. Validate: 8080 is valid port ✅
4. Result: Application starts successfully
```

### Example 2: Invalid Configuration (Fail-Fast)

```
1. Define: serverPort requires valid port (1-65535)
2. Provide: server.port=99999 via command line
3. Validate: 99999 exceeds max port ❌
4. Result: IllegalStateException thrown
           → Application fails to start
           → Error message shows: "Port must be between 1 and 65535"
```

### Example 3: Multiple Sources

```
Priority Order (highest wins):
1. Command line:  --server.port=9090     ← WINS
2. Environment:   SERVER_PORT=8080
3. application.properties: server.port=7070

Validation checks: 9090
Result: Valid port ✅
```

## Quick Start

### 1. Add Dependency
```gradle
implementation 'com.cleanconfig:cleanconfig-spring-boot-starter:0.1.0'
```

### 2. Define Properties
```java
@Configuration
public class AppConfig {
    @Bean
    public PropertyDefinition<String> appName() {
        return PropertyDefinition.builder(String.class)
                .name("app.name")
                .validationRule(
                    Rules.notBlank()
                        .and(Rules.minLength(3))
                )
                .build();
    }
}
```

### 3. Provide Values
```properties
# application.properties
app.name=My Application
```

### 4. Use Properties
```java
@RestController
public class MyController {
    @Value("${app.name}")
    private String appName;
}
```

## See Also

- [Example Application](../cleanconfig-spring-boot-example/README.md)
- [Validation Rules](validation-rules.md)
- [Advanced Validation](advanced-validation.md)
