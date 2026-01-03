package com.cleanconfig.examples.springboot;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.validation.Rules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class demonstrating CleanConfig with Spring Boot.
 *
 * <p><b>How Property Values Are Provided:</b></p>
 * <p>These definitions specify validation rules. Actual values come from Spring Boot's standard sources:
 * <ul>
 *   <li><b>application.properties/yml</b> - Main config file</li>
 *   <li><b>Environment variables</b> - SERVER_PORT=9090</li>
 *   <li><b>Command line</b> - --server.port=9090</li>
 *   <li><b>External sources</b> - Spring Cloud Config, Vault, Consul</li>
 *   <li><b>Profile-specific</b> - application-prod.properties</li>
 * </ul>
 *
 * <p><b>Multiple Validation Rules:</b></p>
 * <p>Use {@code .and()} and {@code .or()} to compose validation rules:
 * <pre>
 * Rules.notBlank().and(Rules.minLength(3)).and(Rules.maxLength(50))
 * </pre>
 */
@Configuration
public class ApplicationPropertiesConfig {

    @Bean
    public PropertyDefinition<Integer> serverPort() {
        return PropertyDefinition.builder(Integer.class)
                .name("server.port")
                .description("Port number for the HTTP server")
                .defaultValue(8080)
                .validationRule(Rules.port())
                .category(PropertyCategory.GENERAL)
                .build();
    }

    /**
     * Example with multiple validation rules using .and()
     */
    @Bean
    public PropertyDefinition<String> applicationName() {
        return PropertyDefinition.builder(String.class)
                .name("app.name")
                .description("Name of the application (3-50 chars, no special chars)")
                .defaultValue("CleanConfig Example")
                .validationRule(
                        Rules.notBlank()
                                .and(Rules.minLength(3))
                                .and(Rules.maxLength(50))
                                .and(Rules.matchesRegex("^[a-zA-Z0-9 ]+$"))
                )
                .category(PropertyCategory.GENERAL)
                .build();
    }

    /**
     * Example with JDBC URL validation
     */
    @Bean
    public PropertyDefinition<String> databaseUrl() {
        return PropertyDefinition.builder(String.class)
                .name("app.database.url")
                .description("JDBC database connection URL")
                .defaultValue("jdbc:h2:mem:testdb")
                .validationRule(
                        Rules.notBlank()
                                .and(Rules.matchesRegex("^jdbc:[a-z0-9]+:.*"))
                )
                .category(PropertyCategory.GENERAL)
                .build();
    }

    /**
     * Example with range and positive number validation
     */
    @Bean
    public PropertyDefinition<Integer> maxConnections() {
        return PropertyDefinition.builder(Integer.class)
                .name("app.database.max-connections")
                .description("Maximum database connections (1-100)")
                .defaultValue(10)
                .validationRule(
                        Rules.integerBetween(1, 100)
                                .and(Rules.positive())
                )
                .category(PropertyCategory.GENERAL)
                .build();
    }

    /**
     * Example with email validation
     */
    @Bean
    public PropertyDefinition<String> adminEmail() {
        return PropertyDefinition.builder(String.class)
                .name("app.admin.email")
                .description("Administrator email address")
                .defaultValue("admin@example.com")
                .validationRule(
                        Rules.notBlank()
                                .and(Rules.email())
                )
                .category(PropertyCategory.GENERAL)
                .build();
    }

    /**
     * Example with complex string validation
     */
    @Bean
    public PropertyDefinition<String> apiKey() {
        return PropertyDefinition.builder(String.class)
                .name("app.api.key")
                .description("API key for external service (32-64 alphanumeric chars)")
                .defaultValue("default-key-12345678901234567890")
                .validationRule(
                        Rules.notBlank()
                                .and(Rules.minLength(32))
                                .and(Rules.maxLength(64))
                                .and(Rules.matchesRegex("^[a-zA-Z0-9-]+$"))
                )
                .category(PropertyCategory.GENERAL)
                .build();
    }
}
