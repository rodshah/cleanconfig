package com.cleanconfig.core.logging;

import com.cleanconfig.core.logging.impl.NoOpLoggerProvider;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NoOpLoggerProvider}.
 */
public class NoOpLoggerProviderTest {

    private NoOpLoggerProvider provider;

    @Before
    public void setUp() {
        provider = new NoOpLoggerProvider();
    }

    @Test
    public void getProviderName_ReturnsNoOp() {
        assertThat(provider.getProviderName()).isEqualTo("No-Op");
    }

    @Test
    public void isAvailable_ReturnsTrue() {
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    public void getLogger_WithClass_ReturnsLogger() {
        Logger logger = provider.getLogger(NoOpLoggerProviderTest.class);
        assertThat(logger).isNotNull();
    }

    @Test
    public void getLogger_WithString_ReturnsLogger() {
        Logger logger = provider.getLogger("test.logger");
        assertThat(logger).isNotNull();
    }

    @Test
    public void getLogger_ReturnsSameInstance() {
        Logger logger1 = provider.getLogger("test.logger");
        Logger logger2 = provider.getLogger("another.logger");
        assertThat(logger1).isSameAs(logger2);
    }

    @Test
    public void debug_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.debug("Debug message");
        // No exception = success
    }

    @Test
    public void debug_WithArgs_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.debug("Value: {}, Count: {}", "test", 42);
        // No exception = success
    }

    @Test
    public void info_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.info("Info message");
        // No exception = success
    }

    @Test
    public void info_WithArgs_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.info("Processing: {}", "item");
        // No exception = success
    }

    @Test
    public void warn_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.warn("Warning message");
        // No exception = success
    }

    @Test
    public void warn_WithArgs_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.warn("Issue with: {}", "configuration");
        // No exception = success
    }

    @Test
    public void warn_WithThrowable_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.warn("Error occurred", new RuntimeException("Test"));
        // No exception = success
    }

    @Test
    public void error_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.error("Error message");
        // No exception = success
    }

    @Test
    public void error_WithArgs_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.error("Failed: {}, Reason: {}", "operation", "timeout");
        // No exception = success
    }

    @Test
    public void error_WithThrowable_DoesNotThrowException() {
        Logger logger = provider.getLogger("test.logger");
        logger.error("Critical error", new IllegalStateException("Invalid"));
        // No exception = success
    }

    @Test
    public void isDebugEnabled_ReturnsFalse() {
        Logger logger = provider.getLogger("test.logger");
        assertThat(logger.isDebugEnabled()).isFalse();
    }

    @Test
    public void isInfoEnabled_ReturnsFalse() {
        Logger logger = provider.getLogger("test.logger");
        assertThat(logger.isInfoEnabled()).isFalse();
    }
}
