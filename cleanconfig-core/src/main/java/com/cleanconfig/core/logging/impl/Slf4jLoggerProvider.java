package com.cleanconfig.core.logging.impl;

import com.cleanconfig.core.logging.Logger;
import com.cleanconfig.core.logging.LoggerProvider;

/**
 * Logger provider that delegates to SLF4J.
 *
 * <p>This provider is only available if SLF4J is on the classpath.
 * It uses reflection to avoid a hard dependency on SLF4J.
 *
 * @since 0.1.0
 */
public class Slf4jLoggerProvider implements LoggerProvider {

    private static final String SLF4J_LOGGER_FACTORY = "org.slf4j.LoggerFactory";
    private final boolean available;

    public Slf4jLoggerProvider() {
        this.available = checkAvailability();
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        if (!available) {
            throw new IllegalStateException("SLF4J is not available");
        }
        try {
            Class<?> loggerFactoryClass = Class.forName(SLF4J_LOGGER_FACTORY);
            Object slf4jLogger = loggerFactoryClass
                    .getMethod("getLogger", Class.class)
                    .invoke(null, clazz);
            return new Slf4jLogger(slf4jLogger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SLF4J logger", e);
        }
    }

    @Override
    public Logger getLogger(String name) {
        if (!available) {
            throw new IllegalStateException("SLF4J is not available");
        }
        try {
            Class<?> loggerFactoryClass = Class.forName(SLF4J_LOGGER_FACTORY);
            Object slf4jLogger = loggerFactoryClass
                    .getMethod("getLogger", String.class)
                    .invoke(null, name);
            return new Slf4jLogger(slf4jLogger);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SLF4J logger", e);
        }
    }

    @Override
    public String getProviderName() {
        return "SLF4J";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    private boolean checkAvailability() {
        try {
            Class.forName(SLF4J_LOGGER_FACTORY);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Logger implementation that delegates to an SLF4J logger instance.
     */
    private static class Slf4jLogger implements Logger {
        private final Object slf4jLogger;

        Slf4jLogger(Object slf4jLogger) {
            this.slf4jLogger = slf4jLogger;
        }

        @Override
        public void debug(String message) {
            invoke("debug", message);
        }

        @Override
        public void debug(String message, Object... args) {
            invoke("debug", message, args);
        }

        @Override
        public void info(String message) {
            invoke("info", message);
        }

        @Override
        public void info(String message, Object... args) {
            invoke("info", message, args);
        }

        @Override
        public void warn(String message) {
            invoke("warn", message);
        }

        @Override
        public void warn(String message, Object... args) {
            invoke("warn", message, args);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            invoke("warn", message, throwable);
        }

        @Override
        public void error(String message) {
            invoke("error", message);
        }

        @Override
        public void error(String message, Object... args) {
            invoke("error", message, args);
        }

        @Override
        public void error(String message, Throwable throwable) {
            invoke("error", message, throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return invokeBoolean("isDebugEnabled");
        }

        @Override
        public boolean isInfoEnabled() {
            return invokeBoolean("isInfoEnabled");
        }

        private void invoke(String methodName, Object... args) {
            try {
                Class<?>[] paramTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof String) {
                        paramTypes[i] = String.class;
                    } else if (args[i] instanceof Throwable) {
                        paramTypes[i] = Throwable.class;
                    } else if (args[i] instanceof Object[]) {
                        paramTypes[i] = Object[].class;
                    } else {
                        paramTypes[i] = Object.class;
                    }
                }
                slf4jLogger.getClass().getMethod(methodName, paramTypes).invoke(slf4jLogger, args);
            } catch (Exception e) {
                // Logging failed - silently ignore to avoid recursive logging errors
            }
        }

        private boolean invokeBoolean(String methodName) {
            try {
                return (Boolean) slf4jLogger.getClass().getMethod(methodName).invoke(slf4jLogger);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
