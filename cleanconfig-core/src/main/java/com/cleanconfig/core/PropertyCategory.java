package com.cleanconfig.core;

/**
 * Categories for organizing and grouping properties.
 *
 * <p>Properties can be categorized for better organization, documentation,
 * and UI presentation.
 *
 * <p>Example usage:
 * <pre>
 * PropertyDefinition&lt;Integer&gt; PORT = PropertyDefinition.builder(Integer.class)
 *     .name("server.port")
 *     .category(PropertyCategory.NETWORKING)
 *     .build();
 * </pre>
 *
 * @since 0.1.0
 */
public enum PropertyCategory {

    /**
     * General/uncategorized properties.
     */
    GENERAL,

    /**
     * Networking-related properties (ports, hosts, timeouts, etc.).
     */
    NETWORKING,

    /**
     * Security-related properties (auth, encryption, certificates, etc.).
     */
    SECURITY,

    /**
     * Database-related properties (connections, pools, queries, etc.).
     */
    DATABASE,

    /**
     * Performance and resource properties (threads, memory, caching, etc.).
     */
    PERFORMANCE,

    /**
     * Logging and monitoring properties.
     */
    LOGGING,

    /**
     * Feature flags and toggles.
     */
    FEATURE_FLAGS,

    /**
     * UI/presentation properties.
     */
    UI,

    /**
     * Integration properties (external APIs, services, etc.).
     */
    INTEGRATION,

    /**
     * Storage properties (files, paths, volumes, etc.).
     */
    STORAGE,

    /**
     * Application-specific business logic properties.
     */
    BUSINESS_LOGIC
}
