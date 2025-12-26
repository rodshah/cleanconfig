package com.cleanconfig.core.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a logical group of related properties with group-level validation rules.
 *
 * <p>Property groups help organize configuration by domain or functional area,
 * making it easier to apply validation rules to related properties. For example,
 * all database-related properties can be grouped together with validation rules
 * that ensure they form a valid database configuration.
 *
 * <p>Example usage:
 * <pre>
 * // Define a database configuration group
 * PropertyGroup dbGroup = PropertyGroup.builder("database")
 *     .addProperty("db.host")
 *     .addProperty("db.port")
 *     .addProperty("db.username")
 *     .addProperty("db.password")
 *     .addRule(ConditionalRequirementRules.allOrNothing(
 *         "db.username", "db.password"
 *     ))
 *     .addRule(NumericRelationshipRules.greaterThan(
 *         "db.port", "db.minPort", Integer.class
 *     ))
 *     .build();
 *
 * // Use in configuration builder
 * Config config = ConfigBuilder.create()
 *     .addPropertyGroup(dbGroup)
 *     .build();
 * </pre>
 *
 * @since 0.2.0
 */
public final class PropertyGroup {

    private final String name;
    private final List<String> propertyNames;
    private final List<MultiPropertyValidationRule> rules;
    private final String description;

    private PropertyGroup(Builder builder) {
        this.name = builder.name;
        this.propertyNames = Collections.unmodifiableList(new ArrayList<>(builder.propertyNames));
        this.rules = Collections.unmodifiableList(new ArrayList<>(builder.rules));
        this.description = builder.description;
    }

    /**
     * Gets the name of this property group.
     *
     * @return the group name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the names of properties in this group.
     *
     * @return unmodifiable list of property names
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * Gets the validation rules for this group.
     *
     * @return unmodifiable list of validation rules
     */
    public List<MultiPropertyValidationRule> getRules() {
        return rules;
    }

    /**
     * Gets the description of this property group.
     *
     * @return the description, or null if not set
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates a new builder for constructing a property group.
     *
     * @param name the name of the group
     * @return a new builder
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is empty
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for creating property groups.
     */
    public static final class Builder {
        private final String name;
        private final List<String> propertyNames = new ArrayList<>();
        private final List<MultiPropertyValidationRule> rules = new ArrayList<>();
        private String description;

        private Builder(String name) {
            Objects.requireNonNull(name, "Group name cannot be null");
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Group name cannot be empty");
            }
            this.name = name;
        }

        /**
         * Adds a property to this group.
         *
         * @param propertyName the property name
         * @return this builder
         * @throws NullPointerException if propertyName is null
         */
        public Builder addProperty(String propertyName) {
            Objects.requireNonNull(propertyName, "Property name cannot be null");
            this.propertyNames.add(propertyName);
            return this;
        }

        /**
         * Adds multiple properties to this group.
         *
         * @param propertyNames the property names
         * @return this builder
         * @throws NullPointerException if propertyNames is null
         */
        public Builder addProperties(String... propertyNames) {
            Objects.requireNonNull(propertyNames, "Property names cannot be null");
            this.propertyNames.addAll(Arrays.asList(propertyNames));
            return this;
        }

        /**
         * Adds a validation rule to this group.
         *
         * @param rule the validation rule
         * @return this builder
         * @throws NullPointerException if rule is null
         */
        public Builder addRule(MultiPropertyValidationRule rule) {
            Objects.requireNonNull(rule, "Validation rule cannot be null");
            this.rules.add(rule);
            return this;
        }

        /**
         * Sets the description for this group.
         *
         * @param description the description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the property group.
         *
         * @return a new property group
         * @throws IllegalArgumentException if no properties have been added
         */
        public PropertyGroup build() {
            if (propertyNames.isEmpty()) {
                throw new IllegalArgumentException(
                        "Property group must contain at least one property");
            }
            return new PropertyGroup(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PropertyGroup that = (PropertyGroup) o;
        return Objects.equals(name, that.name)
                && Objects.equals(propertyNames, that.propertyNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, propertyNames);
    }

    @Override
    public String toString() {
        return "PropertyGroup{"
                + "name='" + name + '\''
                + ", propertyCount=" + propertyNames.size()
                + ", ruleCount=" + rules.size()
                + (description != null ? ", description='" + description + '\'' : "")
                + '}';
    }
}
