package com.cleanconfig.spring.boot.autoconfigure;

import com.cleanconfig.core.PropertyCategory;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.validation.Rules;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CleanConfigAutoConfiguration}.
 */
class CleanConfigAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CleanConfigAutoConfiguration.class));

    @Test
    void autoConfigurationIsEnabled() {
        contextRunner
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(PropertyRegistry.class);
                    assertThat(context).hasSingleBean(PropertyValidator.class);
                });
    }

    @Test
    void autoConfigurationCanBeDisabled() {
        contextRunner
                .withPropertyValues("cleanconfig.enabled=false")
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PropertyRegistry.class);
                    assertThat(context).doesNotHaveBean(PropertyValidator.class);
                });
    }

    @Test
    void propertyRegistryIsCreatedFromBeans() {
        contextRunner
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    PropertyRegistry registry = context.getBean(PropertyRegistry.class);
                    assertThat(registry.getAllProperties()).hasSize(2);
                    assertThat(registry.getProperty("server.port")).isPresent();
                    assertThat(registry.getProperty("server.host")).isPresent();
                });
    }

    @Test
    void validationPassesWithValidProperties() {
        contextRunner
                .withPropertyValues(
                        "server.port=8080",
                        "server.host=localhost"
                )
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    // Should not throw exception
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    void validationFailsWithInvalidProperties() {
        contextRunner
                .withPropertyValues(
                        "server.port=99999",  // Invalid port
                        "server.host=localhost"
                )
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("CleanConfig validation failed");
                });
    }

    @Test
    void validationCanBeConfiguredToLogWarnings() {
        contextRunner
                .withPropertyValues(
                        "server.port=99999",  // Invalid port
                        "server.host=localhost",
                        "cleanconfig.validation.fail-on-error=false"
                )
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    // Should not fail, just log warnings
                    assertThat(context).hasNotFailed();
                });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public PropertyDefinition<Integer> serverPort() {
            return PropertyDefinition.builder(Integer.class)
                    .name("server.port")
                    .defaultValue(8080)
                    .validationRule(Rules.port())
                    .category(PropertyCategory.GENERAL)
                    .build();
        }

        @Bean
        public PropertyDefinition<String> serverHost() {
            return PropertyDefinition.builder(String.class)
                    .name("server.host")
                    .defaultValue("localhost")
                    .validationRule(Rules.notBlank())
                    .category(PropertyCategory.GENERAL)
                    .build();
        }
    }
}
