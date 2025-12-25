package com.cleanconfig.core.logging.impl;

import com.cleanconfig.core.logging.Logger;
import com.cleanconfig.core.logging.LoggerProvider;

/**
 * Logger provider that provides no-op (silent) logging.
 *
 * <p>This provider discards all log messages. It's useful when:
 * <ul>
 *   <li>No logging is desired</li>
 *   <li>In production environments where logging overhead should be minimized</li>
 *   <li>For testing scenarios where log output is not needed</li>
 * </ul>
 *
 * <p>Users can explicitly set this provider:
 * <pre>
 * LoggerFactory.setLoggerProvider(new NoOpLoggerProvider());
 * </pre>
 *
 * @since 0.1.0
 */
public class NoOpLoggerProvider implements LoggerProvider {

    private static final Logger NO_OP_LOGGER = new NoOpLogger();

    @Override
    public Logger getLogger(Class<?> clazz) {
        return NO_OP_LOGGER;
    }

    @Override
    public Logger getLogger(String name) {
        return NO_OP_LOGGER;
    }

    @Override
    public String getProviderName() {
        return "No-Op";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * No-op logger implementation that discards all log messages.
     */
    private static class NoOpLogger implements Logger {

        @Override
        public void debug(String message) {
            // No-op
        }

        @Override
        public void debug(String message, Object... args) {
            // No-op
        }

        @Override
        public void info(String message) {
            // No-op
        }

        @Override
        public void info(String message, Object... args) {
            // No-op
        }

        @Override
        public void warn(String message) {
            // No-op
        }

        @Override
        public void warn(String message, Object... args) {
            // No-op
        }

        @Override
        public void warn(String message, Throwable throwable) {
            // No-op
        }

        @Override
        public void error(String message) {
            // No-op
        }

        @Override
        public void error(String message, Object... args) {
            // No-op
        }

        @Override
        public void error(String message, Throwable throwable) {
            // No-op
        }

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
