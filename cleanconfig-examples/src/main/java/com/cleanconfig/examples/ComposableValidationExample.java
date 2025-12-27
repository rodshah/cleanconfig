package com.cleanconfig.examples;

import com.cleanconfig.core.PropertyDefinition;
import com.cleanconfig.core.PropertyRegistry;
import com.cleanconfig.core.PropertyValidator;
import com.cleanconfig.core.impl.DefaultPropertyValidator;
import com.cleanconfig.core.validation.Rules;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating composable validation rules.
 *
 * <p>This example shows how simple validation rules can be composed
 * into complex validation logic using and(), or(), and onlyIf().
 */
public class ComposableValidationExample {

    public static void main(String[] args) {
        System.out.println("=== Composable Validation Example ===\n");

        // Example 1: Email validation with domain restriction
        System.out.println("Example 1: Composable email validation");
        ValidationRule<String> corporateEmailRule = Rules.notBlank()
                .and(Rules.email())
                .and(Rules.endsWith("@company.com"));

        PropertyDefinition<String> userEmail = PropertyDefinition.builder(String.class)
                .name("user.email")
                .description("Corporate email address")
                .validationRule(corporateEmailRule)
                .required(true)
                .build();

        PropertyRegistry registry1 = PropertyRegistry.builder()
                .register(userEmail)
                .build();

        PropertyValidator validator1 = new DefaultPropertyValidator(registry1);

        // Valid corporate email
        Map<String, String> validEmail = new HashMap<>();
        validEmail.put("user.email", "john.doe@company.com");
        ValidationResult result1 = validator1.validate(validEmail);
        System.out.println("Valid email: " + result1.isValid());

        // Invalid domain
        Map<String, String> invalidDomain = new HashMap<>();
        invalidDomain.put("user.email", "john.doe@gmail.com");
        ValidationResult result2 = validator1.validate(invalidDomain);
        System.out.println("Invalid domain: " + result2.isValid());
        if (!result2.isValid()) {
            result2.getErrors().forEach(error ->
                System.out.println("  Error: " + error.getErrorMessage())
            );
        }

        System.out.println();

        // Example 2: String length with alphanumeric constraint
        System.out.println("Example 2: Composed string rules");
        ValidationRule<String> usernameRule = Rules.notBlank()
                .and(Rules.minLength(3))
                .and(Rules.maxLength(20))
                .and(Rules.alphanumeric());

        PropertyDefinition<String> username = PropertyDefinition.builder(String.class)
                .name("username")
                .description("Alphanumeric username, 3-20 characters")
                .validationRule(usernameRule)
                .required(true)
                .build();

        PropertyRegistry registry2 = PropertyRegistry.builder()
                .register(username)
                .build();

        PropertyValidator validator2 = new DefaultPropertyValidator(registry2);

        // Valid username
        Map<String, String> validUsername = Map.of("username", "user123");
        ValidationResult result3 = validator2.validate(validUsername);
        System.out.println("Valid username 'user123': " + result3.isValid());

        // Invalid: contains special characters
        Map<String, String> invalidChars = Map.of("username", "user@123");
        ValidationResult result4 = validator2.validate(invalidChars);
        System.out.println("Invalid username 'user@123': " + result4.isValid());

        // Invalid: too short
        Map<String, String> tooShort = Map.of("username", "ab");
        ValidationResult result5 = validator2.validate(tooShort);
        System.out.println("Invalid username 'ab': " + result5.isValid());

        System.out.println();

        // Example 3: Multiple validation paths with OR
        System.out.println("Example 3: OR composition");
        ValidationRule<Integer> portOrZeroRule = Rules.port().or(Rules.oneOf(0));

        PropertyDefinition<Integer> serverPort = PropertyDefinition.builder(Integer.class)
                .name("server.port")
                .description("Server port or 0 for random")
                .validationRule(portOrZeroRule)
                .build();

        PropertyRegistry registry3 = PropertyRegistry.builder()
                .register(serverPort)
                .build();

        PropertyValidator validator3 = new DefaultPropertyValidator(registry3);

        // Valid: port 0 (random)
        Map<String, String> randomPort = Map.of("server.port", "0");
        ValidationResult result6 = validator3.validate(randomPort);
        System.out.println("Port 0 (random): " + result6.isValid());

        // Valid: standard port
        Map<String, String> standardPort = Map.of("server.port", "8080");
        ValidationResult result7 = validator3.validate(standardPort);
        System.out.println("Port 8080: " + result7.isValid());

        // Invalid: negative port
        Map<String, String> negativePort = Map.of("server.port", "-1");
        ValidationResult result8 = validator3.validate(negativePort);
        System.out.println("Port -1: " + result8.isValid());

        System.out.println("\n=== Composable rules provide flexible validation! ===");
    }
}
