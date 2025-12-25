package com.cleanconfig.core.logging;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory for creating logger instances with automatic provider detection.
 *
 * <p>The factory automatically detects available logging frameworks in this order:
 * <ol>
 *   <li>SLF4J (if available on classpath)</li>
 *   <li>JUL (java.util.logging - always available)</li>
 *   <li>No-Op (silent logging)</li>
 * </ol>
 *
 * <p>Users can override the auto-detected provider:
 * <pre>
 * LoggerFactory.setLoggerProvider(new CustomLoggerProvider());
 * </pre>
 *
 * <p>Example usage:
 * <pre>
 * Logger log = LoggerFactory.getLogger(MyClass.class);
 * log.info("Processing started");
 * </pre>
 *
 * @since 0.1.0
 */
public final class LoggerFactory {

    private static final AtomicReference<LoggerProvider> PROVIDER = new AtomicReference<>();
    private static volatile boolean autoDetectionAttempted = false;

    private LoggerFactory() {
        // Utility class
    }

    /**
     * Gets a logger for the specified class.
     *
     * @param clazz the class for which to create a logger
     * @return a logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return getProvider().getLogger(clazz);
    }

    /**
     * Gets a logger with the specified name.
     *
     * @param name the logger name
     * @return a logger instance
     */
    public static Logger getLogger(String name) {
        return getProvider().getLogger(name);
    }

    /**
     * Sets a custom logger provider.
     *
     * <p>This overrides the auto-detected provider.
     *
     * @param provider the logger provider to use
     * @throws IllegalArgumentException if provider is null
     */
    public static void setLoggerProvider(LoggerProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("LoggerProvider cannot be null");
        }
        PROVIDER.set(provider);
        autoDetectionAttempted = true;
    }

    /**
     * Gets the current logger provider.
     *
     * <p>If no provider has been set, auto-detection is performed.
     *
     * @return the current logger provider
     */
    public static LoggerProvider getProvider() {
        LoggerProvider provider = PROVIDER.get();
        if (provider == null && !autoDetectionAttempted) {
            synchronized (LoggerFactory.class) {
                provider = PROVIDER.get();
                if (provider == null && !autoDetectionAttempted) {
                    provider = autoDetectProvider();
                    PROVIDER.set(provider);
                    autoDetectionAttempted = true;
                }
            }
        }
        return PROVIDER.get();
    }

    /**
     * Resets the logger provider to trigger auto-detection on next use.
     *
     * <p>This is primarily useful for testing.
     */
    static void reset() {
        PROVIDER.set(null);
        autoDetectionAttempted = false;
    }

    /**
     * Auto-detects the best available logging provider.
     *
     * <p>Detection order: SLF4J → JUL → No-Op
     *
     * @return the detected logger provider
     */
    private static LoggerProvider autoDetectProvider() {
        // Try SLF4J first
        LoggerProvider slf4jProvider = tryLoadProvider("com.cleanconfig.core.logging.impl.Slf4jLoggerProvider");
        if (slf4jProvider != null && slf4jProvider.isAvailable()) {
            return slf4jProvider;
        }

        // Fallback to JUL (always available)
        LoggerProvider julProvider = tryLoadProvider("com.cleanconfig.core.logging.impl.JulLoggerProvider");
        if (julProvider != null && julProvider.isAvailable()) {
            return julProvider;
        }

        // Last resort: No-Op
        LoggerProvider noOpProvider = tryLoadProvider("com.cleanconfig.core.logging.impl.NoOpLoggerProvider");
        if (noOpProvider != null) {
            return noOpProvider;
        }

        // This should never happen, but provide a fallback
        return new LoggerProvider() {
            @Override
            public Logger getLogger(Class<?> clazz) {
                return new NoOpLogger();
            }

            @Override
            public Logger getLogger(String name) {
                return new NoOpLogger();
            }

            @Override
            public String getProviderName() {
                return "Fallback-No-Op";
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };
    }

    /**
     * Attempts to load a provider class by name.
     *
     * @param className the fully qualified class name
     * @return the provider instance, or null if loading failed
     */
    private static LoggerProvider tryLoadProvider(String className) {
        try {
            Class<?> providerClass = Class.forName(className);
            return (LoggerProvider) providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Provider not available - this is expected
            return null;
        }
    }

    /**
     * Simple no-op logger for fallback.
     */
    private static class NoOpLogger implements Logger {
        @Override
        public void debug(String message) { }

        @Override
        public void debug(String message, Object... args) { }

        @Override
        public void info(String message) { }

        @Override
        public void info(String message, Object... args) { }

        @Override
        public void warn(String message) { }

        @Override
        public void warn(String message, Object... args) { }

        @Override
        public void warn(String message, Throwable throwable) { }

        @Override
        public void error(String message) { }

        @Override
        public void error(String message, Object... args) { }

        @Override
        public void error(String message, Throwable throwable) { }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }
    }
}
