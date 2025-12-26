package com.cleanconfig.serialization;

import java.util.Objects;

/**
 * Options for property serialization.
 * <p>
 * Controls various aspects of serialization including formatting,
 * metadata inclusion, and default value handling.
 * </p>
 *
 * @since 0.1.0
 */
public final class SerializationOptions {

    private final boolean prettyPrint;
    private final boolean includeMetadata;
    private final boolean includeDefaults;
    private final boolean includeDescriptions;

    private SerializationOptions(Builder builder) {
        this.prettyPrint = builder.prettyPrint;
        this.includeMetadata = builder.includeMetadata;
        this.includeDefaults = builder.includeDefaults;
        this.includeDescriptions = builder.includeDescriptions;
    }

    /**
     * Returns whether to use pretty printing (indentation, line breaks).
     *
     * @return true if pretty printing is enabled
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * Returns whether to include property metadata (types, categories, etc.).
     *
     * @return true if metadata should be included
     */
    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    /**
     * Returns whether to include default values in the output.
     *
     * @return true if default values should be included
     */
    public boolean isIncludeDefaults() {
        return includeDefaults;
    }

    /**
     * Returns whether to include property descriptions as comments.
     *
     * @return true if descriptions should be included
     */
    public boolean isIncludeDescriptions() {
        return includeDescriptions;
    }

    /**
     * Creates a new builder for serialization options.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates default serialization options (pretty print enabled, no metadata/defaults).
     *
     * @return default options
     */
    public static SerializationOptions defaults() {
        return builder()
                .prettyPrint(true)
                .includeMetadata(false)
                .includeDefaults(false)
                .includeDescriptions(false)
                .build();
    }

    /**
     * Creates compact serialization options (no formatting, no metadata).
     *
     * @return compact options
     */
    public static SerializationOptions compact() {
        return builder()
                .prettyPrint(false)
                .includeMetadata(false)
                .includeDefaults(false)
                .includeDescriptions(false)
                .build();
    }

    /**
     * Creates verbose serialization options (pretty print, metadata, defaults, descriptions).
     *
     * @return verbose options
     */
    public static SerializationOptions verbose() {
        return builder()
                .prettyPrint(true)
                .includeMetadata(true)
                .includeDefaults(true)
                .includeDescriptions(true)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SerializationOptions that = (SerializationOptions) o;
        return prettyPrint == that.prettyPrint
                && includeMetadata == that.includeMetadata
                && includeDefaults == that.includeDefaults
                && includeDescriptions == that.includeDescriptions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prettyPrint, includeMetadata, includeDefaults, includeDescriptions);
    }

    @Override
    public String toString() {
        return "SerializationOptions{"
                + "prettyPrint=" + prettyPrint
                + ", includeMetadata=" + includeMetadata
                + ", includeDefaults=" + includeDefaults
                + ", includeDescriptions=" + includeDescriptions
                + '}';
    }

    /**
     * Builder for {@link SerializationOptions}.
     */
    public static final class Builder {
        private boolean prettyPrint = true;
        private boolean includeMetadata = false;
        private boolean includeDefaults = false;
        private boolean includeDescriptions = false;

        private Builder() {
        }

        /**
         * Sets whether to use pretty printing.
         *
         * @param prettyPrint true to enable pretty printing
         * @return this builder
         */
        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        /**
         * Sets whether to include property metadata.
         *
         * @param includeMetadata true to include metadata
         * @return this builder
         */
        public Builder includeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
            return this;
        }

        /**
         * Sets whether to include default values.
         *
         * @param includeDefaults true to include defaults
         * @return this builder
         */
        public Builder includeDefaults(boolean includeDefaults) {
            this.includeDefaults = includeDefaults;
            return this;
        }

        /**
         * Sets whether to include property descriptions.
         *
         * @param includeDescriptions true to include descriptions
         * @return this builder
         */
        public Builder includeDescriptions(boolean includeDescriptions) {
            this.includeDescriptions = includeDescriptions;
            return this;
        }

        /**
         * Builds the serialization options.
         *
         * @return the serialization options
         */
        public SerializationOptions build() {
            return new SerializationOptions(this);
        }
    }
}
