package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.converter.TypeConverterRegistry;
import com.cleanconfig.core.impl.DefaultPropertyContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for composite rules in {@link Rules}.
 */
public class CompositeRulesTest {

    private PropertyContext context;

    @Before
    public void setUp() {
        context = new DefaultPropertyContext(
                Collections.emptyMap(),
                ValidationContextType.STARTUP,
                TypeConverterRegistry.getInstance()
        );
    }

    // ==================== allOf() Tests ====================

    @Test
    public void allOf_AllRulesPass_ReturnsSuccess() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.minLength(3),
                Rules.maxLength(10)
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void allOf_FirstRuleFails_ReturnsFailure() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.minLength(3),
                Rules.maxLength(10)
        );

        ValidationResult result = rule.validate("test.property", "   ", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void allOf_MiddleRuleFails_ReturnsFailure() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.minLength(10),
                Rules.maxLength(20)
        );

        ValidationResult result = rule.validate("test.property", "short", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void allOf_LastRuleFails_ReturnsFailure() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.minLength(3),
                Rules.maxLength(5)
        );

        ValidationResult result = rule.validate("test.property", "too long value", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    public void allOf_ShortCircuits_OnFirstFailure() {
        // Create a rule that would throw if executed
        ValidationRule<String> shouldNotExecute = (name, value, ctx) -> {
            throw new RuntimeException("This rule should not execute");
        };

        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(), // This will fail
                shouldNotExecute   // This should not execute due to short-circuit
        );

        ValidationResult result = rule.validate("test.property", "   ", context);

        assertThat(result.isValid()).isFalse();
        // No exception thrown - confirms short-circuit
    }

    @Test
    public void allOf_SingleRule_Works() {
        ValidationRule<String> rule = Rules.allOf(Rules.notBlank());

        ValidationResult validResult = rule.validate("test.property", "value", context);
        assertThat(validResult.isValid()).isTrue();

        ValidationResult invalidResult = rule.validate("test.property", "   ", context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    @Test
    public void allOf_NoRules_ThrowsException() {
        assertThatThrownBy(() -> Rules.allOf())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one rule is required");
    }

    @Test
    public void allOf_NullRulesArray_ThrowsException() {
        assertThatThrownBy(() -> Rules.allOf((ValidationRule<String>[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one rule is required");
    }

    @Test
    public void allOf_NumericRules_Works() {
        ValidationRule<Integer> rule = Rules.allOf(
                Rules.positive(),
                Rules.min(10),
                Rules.max(100)
        );

        ValidationResult validResult = rule.validate("test.property", 50, context);
        assertThat(validResult.isValid()).isTrue();

        ValidationResult invalidResult = rule.validate("test.property", -5, context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    @Test
    public void allOf_ComplexNesting_Works() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.allOf(
                        Rules.minLength(3),
                        Rules.maxLength(10)
                ),
                Rules.alphanumeric()
        );

        ValidationResult validResult = rule.validate("test.property", "abc123", context);
        assertThat(validResult.isValid()).isTrue();

        ValidationResult invalidResult = rule.validate("test.property", "ab", context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    // ==================== anyOf() Tests ====================

    @Test
    public void anyOf_AllRulesPass_ReturnsSuccess() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.notBlank(),
                Rules.minLength(3)
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void anyOf_FirstRulePasses_ReturnsSuccess() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.notBlank(),
                Rules.minLength(100) // This would fail
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void anyOf_LastRulePasses_ReturnsSuccess() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.minLength(100), // This fails
                Rules.notBlank()      // This passes
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void anyOf_AllRulesFail_ReturnsFailure() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.minLength(100),
                Rules.maxLength(2)
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(2); // All errors collected
    }

    @Test
    public void anyOf_CollectsAllErrors_WhenAllFail() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.minLength(100),
                Rules.maxLength(2),
                Rules.startsWith("xyz")
        );

        ValidationResult result = rule.validate("test.property", "hello", context);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(3); // All 3 errors collected
    }

    @Test
    public void anyOf_SingleRule_Works() {
        ValidationRule<String> rule = Rules.anyOf(Rules.notBlank());

        ValidationResult validResult = rule.validate("test.property", "value", context);
        assertThat(validResult.isValid()).isTrue();

        ValidationResult invalidResult = rule.validate("test.property", "   ", context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    @Test
    public void anyOf_NoRules_ThrowsException() {
        assertThatThrownBy(() -> Rules.anyOf())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one rule is required");
    }

    @Test
    public void anyOf_NullRulesArray_ThrowsException() {
        assertThatThrownBy(() -> Rules.anyOf((ValidationRule<String>[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one rule is required");
    }

    @Test
    public void anyOf_NumericRules_Works() {
        ValidationRule<Integer> rule = Rules.anyOf(
                Rules.between(1, 10),
                Rules.between(90, 100)
        );

        ValidationResult validResult1 = rule.validate("test.property", 5, context);
        assertThat(validResult1.isValid()).isTrue();

        ValidationResult validResult2 = rule.validate("test.property", 95, context);
        assertThat(validResult2.isValid()).isTrue();

        ValidationResult invalidResult = rule.validate("test.property", 50, context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    @Test
    public void anyOf_ComplexNesting_Works() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.allOf(
                        Rules.notBlank(),
                        Rules.minLength(3),
                        Rules.maxLength(5)
                ),
                Rules.allOf(
                        Rules.email(),
                        Rules.endsWith("@company.com")
                )
        );

        // Matches first allOf
        ValidationResult validResult1 = rule.validate("test.property", "abc", context);
        assertThat(validResult1.isValid()).isTrue();

        // Matches second allOf
        ValidationResult validResult2 = rule.validate("test.property", "user@company.com", context);
        assertThat(validResult2.isValid()).isTrue();

        // Matches neither
        ValidationResult invalidResult = rule.validate("test.property", "invalid", context);
        assertThat(invalidResult.isValid()).isFalse();
    }

    // ==================== Combined allOf() and anyOf() Tests ====================

    @Test
    public void combined_AllOfInsideAnyOf_Works() {
        ValidationRule<String> rule = Rules.anyOf(
                Rules.allOf(Rules.notBlank(), Rules.maxLength(5)),
                Rules.allOf(Rules.notBlank(), Rules.minLength(10))
        );

        // Short string - matches first allOf
        ValidationResult shortValid = rule.validate("test.property", "abc", context);
        assertThat(shortValid.isValid()).isTrue();

        // Long string - matches second allOf
        ValidationResult longValid = rule.validate("test.property", "very long string", context);
        assertThat(longValid.isValid()).isTrue();

        // Medium string - matches neither
        ValidationResult invalid = rule.validate("test.property", "medium", context);
        assertThat(invalid.isValid()).isFalse();
    }

    @Test
    public void combined_AnyOfInsideAllOf_Works() {
        ValidationRule<String> rule = Rules.allOf(
                Rules.notBlank(),
                Rules.anyOf(
                        Rules.startsWith("user"),
                        Rules.startsWith("admin")
                )
        );

        ValidationResult validUser = rule.validate("test.property", "user123", context);
        assertThat(validUser.isValid()).isTrue();

        ValidationResult validAdmin = rule.validate("test.property", "admin456", context);
        assertThat(validAdmin.isValid()).isTrue();

        ValidationResult invalid = rule.validate("test.property", "guest789", context);
        assertThat(invalid.isValid()).isFalse();
    }

    @Test
    public void realWorld_EmailValidation_Works() {
        ValidationRule<String> strictEmailRule = Rules.allOf(
                Rules.notBlank(),
                Rules.email(),
                Rules.anyOf(
                        Rules.endsWith("@company.com"),
                        Rules.endsWith("@partner.com")
                )
        );

        ValidationResult validCompany = strictEmailRule.validate("email", "user@company.com", context);
        assertThat(validCompany.isValid()).isTrue();

        ValidationResult validPartner = strictEmailRule.validate("email", "user@partner.com", context);
        assertThat(validPartner.isValid()).isTrue();

        ValidationResult invalidDomain = strictEmailRule.validate("email", "user@other.com", context);
        assertThat(invalidDomain.isValid()).isFalse();

        ValidationResult notEmail = strictEmailRule.validate("email", "notanemail", context);
        assertThat(notEmail.isValid()).isFalse();
    }

    @Test
    public void realWorld_PortValidation_Works() {
        ValidationRule<Integer> portRule = Rules.anyOf(
                Rules.allOf(Rules.integerBetween(80, 80)), // HTTP
                Rules.allOf(Rules.integerBetween(443, 443)), // HTTPS
                Rules.allOf(Rules.integerBetween(8000, 9000)) // Custom range
        );

        ValidationResult validHttp = portRule.validate("port", 80, context);
        assertThat(validHttp.isValid()).isTrue();

        ValidationResult validHttps = portRule.validate("port", 443, context);
        assertThat(validHttps.isValid()).isTrue();

        ValidationResult validCustom = portRule.validate("port", 8080, context);
        assertThat(validCustom.isValid()).isTrue();

        ValidationResult invalid = portRule.validate("port", 3000, context);
        assertThat(invalid.isValid()).isFalse();
    }
}
