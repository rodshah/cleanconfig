package com.cleanconfig.core.logging;

import com.cleanconfig.core.logging.impl.JulLoggerProvider;
import com.cleanconfig.core.logging.impl.NoOpLoggerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link LoggerFactory}.
 */
public class LoggerFactoryTest {

    @Before
    public void setUp() {
        LoggerFactory.reset();
    }

    @After
    public void tearDown() {
        LoggerFactory.reset();
    }

    @Test
    public void getLogger_WithClass_ReturnsLogger() {
        Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        assertThat(logger).isNotNull();
    }

    @Test
    public void getLogger_WithString_ReturnsLogger() {
        Logger logger = LoggerFactory.getLogger("test.logger");
        assertThat(logger).isNotNull();
    }

    @Test
    public void getProvider_AfterAutoDetection_ReturnsProvider() {
        LoggerProvider provider = LoggerFactory.getProvider();
        assertThat(provider).isNotNull();
        assertThat(provider.getProviderName()).isIn("SLF4J", "JUL", "No-Op", "Fallback-No-Op");
    }

    @Test
    public void setLoggerProvider_WithValidProvider_SetsProvider() {
        LoggerProvider customProvider = new NoOpLoggerProvider();
        LoggerFactory.setLoggerProvider(customProvider);

        LoggerProvider provider = LoggerFactory.getProvider();
        assertThat(provider).isSameAs(customProvider);
        assertThat(provider.getProviderName()).isEqualTo("No-Op");
    }

    @Test
    public void setLoggerProvider_WithNull_ThrowsException() {
        assertThatThrownBy(() -> LoggerFactory.setLoggerProvider(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("LoggerProvider cannot be null");
    }

    @Test
    public void getLogger_AfterSettingProvider_UsesCustomProvider() {
        LoggerFactory.setLoggerProvider(new NoOpLoggerProvider());

        Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);
        assertThat(logger).isNotNull();
        assertThat(logger.isDebugEnabled()).isFalse();
        assertThat(logger.isInfoEnabled()).isFalse();
    }

    @Test
    public void reset_ClearsProvider_TriggersAutoDetectionOnNextUse() {
        LoggerFactory.setLoggerProvider(new NoOpLoggerProvider());
        assertThat(LoggerFactory.getProvider().getProviderName()).isEqualTo("No-Op");

        LoggerFactory.reset();

        // After reset, auto-detection should run again
        LoggerProvider provider = LoggerFactory.getProvider();
        assertThat(provider).isNotNull();
        // Should auto-detect to JUL since SLF4J likely not available in test
        assertThat(provider.getProviderName()).isIn("SLF4J", "JUL", "No-Op", "Fallback-No-Op");
    }
}
