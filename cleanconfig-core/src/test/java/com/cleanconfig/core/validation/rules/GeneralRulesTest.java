package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GeneralRules}.
 */
public class GeneralRulesTest {

    private PropertyContext context;

    @Before
    public void setUp() {
        context = new TestPropertyContext();
    }

    // required() tests
    @Test
    public void required_NonNullValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.required();
        assertThat(rule.validate("name", "value", context).isValid()).isTrue();
    }

    @Test
    public void required_NullValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.required();
        ValidationResult result = rule.validate("name", null, context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("is required");
    }

    // notNull() tests
    @Test
    public void notNull_NonNullValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.notNull();
        assertThat(rule.validate("name", "value", context).isValid()).isTrue();
    }

    @Test
    public void notNull_NullValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.notNull();
        assertThat(rule.validate("name", null, context).isValid()).isFalse();
    }

    // oneOf(Collection) tests
    @Test
    public void oneOf_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.oneOf(Arrays.asList("dev", "staging", "prod"));
        assertThat(rule.validate("env", "dev", context).isValid()).isTrue();
        assertThat(rule.validate("env", "staging", context).isValid()).isTrue();
        assertThat(rule.validate("env", "prod", context).isValid()).isTrue();
    }

    @Test
    public void oneOf_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.oneOf(Arrays.asList("dev", "staging", "prod"));
        ValidationResult result = rule.validate("env", "test", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must be one of");
    }

    // oneOf(varargs) tests
    @Test
    public void oneOf_Varargs_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.oneOf("red", "green", "blue");
        assertThat(rule.validate("color", "red", context).isValid()).isTrue();
    }

    @Test
    public void oneOf_Varargs_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.oneOf("red", "green", "blue");
        assertThat(rule.validate("color", "yellow", context).isValid()).isFalse();
    }

    // noneOf(Collection) tests
    @Test
    public void noneOf_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.noneOf(Arrays.asList("admin", "root", "system"));
        assertThat(rule.validate("username", "user123", context).isValid()).isTrue();
    }

    @Test
    public void noneOf_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.noneOf(Arrays.asList("admin", "root", "system"));
        ValidationResult result = rule.validate("username", "admin", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must not be one of");
    }

    // noneOf(varargs) tests
    @Test
    public void noneOf_Varargs_ValidValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.noneOf("bad", "evil", "malicious");
        assertThat(rule.validate("word", "good", context).isValid()).isTrue();
    }

    @Test
    public void noneOf_Varargs_InvalidValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.noneOf("bad", "evil", "malicious");
        assertThat(rule.validate("word", "evil", context).isValid()).isFalse();
    }

    // equalTo() tests
    @Test
    public void equalTo_EqualValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.equalTo("expected");
        assertThat(rule.validate("name", "expected", context).isValid()).isTrue();
    }

    @Test
    public void equalTo_DifferentValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.equalTo("expected");
        ValidationResult result = rule.validate("name", "actual", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must equal: expected");
    }

    @Test
    public void equalTo_NullValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.equalTo("expected");
        ValidationResult result = rule.validate("name", null, context);
        assertThat(result.isValid()).isFalse();
    }

    // notEqualTo() tests
    @Test
    public void notEqualTo_DifferentValue_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.notEqualTo("forbidden");
        assertThat(rule.validate("name", "allowed", context).isValid()).isTrue();
    }

    @Test
    public void notEqualTo_SameValue_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.notEqualTo("forbidden");
        ValidationResult result = rule.validate("name", "forbidden", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must not equal: forbidden");
    }

    // custom() tests
    @Test
    public void custom_PredicatePass_ReturnsSuccess() {
        ValidationRule<String> rule = GeneralRules.custom(
                value -> value.length() > 5,
                "Value must have more than 5 characters"
        );
        assertThat(rule.validate("name", "hello world", context).isValid()).isTrue();
    }

    @Test
    public void custom_PredicateFail_ReturnsFailure() {
        ValidationRule<String> rule = GeneralRules.custom(
                value -> value.length() > 5,
                "Value must have more than 5 characters"
        );
        ValidationResult result = rule.validate("name", "hi", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("more than 5 characters");
    }

    // custom() with expected value tests
    @Test
    public void customWithExpected_PredicateFail_IncludesExpectedValue() {
        ValidationRule<String> rule = GeneralRules.custom(
                value -> value.length() > 5,
                "Value must have more than 5 characters",
                "length > 5"
        );
        ValidationResult result = rule.validate("name", "hi", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getExpectedValue()).isEqualTo("length > 5");
    }

    // customWithContext() tests
    @Test
    public void customWithContext_PredicatePass_ReturnsSuccess() {
        TestPropertyContext testContext = new TestPropertyContext();
        testContext.setProperty("enabled", "true");

        ValidationRule<String> rule = GeneralRules.customWithContext(
                (value, ctx) -> ctx.getProperty("enabled").map(Boolean::parseBoolean).orElse(false),
                "Feature must be enabled"
        );
        assertThat(rule.validate("feature", "test", testContext).isValid()).isTrue();
    }

    @Test
    public void customWithContext_PredicateFail_ReturnsFailure() {
        TestPropertyContext testContext = new TestPropertyContext();
        testContext.setProperty("enabled", "false");

        ValidationRule<String> rule = GeneralRules.customWithContext(
                (value, ctx) -> ctx.getProperty("enabled").map(Boolean::parseBoolean).orElse(false),
                "Feature must be enabled"
        );
        ValidationResult result = rule.validate("feature", "test", testContext);
        assertThat(result.isValid()).isFalse();
    }

    // comparableBetween() tests
    @Test
    public void comparableBetween_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = GeneralRules.comparableBetween(1, 100);

        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
        assertThat(rule.validate("value", 1, context).isValid()).isTrue();
        assertThat(rule.validate("value", 100, context).isValid()).isTrue();
    }

    @Test
    public void comparableBetween_OutOfRange_ReturnsFailure() {
        ValidationRule<Integer> rule = GeneralRules.comparableBetween(1, 100);

        assertThat(rule.validate("value", 0, context).isValid()).isFalse();
        assertThat(rule.validate("value", 101, context).isValid()).isFalse();
    }

    // comparableGreaterThan() tests
    @Test
    public void comparableGreaterThan_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = GeneralRules.comparableGreaterThan(10);
        assertThat(rule.validate("value", 11, context).isValid()).isTrue();
        assertThat(rule.validate("value", 100, context).isValid()).isTrue();
    }

    @Test
    public void comparableGreaterThan_EqualOrLess_ReturnsFailure() {
        ValidationRule<Integer> rule = GeneralRules.comparableGreaterThan(10);
        assertThat(rule.validate("value", 10, context).isValid()).isFalse(); // Equal
        assertThat(rule.validate("value", 5, context).isValid()).isFalse(); // Less
    }

    // comparableLessThan() tests
    @Test
    public void comparableLessThan_ValidValue_ReturnsSuccess() {
        ValidationRule<Integer> rule = GeneralRules.comparableLessThan(100);
        assertThat(rule.validate("value", 99, context).isValid()).isTrue();
        assertThat(rule.validate("value", 50, context).isValid()).isTrue();
    }

    @Test
    public void comparableLessThan_EqualOrGreater_ReturnsFailure() {
        ValidationRule<Integer> rule = GeneralRules.comparableLessThan(100);
        assertThat(rule.validate("value", 100, context).isValid()).isFalse(); // Equal
        assertThat(rule.validate("value", 150, context).isValid()).isFalse(); // Greater
    }

    // Test composition
    @Test
    public void composition_MultipleRules_Works() {
        ValidationRule<String> rule = GeneralRules.<String>required()
                .and(GeneralRules.oneOf("dev", "staging", "prod"))
                .and(GeneralRules.notEqualTo("dev")); // Not dev environment

        assertThat(rule.validate("env", "staging", context).isValid()).isTrue();
        assertThat(rule.validate("env", null, context).isValid()).isFalse(); // Required
        assertThat(rule.validate("env", "test", context).isValid()).isFalse(); // Not in oneOf
        assertThat(rule.validate("env", "dev", context).isValid()).isFalse(); // Equals forbidden value
    }

    // Null value tests
    @Test
    public void nullSafeRules_NullValue_ReturnsSuccess() {
        // Rules that should pass for null (except required/notNull)
        assertThat(GeneralRules.oneOf("a", "b").validate("v", null, context).isValid()).isTrue();
        assertThat(GeneralRules.noneOf("a", "b").validate("v", null, context).isValid()).isTrue();
        assertThat(GeneralRules.notEqualTo("forbidden").validate("v", null, context).isValid()).isTrue();
        assertThat(GeneralRules.custom(x -> x.equals("test"), "msg").validate("v", null, context).isValid()).isTrue();
    }

    // Simple test context implementation
    private static class TestPropertyContext implements PropertyContext {
        private final Map<String, String> properties = new HashMap<>();

        public void setProperty(String name, String value) {
            properties.put(name, value);
        }

        @Override
        public Optional<String> getProperty(String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }

        @Override
        public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
            return Optional.empty();
        }

        @Override
        public Map<String, String> getAllProperties() {
            return Collections.unmodifiableMap(properties);
        }

        @Override
        public ValidationContextType getContextType() {
            return ValidationContextType.STARTUP;
        }

        @Override
        public Optional<String> getMetadata(String key) {
            return Optional.empty();
        }

        @Override
        public boolean hasProperty(String propertyName) {
            return properties.containsKey(propertyName);
        }
    }
}
