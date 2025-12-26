package com.cleanconfig.core.logging;

import com.cleanconfig.core.logging.impl.JulLoggerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JulLoggerProvider}.
 */
public class JulLoggerProviderTest {

    private JulLoggerProvider provider;
    private TestHandler testHandler;
    private java.util.logging.Logger julLogger;

    @Before
    public void setUp() {
        provider = new JulLoggerProvider();
        julLogger = java.util.logging.Logger.getLogger("test.logger");
        julLogger.setLevel(Level.ALL);
        julLogger.setUseParentHandlers(false);

        testHandler = new TestHandler();
        testHandler.setLevel(Level.ALL);
        julLogger.addHandler(testHandler);
    }

    @After
    public void tearDown() {
        if (julLogger != null && testHandler != null) {
            julLogger.removeHandler(testHandler);
        }
    }

    @Test
    public void getProviderName_ReturnsJUL() {
        assertThat(provider.getProviderName()).isEqualTo("JUL");
    }

    @Test
    public void isAvailable_ReturnsTrue() {
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    public void getLogger_WithClass_ReturnsLogger() {
        Logger logger = provider.getLogger(JulLoggerProviderTest.class);
        assertThat(logger).isNotNull();
    }

    @Test
    public void getLogger_WithString_ReturnsLogger() {
        Logger logger = provider.getLogger("test.logger");
        assertThat(logger).isNotNull();
    }

    @Test
    public void debug_LogsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.debug("Debug message");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.FINE);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Debug message");
    }

    @Test
    public void debug_WithArgs_FormatsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.debug("Value: {}, Count: {}", "test", 42);

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Value: test, Count: 42");
    }

    @Test
    public void info_LogsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.info("Info message");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.INFO);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Info message");
    }

    @Test
    public void info_WithArgs_FormatsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.info("Processing: {}", "item");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Processing: item");
    }

    @Test
    public void warn_LogsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.warn("Warning message");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.WARNING);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Warning message");
    }

    @Test
    public void warn_WithArgs_FormatsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.warn("Issue with: {}", "configuration");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Issue with: configuration");
    }

    @Test
    public void warn_WithThrowable_LogsException() {
        Logger logger = provider.getLogger("test.logger");
        Exception ex = new RuntimeException("Test exception");
        logger.warn("Error occurred", ex);

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.WARNING);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Error occurred");
        assertThat(testHandler.getLastRecord().getThrown()).isSameAs(ex);
    }

    @Test
    public void error_LogsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.error("Error message");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.SEVERE);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Error message");
    }

    @Test
    public void error_WithArgs_FormatsMessage() {
        Logger logger = provider.getLogger("test.logger");
        logger.error("Failed: {}, Reason: {}", "operation", "timeout");

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Failed: operation, Reason: timeout");
    }

    @Test
    public void error_WithThrowable_LogsException() {
        Logger logger = provider.getLogger("test.logger");
        Exception ex = new IllegalStateException("Invalid state");
        logger.error("Critical error", ex);

        assertThat(testHandler.getLastRecord()).isNotNull();
        assertThat(testHandler.getLastRecord().getLevel()).isEqualTo(Level.SEVERE);
        assertThat(testHandler.getLastRecord().getMessage()).isEqualTo("Critical error");
        assertThat(testHandler.getLastRecord().getThrown()).isSameAs(ex);
    }

    @Test
    public void isDebugEnabled_ReturnsTrue_WhenLevelIsFine() {
        julLogger.setLevel(Level.FINE);
        Logger logger = provider.getLogger("test.logger");

        assertThat(logger.isDebugEnabled()).isTrue();
    }

    @Test
    public void isDebugEnabled_ReturnsFalse_WhenLevelIsInfo() {
        julLogger.setLevel(Level.INFO);
        Logger logger = provider.getLogger("test.logger");

        assertThat(logger.isDebugEnabled()).isFalse();
    }

    @Test
    public void isInfoEnabled_ReturnsTrue_WhenLevelIsInfo() {
        julLogger.setLevel(Level.INFO);
        Logger logger = provider.getLogger("test.logger");

        assertThat(logger.isInfoEnabled()).isTrue();
    }

    @Test
    public void isInfoEnabled_ReturnsFalse_WhenLevelIsWarning() {
        julLogger.setLevel(Level.WARNING);
        Logger logger = provider.getLogger("test.logger");

        assertThat(logger.isInfoEnabled()).isFalse();
    }

    /**
     * Test handler that captures log records.
     */
    private static class TestHandler extends Handler {
        private LogRecord lastRecord;

        @Override
        public void publish(LogRecord record) {
            this.lastRecord = record;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public LogRecord getLastRecord() {
            return lastRecord;
        }
    }
}
