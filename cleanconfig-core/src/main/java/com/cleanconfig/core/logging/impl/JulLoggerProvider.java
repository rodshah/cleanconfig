package com.cleanconfig.core.logging.impl;

import com.cleanconfig.core.logging.Logger;
import com.cleanconfig.core.logging.LoggerProvider;

import java.util.logging.Level;

/**
 * Logger provider that delegates to java.util.logging (JUL).
 *
 * <p>This provider is always available as JUL is part of the standard JDK.
 * It serves as the fallback logging implementation.
 *
 * @since 0.1.0
 */
public class JulLoggerProvider implements LoggerProvider {

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new JulLogger(java.util.logging.Logger.getLogger(clazz.getName()));
    }

    @Override
    public Logger getLogger(String name) {
        return new JulLogger(java.util.logging.Logger.getLogger(name));
    }

    @Override
    public String getProviderName() {
        return "JUL";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * Logger implementation that delegates to a JUL logger instance.
     */
    private static class JulLogger implements Logger {
        private final java.util.logging.Logger julLogger;

        JulLogger(java.util.logging.Logger julLogger) {
            this.julLogger = julLogger;
        }

        @Override
        public void debug(String message) {
            julLogger.log(Level.FINE, message);
        }

        @Override
        public void debug(String message, Object... args) {
            if (julLogger.isLoggable(Level.FINE)) {
                julLogger.log(Level.FINE, format(message, args));
            }
        }

        @Override
        public void info(String message) {
            julLogger.log(Level.INFO, message);
        }

        @Override
        public void info(String message, Object... args) {
            if (julLogger.isLoggable(Level.INFO)) {
                julLogger.log(Level.INFO, format(message, args));
            }
        }

        @Override
        public void warn(String message) {
            julLogger.log(Level.WARNING, message);
        }

        @Override
        public void warn(String message, Object... args) {
            if (julLogger.isLoggable(Level.WARNING)) {
                julLogger.log(Level.WARNING, format(message, args));
            }
        }

        @Override
        public void warn(String message, Throwable throwable) {
            julLogger.log(Level.WARNING, message, throwable);
        }

        @Override
        public void error(String message) {
            julLogger.log(Level.SEVERE, message);
        }

        @Override
        public void error(String message, Object... args) {
            if (julLogger.isLoggable(Level.SEVERE)) {
                julLogger.log(Level.SEVERE, format(message, args));
            }
        }

        @Override
        public void error(String message, Throwable throwable) {
            julLogger.log(Level.SEVERE, message, throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return julLogger.isLoggable(Level.FINE);
        }

        @Override
        public boolean isInfoEnabled() {
            return julLogger.isLoggable(Level.INFO);
        }

        /**
         * Formats a message with arguments using {} placeholders.
         *
         * @param message the message template
         * @param args the arguments
         * @return the formatted message
         */
        private String format(String message, Object... args) {
            if (args == null || args.length == 0) {
                return message;
            }

            StringBuilder result = new StringBuilder();
            int argIndex = 0;
            int pos = 0;

            while (pos < message.length()) {
                int placeholderPos = message.indexOf("{}", pos);
                if (placeholderPos == -1) {
                    result.append(message.substring(pos));
                    break;
                }

                result.append(message, pos, placeholderPos);
                if (argIndex < args.length) {
                    result.append(args[argIndex++]);
                } else {
                    result.append("{}");
                }
                pos = placeholderPos + 2;
            }

            return result.toString();
        }
    }
}
