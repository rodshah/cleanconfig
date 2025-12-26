package com.cleanconfig.core;

import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertyValidator} interface.
 */
public class PropertyValidatorTest {

    private PropertyRegistry registry;
    private PropertyValidator validator;

    @Before
    public void setUp() {
        PropertyDefinition<String> nameProperty = PropertyDefinition.builder(String.class)
                .name("user.name")
                .required(true)
                .validationRule(Rules.notBlank())
                .build();

        PropertyDefinition<Integer> ageProperty = PropertyDefinition.builder(Integer.class)
                .name("user.age")
                .validationRule(Rules.positive())
                .build();

        registry = PropertyRegistry.builder()
                .register(nameProperty)
                .register(ageProperty)
                .build();

        validator = new DefaultPropertyValidator(registry);
    }

    @Test
    public void validate_ValidProperties_ReturnsSuccess() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.name", "John Doe");
        properties.put("user.age", "30");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void validate_MissingRequiredProperty_ReturnsFailure() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.age", "30");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("user.name");
    }

    @Test
    public void validate_InvalidPropertyValue_ReturnsFailure() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.name", "John Doe");
        properties.put("user.age", "-5");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("user.age");
    }

    @Test
    public void validate_EmptyProperties_ValidatesRequiredOnly() {
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("user.name");
    }

    @Test
    public void validateProperty_ValidProperty_ReturnsSuccess() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.name", "John Doe");

        ValidationResult result = validator.validateProperty("user.name", "Jane Doe", properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validateProperty_InvalidProperty_ReturnsFailure() {
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validateProperty("user.age", "-5", properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void validateProperty_UnknownProperty_ReturnsFailure() {
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validateProperty("unknown.property", "value", properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("Unknown property");
    }

    @Test
    public void validate_WithContextType_ReturnsSuccess() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.name", "John Doe");
        properties.put("user.age", "30");

        ValidationResult result = validator.validate(properties, ValidationContextType.STARTUP);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_WithDifferentContextType_ReturnsSuccess() {
        Map<String, String> properties = new HashMap<>();
        properties.put("user.name", "John Doe");
        properties.put("user.age", "30");

        ValidationResult result = validator.validate(properties, ValidationContextType.RUNTIME_OVERRIDE);

        assertThat(result.isValid()).isTrue();
    }
}
