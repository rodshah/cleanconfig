# CleanConfig Spring Boot Example

This example demonstrates how to use CleanConfig with Spring Boot to validate application properties.

## Features Demonstrated

- **Property Schema Definition**: Define property schemas with validation rules as Spring beans
- **Multiple Validation Rules**: Chain multiple validation rules using `.and()` and `.or()`
- **Spring Boot Integration**: Automatic validation on application startup
- **Multiple Configuration Sources**: Support for various property sources
- **Fail-Fast Behavior**: Application fails to start if properties are invalid

## How It Works

### 1. Define Property Schemas

In `ApplicationPropertiesConfig.java`, property definitions are created as Spring beans:

```java
@Bean
public PropertyDefinition<String> applicationName() {
    return PropertyDefinition.builder(String.class)
            .name("app.name")
            .description("Name of the application")
            .defaultValue("CleanConfig Example")
            .validationRule(
                    Rules.notBlank()
                            .and(Rules.minLength(3))
                            .and(Rules.maxLength(50))
                            .and(Rules.pattern("^[a-zA-Z0-9 ]+$"))
            )
            .category(PropertyCategory.GENERAL)
            .build();
}
```

### 2. Provide Property Values

Property values can be provided through multiple sources (Spring Boot standard):

#### application.properties
```properties
app.name=My Application
app.admin.email=admin@example.com
```

#### Environment Variables
```bash
export APP_NAME="My Application"
export APP_ADMIN_EMAIL="admin@example.com"
```

#### Command Line Arguments
```bash
java -jar app.jar --app.name="My Application" --app.admin.email=admin@example.com
```

#### External Config (Vault, Consul, Config Server)
```yaml
# Spring Cloud Config
spring:
  cloud:
    vault:
      token: ${VAULT_TOKEN}
      kv:
        enabled: true
```

### 3. Validation Happens Automatically

CleanConfig validates all properties when the Spring application context starts:
- If validation passes → Application starts normally
- If validation fails → Application fails to start with clear error messages

## Running the Example

### Default Profile
```bash
./gradlew :cleanconfig-spring-boot-example:bootRun
```

### Production Profile
```bash
./gradlew :cleanconfig-spring-boot-example:bootRun --args='--spring.profiles.active=prod'
```

### With Custom Properties
```bash
./gradlew :cleanconfig-spring-boot-example:bootRun --args='--app.name="Custom Name" --server.port=9090'
```

### Test Validation Failure
Try providing an invalid value to see validation in action:
```bash
./gradlew :cleanconfig-spring-boot-example:bootRun --args='--server.port=99999'
```

This will fail with a validation error because 99999 is not a valid port number.

## Testing the API

Once the application is running, test the endpoints:

### Get Configuration
```bash
curl http://localhost:8080/api/config
```

Response:
```json
{
  "appName": "CleanConfig Spring Boot Example",
  "adminEmail": "admin@cleanconfig.example.com",
  "maxConnections": 20,
  "serverPort": "8080",
  "databaseUrl": "jdbc:h2:mem:testdb",
  "apiKey": "prod****fgh"
}
```

### Health Check
```bash
curl http://localhost:8080/api/health
```

Response:
```json
{
  "status": "UP",
  "application": "CleanConfig Spring Boot Example"
}
```

## Configuration Options

### CleanConfig Settings

```properties
# Enable/disable CleanConfig (default: true)
cleanconfig.enabled=true

# Fail application startup on validation errors (default: true)
cleanconfig.validation.fail-on-error=true

# Log validation warnings (default: true)
cleanconfig.validation.log-warnings=true
```

### Disable Validation (Not Recommended)

To disable validation (useful for development):
```properties
cleanconfig.enabled=false
```

Or only log warnings instead of failing:
```properties
cleanconfig.validation.fail-on-error=false
```

## Integration with External Config

### Spring Cloud Config Server
```properties
spring.config.import=optional:configserver:http://config-server:8888
```

### HashiCorp Vault
```properties
spring.cloud.vault.token=${VAULT_TOKEN}
spring.cloud.vault.scheme=https
spring.cloud.vault.host=vault.example.com
spring.cloud.vault.port=8200
```

### Kubernetes ConfigMaps
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  application.properties: |
    app.name=Production App
    app.admin.email=admin@prod.example.com
```

## Key Benefits

1. **Type Safety**: Properties are strongly typed (Integer, String, Boolean, etc.)
2. **Validation**: Comprehensive validation rules ensure data quality
3. **Fail-Fast**: Invalid configuration is caught at startup, not at runtime
4. **Documentation**: Property definitions serve as living documentation
5. **Standard Spring**: Works with all Spring Boot property sources
6. **Zero Boilerplate**: No need to write validation code manually
