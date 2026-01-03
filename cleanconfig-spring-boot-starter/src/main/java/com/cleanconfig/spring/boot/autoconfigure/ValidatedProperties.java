package com.cleanconfig.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a configuration class for CleanConfig validation.
 *
 * <p>This annotation combines {@link Component} and {@link ConfigurationProperties}
 * to automatically validate properties against CleanConfig property definitions.
 *
 * <p>Example usage:
 * <pre>
 * {@code @ValidatedProperties}(prefix = "app.server")
 * public class ServerConfig {
 *     private int port = 8080;
 *     private String host = "localhost";
 *
 *     // getters and setters
 * }
 * </pre>
 *
 * <p>The corresponding property definitions in PropertyRegistry:
 * <pre>
 * PropertyRegistry registry = PropertyRegistry.builder()
 *     .register(PropertyDefinition.builder(Integer.class)
 *         .name("app.server.port")
 *         .validationRule(Rules.port())
 *         .build())
 *     .register(PropertyDefinition.builder(String.class)
 *         .name("app.server.host")
 *         .validationRule(Rules.notBlank())
 *         .build())
 *     .build();
 * </pre>
 *
 * @since 1.0.0
 * @see ConfigurationProperties
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@ConfigurationProperties
public @interface ValidatedProperties {

    /**
     * The prefix of the properties that are valid to bind to this object.
     * Synonym for {@link #prefix()}.
     *
     * @return the prefix
     */
    @AliasFor(annotation = ConfigurationProperties.class, attribute = "prefix")
    String value() default "";

    /**
     * The prefix of the properties that are valid to bind to this object.
     * Synonym for {@link #value()}.
     *
     * @return the prefix
     */
    @AliasFor(annotation = ConfigurationProperties.class, attribute = "prefix")
    String prefix() default "";

    /**
     * Whether to ignore invalid fields.
     * Default is false, meaning invalid fields will cause validation to fail.
     *
     * @return whether to ignore invalid fields
     */
    @AliasFor(annotation = ConfigurationProperties.class, attribute = "ignoreInvalidFields")
    boolean ignoreInvalidFields() default false;

    /**
     * Whether to ignore unknown fields.
     * Default is true, allowing forward compatibility.
     *
     * @return whether to ignore unknown fields
     */
    @AliasFor(annotation = ConfigurationProperties.class, attribute = "ignoreUnknownFields")
    boolean ignoreUnknownFields() default true;
}
