package com.cleanconfig.core.impl;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultPropertyValidator}.
 */
public class DefaultPropertyValidatorTest {

    @Test
    public void constructor_NullRegistry_ThrowsException() {
        assertThatThrownBy(() -> new DefaultPropertyValidator(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property registry cannot be null");
    }

    @Test
    public void constructor_NullConverterRegistry_ThrowsException() {
        PropertyRegistry registry = PropertyRegistry.builder().build();

        assertThatThrownBy(() -> new DefaultPropertyValidator(registry, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Converter registry cannot be null");
    }

    @Test
    public void validate_NullProperties_ThrowsException() {
        PropertyRegistry registry = PropertyRegistry.builder().build();
        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);

        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Properties cannot be null");
    }

    @Test
    public void validate_NullContextType_ThrowsException() {
        PropertyRegistry registry = PropertyRegistry.builder().build();
        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        assertThatThrownBy(() -> validator.validate(properties, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Context type cannot be null");
    }

    @Test
    public void validate_EmptyRegistry_EmptyProperties_ReturnsSuccess() {
        PropertyRegistry registry = PropertyRegistry.builder().build();
        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void validate_SimpleProperty_ValidValue_ReturnsSuccess() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("test.property")
                .validationRule(Rules.notBlank())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "valid value");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_SimpleProperty_InvalidValue_ReturnsFailure() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("test.property")
                .validationRule(Rules.notBlank())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "   ");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("test.property");
    }

    @Test
    public void validate_RequiredProperty_Missing_ReturnsFailure() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("required.property")
                .required(true)
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("required.property");
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("Required property is missing");
    }

    @Test
    public void validate_RequiredProperty_EmptyString_ReturnsFailure() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("required.property")
                .required(true)
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("required.property", "");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void validate_OptionalProperty_Missing_ReturnsSuccess() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("optional.property")
                .required(false)
                .validationRule(Rules.notBlank())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_TypeConversion_ValidValue_ReturnsSuccess() {
        PropertyDefinition<Integer> property = PropertyDefinition.builder(Integer.class)
                .name("int.property")
                .validationRule(Rules.positive())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("int.property", "42");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_TypeConversion_InvalidValue_ReturnsFailure() {
        PropertyDefinition<Integer> property = PropertyDefinition.builder(Integer.class)
                .name("int.property")
                .validationRule(Rules.positive())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("int.property", "not a number");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("Type conversion failed");
    }

    @Test
    public void validate_DependencyChain_ValidatesInCorrectOrder() {
        // Create a dependency chain: A -> B -> C
        PropertyDefinition<String> propC = PropertyDefinition.builder(String.class)
                .name("propC")
                .validationRule(Rules.notBlank())
                .build();

        PropertyDefinition<String> propB = PropertyDefinition.builder(String.class)
                .name("propB")
                .dependsOnForValidation("propC")
                .validationRule(Rules.notBlank())
                .build();

        PropertyDefinition<String> propA = PropertyDefinition.builder(String.class)
                .name("propA")
                .dependsOnForValidation("propB")
                .validationRule(Rules.notBlank())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(propA)
                .register(propB)
                .register(propC)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("propA", "valueA");
        properties.put("propB", "valueB");
        properties.put("propC", "valueC");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_DependentProperty_CanAccessDependency() {
        PropertyDefinition<Integer> baseProperty = PropertyDefinition.builder(Integer.class)
                .name("base.value")
                .validationRule(Rules.positive())
                .build();

        // Custom rule that depends on base.value
        ValidationRule<Integer> dependentRule = (propertyName, value, context) -> {
            Integer baseValue = context.getTypedProperty("base.value", Integer.class).orElse(0);
            if (value <= baseValue) {
                return ValidationResult.failure(
                        com.cleanconfig.core.validation.ValidationError.builder()
                                .propertyName(propertyName)
                                .actualValue(String.valueOf(value))
                                .errorMessage("Must be greater than base.value")
                                .build()
                );
            }
            return ValidationResult.success();
        };

        PropertyDefinition<Integer> dependentProperty = PropertyDefinition.builder(Integer.class)
                .name("dependent.value")
                .dependsOnForValidation("base.value")
                .validationRule(dependentRule)
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(baseProperty)
                .register(dependentProperty)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);

        // Test valid case: dependent > base
        Map<String, String> validProperties = new HashMap<>();
        validProperties.put("base.value", "10");
        validProperties.put("dependent.value", "20");

        ValidationResult result = validator.validate(validProperties);
        assertThat(result.isValid()).isTrue();

        // Test invalid case: dependent <= base
        Map<String, String> invalidProperties = new HashMap<>();
        invalidProperties.put("base.value", "10");
        invalidProperties.put("dependent.value", "5");

        result = validator.validate(invalidProperties);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void validate_MultipleErrors_CollectsAll() {
        PropertyDefinition<String> prop1 = PropertyDefinition.builder(String.class)
                .name("prop1")
                .required(true)
                .build();

        PropertyDefinition<Integer> prop2 = PropertyDefinition.builder(Integer.class)
                .name("prop2")
                .validationRule(Rules.positive())
                .build();

        PropertyDefinition<String> prop3 = PropertyDefinition.builder(String.class)
                .name("prop3")
                .validationRule(Rules.email())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(prop1)
                .register(prop2)
                .register(prop3)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        // prop1 is missing (required)
        properties.put("prop2", "-5"); // invalid (not positive)
        properties.put("prop3", "not-an-email"); // invalid (not email)

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3);
    }

    @Test
    public void validate_UnknownProperty_ReturnsFailure() {
        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("known.property")
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("known.property", "value");
        properties.put("unknown.property", "value");

        ValidationResult result = validator.validate(properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getPropertyName()).isEqualTo("unknown.property");
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("Unknown property");
    }

    @Test
    public void validateProperty_NullPropertyName_ThrowsException() {
        PropertyRegistry registry = PropertyRegistry.builder().build();
        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        assertThatThrownBy(() -> validator.validateProperty(null, "value", properties))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Property name cannot be null");
    }

    @Test
    public void validateProperty_NullProperties_ThrowsException() {
        PropertyRegistry registry = PropertyRegistry.builder().build();
        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);

        assertThatThrownBy(() -> validator.validateProperty("prop", "value", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Properties cannot be null");
    }

    @Test
    public void validateProperty_ValidValue_ReturnsSuccess() {
        PropertyDefinition<Integer> property = PropertyDefinition.builder(Integer.class)
                .name("test.property")
                .validationRule(Rules.positive())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "42");

        ValidationResult result = validator.validateProperty("test.property", "100", properties);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validateProperty_InvalidValue_ReturnsFailure() {
        PropertyDefinition<Integer> property = PropertyDefinition.builder(Integer.class)
                .name("test.property")
                .validationRule(Rules.positive())
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();

        ValidationResult result = validator.validateProperty("test.property", "-5", properties);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void validate_ContextTypeStartup_UsesCorrectContext() {
        ValidationRule<String> contextAwareRule = new ValidationRule<String>() {
            @Override
            public ValidationResult validate(String propertyName, String value, PropertyContext context) {
                assertThat(context.getContextType()).isEqualTo(ValidationContextType.STARTUP);
                return ValidationResult.success();
            }
        };

        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("test.property")
                .validationRule(contextAwareRule)
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "value");

        ValidationResult result = validator.validate(properties, ValidationContextType.STARTUP);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void validate_ContextTypeRuntimeOverride_UsesCorrectContext() {
        ValidationRule<String> contextAwareRule = new ValidationRule<String>() {
            @Override
            public ValidationResult validate(String propertyName, String value, PropertyContext context) {
                assertThat(context.getContextType()).isEqualTo(ValidationContextType.RUNTIME_OVERRIDE);
                return ValidationResult.success();
            }
        };

        PropertyDefinition<String> property = PropertyDefinition.builder(String.class)
                .name("test.property")
                .validationRule(contextAwareRule)
                .build();

        PropertyRegistry registry = PropertyRegistry.builder()
                .register(property)
                .build();

        DefaultPropertyValidator validator = new DefaultPropertyValidator(registry);
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "value");

        ValidationResult result = validator.validate(properties, ValidationContextType.RUNTIME_OVERRIDE);

        assertThat(result.isValid()).isTrue();
    }
}
