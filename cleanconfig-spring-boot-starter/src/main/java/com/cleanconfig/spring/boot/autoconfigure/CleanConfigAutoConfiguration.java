package com.cleanconfig.spring.boot.autoconfigure;

import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyRegistryBuilder;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.format.TextValidationFormatter;
import com.cleanconfig.core.validation.format.ValidationFormatter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration for CleanConfig Spring Boot integration.
 *
 * <p>This auto-configuration:
 * <ul>
 *   <li>Creates a {@link PropertyRegistry} bean from property definitions</li>
 *   <li>Creates a {@link PropertyValidator} bean for validation</li>
 *   <li>Validates properties on application startup</li>
 *   <li>Integrates with Spring's {@link Environment}</li>
 * </ul>
 *
 * <p>To use, add property definitions as beans:
 * <pre>
 * {@code @Bean}
 * public PropertyDefinition&lt;Integer&gt; serverPort() {
 *     return PropertyDefinition.builder(Integer.class)
 *         .name("server.port")
 *         .defaultValue(8080)
 *         .validationRule(Rules.port())
 *         .build();
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(PropertyRegistry.class)
@ConditionalOnProperty(prefix = "cleanconfig", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CleanConfigProperties.class)
public class CleanConfigAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(CleanConfigAutoConfiguration.class);

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final CleanConfigProperties properties;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring-managed beans are safe to store as dependencies via constructor injection"
    )
    public CleanConfigAutoConfiguration(
            ApplicationContext applicationContext,
            Environment environment,
            CleanConfigProperties properties) {
        this.applicationContext = applicationContext;
        this.environment = environment;
        this.properties = properties;
    }

    /**
     * Creates a PropertyRegistry from all PropertyDefinition beans.
     *
     * @return property registry
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PropertyRegistry propertyRegistry() {
        Map<String, PropertyDefinition> definitions =
                (Map) applicationContext.getBeansOfType(PropertyDefinition.class);

        if (definitions.isEmpty()) {
            log.warn("No PropertyDefinition beans found. CleanConfig validation will not be active.");
            return PropertyRegistry.builder().build();
        }

        log.info("Found {} PropertyDefinition beans", definitions.size());

        PropertyRegistryBuilder builder = PropertyRegistry.builder();
        definitions.values().forEach(builder::register);

        return builder.build();
    }

    /**
     * Creates a PropertyValidator for the registry.
     *
     * @param registry property registry
     * @return property validator
     */
    @Bean
    public PropertyValidator propertyValidator(PropertyRegistry registry) {
        return new DefaultPropertyValidator(registry);
    }

    /**
     * Creates a ValidationFormatter for formatting validation results.
     *
     * @return validation formatter
     */
    @Bean
    public ValidationFormatter validationFormatter() {
        return new TextValidationFormatter();
    }

    /**
     * Validates properties on application startup.
     * This method is called when the application context is refreshed.
     * If validation fails and fail-on-error is true, this will prevent the application from starting.
     *
     * @param event the context refreshed event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Get beans from event's application context
        ApplicationContext context = event.getApplicationContext();
        PropertyRegistry registry = context.getBean(PropertyRegistry.class);
        PropertyValidator validator = context.getBean(PropertyValidator.class);
        ValidationFormatter formatter = context.getBean(ValidationFormatter.class);

        // Collect all properties
        Map<String, String> propertyMap = new HashMap<>();
        for (PropertyDefinition<?> def : registry.getAllProperties()) {
            String value = environment.getProperty(def.getName());
            if (value != null) {
                propertyMap.put(def.getName(), value);
            }
        }

        // Validate
        ValidationResult result = validator.validate(propertyMap);

        if (!result.isValid()) {
            String formattedErrors = formatter.format(result);

            if (properties.getValidation().isFailOnError()) {
                throw new IllegalStateException(
                        "CleanConfig validation failed:\n" + formattedErrors);
            } else if (properties.getValidation().isLogWarnings()) {
                log.warn("CleanConfig validation warnings:\n{}", formattedErrors);
            }
        } else {
            log.info("CleanConfig validation passed: {} properties validated", registry.getAllProperties().size());
        }
    }
}
