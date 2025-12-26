package com.cleanconfig.core.validation;

import com.cleanconfig.core.PropertyContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Conditions}.
 */
public class ConditionsTest {

    private TestPropertyContext context;

    @Before
    public void setUp() {
        context = new TestPropertyContext();
    }

    // propertyEquals() tests
    @Test
    public void propertyEquals_MatchingValue_ReturnsTrue() {
        context.setProperty("ssl.enabled", "true");
        Predicate<PropertyContext> condition = Conditions.propertyEquals("ssl.enabled", "true");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyEquals_DifferentValue_ReturnsFalse() {
        context.setProperty("ssl.enabled", "false");
        Predicate<PropertyContext> condition = Conditions.propertyEquals("ssl.enabled", "true");
        assertThat(condition.test(context)).isFalse();
    }

    @Test
    public void propertyEquals_MissingProperty_ReturnsFalse() {
        Predicate<PropertyContext> condition = Conditions.propertyEquals("missing", "value");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyNotEquals() tests
    @Test
    public void propertyNotEquals_DifferentValue_ReturnsTrue() {
        context.setProperty("mode", "async");
        Predicate<PropertyContext> condition = Conditions.propertyNotEquals("mode", "sync");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyNotEquals_SameValue_ReturnsFalse() {
        context.setProperty("mode", "sync");
        Predicate<PropertyContext> condition = Conditions.propertyNotEquals("mode", "sync");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyIsPresent() tests
    @Test
    public void propertyIsPresent_ExistingProperty_ReturnsTrue() {
        context.setProperty("name", "value");
        Predicate<PropertyContext> condition = Conditions.propertyIsPresent("name");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyIsPresent_MissingProperty_ReturnsFalse() {
        Predicate<PropertyContext> condition = Conditions.propertyIsPresent("missing");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyIsAbsent() tests
    @Test
    public void propertyIsAbsent_MissingProperty_ReturnsTrue() {
        Predicate<PropertyContext> condition = Conditions.propertyIsAbsent("missing");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyIsAbsent_ExistingProperty_ReturnsFalse() {
        context.setProperty("name", "value");
        Predicate<PropertyContext> condition = Conditions.propertyIsAbsent("name");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyIsTrue() tests
    @Test
    public void propertyIsTrue_TrueValues_ReturnsTrue() {
        context.setProperty("flag", "true");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isTrue();

        context.setProperty("flag", "TRUE");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isTrue();

        context.setProperty("flag", "yes");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isTrue();

        context.setProperty("flag", "1");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isTrue();
    }

    @Test
    public void propertyIsTrue_FalseValues_ReturnsFalse() {
        context.setProperty("flag", "false");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isFalse();

        context.setProperty("flag", "no");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isFalse();

        context.setProperty("flag", "0");
        assertThat(Conditions.propertyIsTrue("flag").test(context)).isFalse();
    }

    // propertyIsFalse() tests
    @Test
    public void propertyIsFalse_FalseValues_ReturnsTrue() {
        context.setProperty("flag", "false");
        assertThat(Conditions.propertyIsFalse("flag").test(context)).isTrue();

        context.setProperty("flag", "no");
        assertThat(Conditions.propertyIsFalse("flag").test(context)).isTrue();

        context.setProperty("flag", "0");
        assertThat(Conditions.propertyIsFalse("flag").test(context)).isTrue();
    }

    @Test
    public void propertyIsFalse_TrueValues_ReturnsFalse() {
        context.setProperty("flag", "true");
        assertThat(Conditions.propertyIsFalse("flag").test(context)).isFalse();
    }

    // propertyMatches() tests
    @Test
    public void propertyMatches_PredicatePass_ReturnsTrue() {
        context.setProperty("count", "10");
        Predicate<PropertyContext> condition = Conditions.propertyMatches("count", value -> Integer.parseInt(value) > 5);
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyMatches_PredicateFail_ReturnsFalse() {
        context.setProperty("count", "3");
        Predicate<PropertyContext> condition = Conditions.propertyMatches("count", value -> Integer.parseInt(value) > 5);
        assertThat(condition.test(context)).isFalse();
    }

    // typedPropertyMatches() tests
    @Test
    public void typedPropertyMatches_PredicatePass_ReturnsTrue() {
        context.setTypedProperty("count", 10);
        Predicate<PropertyContext> condition = Conditions.typedPropertyMatches("count", Integer.class, value -> value > 5);
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void typedPropertyMatches_PredicateFail_ReturnsFalse() {
        context.setTypedProperty("count", 3);
        Predicate<PropertyContext> condition = Conditions.typedPropertyMatches("count", Integer.class, value -> value > 5);
        assertThat(condition.test(context)).isFalse();
    }

    // metadataEquals() tests
    @Test
    public void metadataEquals_MatchingValue_ReturnsTrue() {
        context.setMetadata("environment", "production");
        Predicate<PropertyContext> condition = Conditions.metadataEquals("environment", "production");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void metadataEquals_DifferentValue_ReturnsFalse() {
        context.setMetadata("environment", "development");
        Predicate<PropertyContext> condition = Conditions.metadataEquals("environment", "production");
        assertThat(condition.test(context)).isFalse();
    }

    // metadataIsPresent() tests
    @Test
    public void metadataIsPresent_ExistingMetadata_ReturnsTrue() {
        context.setMetadata("key", "value");
        Predicate<PropertyContext> condition = Conditions.metadataIsPresent("key");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void metadataIsPresent_MissingMetadata_ReturnsFalse() {
        Predicate<PropertyContext> condition = Conditions.metadataIsPresent("missing");
        assertThat(condition.test(context)).isFalse();
    }

    // alwaysTrue() tests
    @Test
    public void alwaysTrue_ReturnsTrue() {
        Predicate<PropertyContext> condition = Conditions.alwaysTrue();
        assertThat(condition.test(context)).isTrue();
    }

    // alwaysFalse() tests
    @Test
    public void alwaysFalse_ReturnsFalse() {
        Predicate<PropertyContext> condition = Conditions.alwaysFalse();
        assertThat(condition.test(context)).isFalse();
    }

    // not() tests
    @Test
    public void not_NegatesCondition() {
        context.setProperty("flag", "true");
        Predicate<PropertyContext> condition = Conditions.not(Conditions.propertyIsTrue("flag"));
        assertThat(condition.test(context)).isFalse();
    }

    // and() tests
    @Test
    public void and_AllTrue_ReturnsTrue() {
        context.setProperty("flag1", "true");
        context.setProperty("flag2", "true");
        Predicate<PropertyContext> condition = Conditions.and(
                Conditions.propertyIsTrue("flag1"),
                Conditions.propertyIsTrue("flag2")
        );
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void and_OneFalse_ReturnsFalse() {
        context.setProperty("flag1", "true");
        context.setProperty("flag2", "false");
        Predicate<PropertyContext> condition = Conditions.and(
                Conditions.propertyIsTrue("flag1"),
                Conditions.propertyIsTrue("flag2")
        );
        assertThat(condition.test(context)).isFalse();
    }

    // or() tests
    @Test
    public void or_OneTrue_ReturnsTrue() {
        context.setProperty("flag1", "false");
        context.setProperty("flag2", "true");
        Predicate<PropertyContext> condition = Conditions.or(
                Conditions.propertyIsTrue("flag1"),
                Conditions.propertyIsTrue("flag2")
        );
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void or_AllFalse_ReturnsFalse() {
        context.setProperty("flag1", "false");
        context.setProperty("flag2", "false");
        Predicate<PropertyContext> condition = Conditions.or(
                Conditions.propertyIsTrue("flag1"),
                Conditions.propertyIsTrue("flag2")
        );
        assertThat(condition.test(context)).isFalse();
    }

    // allPropertiesPresent() tests
    @Test
    public void allPropertiesPresent_AllExist_ReturnsTrue() {
        context.setProperty("a", "1");
        context.setProperty("b", "2");
        context.setProperty("c", "3");
        Predicate<PropertyContext> condition = Conditions.allPropertiesPresent("a", "b", "c");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void allPropertiesPresent_OneMissing_ReturnsFalse() {
        context.setProperty("a", "1");
        context.setProperty("b", "2");
        Predicate<PropertyContext> condition = Conditions.allPropertiesPresent("a", "b", "c");
        assertThat(condition.test(context)).isFalse();
    }

    // anyPropertyPresent() tests
    @Test
    public void anyPropertyPresent_OneExists_ReturnsTrue() {
        context.setProperty("a", "1");
        Predicate<PropertyContext> condition = Conditions.anyPropertyPresent("a", "b", "c");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void anyPropertyPresent_NonExist_ReturnsFalse() {
        Predicate<PropertyContext> condition = Conditions.anyPropertyPresent("a", "b", "c");
        assertThat(condition.test(context)).isFalse();
    }

    // integerPropertyBetween() tests
    @Test
    public void integerPropertyBetween_ValidValue_ReturnsTrue() {
        context.setTypedProperty("port", 8080);
        Predicate<PropertyContext> condition = Conditions.integerPropertyBetween("port", 1, 65535);
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void integerPropertyBetween_OutOfRange_ReturnsFalse() {
        context.setTypedProperty("port", 70000);
        Predicate<PropertyContext> condition = Conditions.integerPropertyBetween("port", 1, 65535);
        assertThat(condition.test(context)).isFalse();
    }

    // propertyOneOf() tests
    @Test
    public void propertyOneOf_AllowedValue_ReturnsTrue() {
        context.setProperty("env", "staging");
        Predicate<PropertyContext> condition = Conditions.propertyOneOf("env", "dev", "staging", "prod");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyOneOf_DisallowedValue_ReturnsFalse() {
        context.setProperty("env", "test");
        Predicate<PropertyContext> condition = Conditions.propertyOneOf("env", "dev", "staging", "prod");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyStartsWith() tests
    @Test
    public void propertyStartsWith_CorrectPrefix_ReturnsTrue() {
        context.setProperty("url", "https://example.com");
        Predicate<PropertyContext> condition = Conditions.propertyStartsWith("url", "https");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyStartsWith_WrongPrefix_ReturnsFalse() {
        context.setProperty("url", "http://example.com");
        Predicate<PropertyContext> condition = Conditions.propertyStartsWith("url", "https");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyEndsWith() tests
    @Test
    public void propertyEndsWith_CorrectSuffix_ReturnsTrue() {
        context.setProperty("file", "config.yaml");
        Predicate<PropertyContext> condition = Conditions.propertyEndsWith("file", ".yaml");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyEndsWith_WrongSuffix_ReturnsFalse() {
        context.setProperty("file", "config.json");
        Predicate<PropertyContext> condition = Conditions.propertyEndsWith("file", ".yaml");
        assertThat(condition.test(context)).isFalse();
    }

    // propertyContains() tests
    @Test
    public void propertyContains_ContainsSubstring_ReturnsTrue() {
        context.setProperty("path", "/usr/local/bin");
        Predicate<PropertyContext> condition = Conditions.propertyContains("path", "local");
        assertThat(condition.test(context)).isTrue();
    }

    @Test
    public void propertyContains_NoSubstring_ReturnsFalse() {
        context.setProperty("path", "/usr/bin");
        Predicate<PropertyContext> condition = Conditions.propertyContains("path", "local");
        assertThat(condition.test(context)).isFalse();
    }

    // Complex combinations
    @Test
    public void complexCondition_Works() {
        context.setProperty("ssl.enabled", "true");
        context.setProperty("ssl.port", "443");

        Predicate<PropertyContext> condition = Conditions.and(
                Conditions.propertyIsTrue("ssl.enabled"),
                Conditions.propertyEquals("ssl.port", "443")
        );

        assertThat(condition.test(context)).isTrue();
    }

    // Test context implementation
    private static class TestPropertyContext implements PropertyContext {
        private final Map<String, String> properties = new HashMap<>();
        private final Map<String, Object> typedProperties = new HashMap<>();
        private final Map<String, String> metadata = new HashMap<>();

        public void setProperty(String name, String value) {
            properties.put(name, value);
        }

        public <T> void setTypedProperty(String name, T value) {
            typedProperties.put(name, value);
        }

        public void setMetadata(String key, String value) {
            metadata.put(key, value);
        }

        @Override
        public Optional<String> getProperty(String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
            return Optional.ofNullable((T) typedProperties.get(propertyName));
        }

        @Override
        public Map<String, String> getAllProperties() {
            return Collections.unmodifiableMap(properties);
        }

        @Override
        public Optional<String> getMetadata(String key) {
            return Optional.ofNullable(metadata.get(key));
        }

        @Override
        public boolean hasProperty(String propertyName) {
            return properties.containsKey(propertyName);
        }
    }
}
