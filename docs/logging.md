# Logging in CleanConfig

CleanConfig provides a flexible, zero-dependency logging abstraction.

## Quick Start

```java
import com.cleanconfig.core.logging.Logger;
import com.cleanconfig.core.logging.LoggerFactory;

public class MyClass {
    private static final Logger log = LoggerFactory.getLogger(MyClass.class);

    public void process() {
        log.info("Processing started");
        log.debug("Property: name={}, value={}", name, value);
        log.error("Failed", exception);
    }
}
```

## Auto-Detection

CleanConfig automatically detects and uses logging frameworks in this order:

1. **SLF4J** (if on classpath)
2. **JUL** (java.util.logging, always available)
3. **No-Op** (silent)

## Using with SLF4J

Add SLF4J to your dependencies:

```groovy
dependencies {
    implementation 'org.slf4j:slf4j-api:1.7.36'
    implementation 'ch.qos.logback:logback-classic:1.2.11'
}
```

CleanConfig will automatically use SLF4J.

## Using with JUL

No additional dependencies needed. Configure via `logging.properties`:

```properties
.level=INFO
com.cleanconfig.level=DEBUG
handlers=java.util.logging.ConsoleHandler
```

## Custom Provider

```java
LoggerFactory.setLoggerProvider(new CustomLoggerProvider());
```

## Silent Logging

```java
import com.cleanconfig.core.logging.impl.NoOpLoggerProvider;

LoggerFactory.setLoggerProvider(new NoOpLoggerProvider());
```

## Log Levels

- `debug()` - Detailed diagnostic info
- `info()` - General informational messages
- `warn()` - Warnings and deprecated usage
- `error()` - Error events

## Best Practices

1. Use static loggers: `private static final Logger log = ...`
2. Use parameterized messages: `log.debug("Value: {}", value)`
3. Check levels for expensive operations:
   ```java
   if (log.isDebugEnabled()) {
       log.debug("Expensive: {}", computeExpensiveInfo());
   }
   ```
4. Never log sensitive data (passwords, tokens, etc.)

## Thread Safety

All logging components are thread-safe.
