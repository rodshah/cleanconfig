package com.cleanconfig.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultApplicationResult}.
 */
public class DefaultApplicationResultTest {

    private Map<String, String> properties;
    private DefaultApplicationInfo info;
    private DefaultApplicationResult result;

    @Before
    public void setUp() {
        properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");

        Map<String, String> appliedDefaults = new HashMap<>();
        appliedDefaults.put("prop2", "value2");

        info = new DefaultApplicationInfo(appliedDefaults);
        result = new DefaultApplicationResult(properties, info);
    }

    @Test
    public void constructor_NullProperties_ThrowsException() {
        assertThatThrownBy(() -> new DefaultApplicationResult(null, info))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Properties cannot be null");
    }

    @Test
    public void constructor_NullApplicationInfo_ThrowsException() {
        assertThatThrownBy(() -> new DefaultApplicationResult(properties, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Application info cannot be null");
    }

    @Test
    public void getPropertiesWithDefaults_ReturnsAllProperties() {
        Map<String, String> props = result.getPropertiesWithDefaults();

        assertThat(props).hasSize(2);
        assertThat(props).containsEntry("prop1", "value1");
        assertThat(props).containsEntry("prop2", "value2");
    }

    @Test
    public void getPropertiesWithDefaults_ReturnsImmutableMap() {
        Map<String, String> props = result.getPropertiesWithDefaults();

        assertThatThrownBy(() -> props.put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void getApplicationInfo_ReturnsCorrectInfo() {
        DefaultApplicationInfo resultInfo = result.getApplicationInfo();

        assertThat(resultInfo).isSameAs(info);
        assertThat(resultInfo.wasDefaultApplied("prop2")).isTrue();
        assertThat(resultInfo.wasDefaultApplied("prop1")).isFalse();
    }

    @Test
    public void toString_ContainsUsefulInformation() {
        String str = result.toString();

        assertThat(str).contains("DefaultApplicationResult");
        assertThat(str).contains("propertyCount=2");
        assertThat(str).contains("defaultsApplied=1");
    }

    @Test
    public void modifyingInputMap_DoesNotAffectResult() {
        Map<String, String> mutableMap = new HashMap<>();
        mutableMap.put("original", "value");

        DefaultApplicationInfo testInfo = DefaultApplicationInfo.empty();
        DefaultApplicationResult immutableResult = new DefaultApplicationResult(mutableMap, testInfo);

        // Modify the original map
        mutableMap.put("new", "new-value");
        mutableMap.remove("original");

        // Result should be unchanged
        assertThat(immutableResult.getPropertiesWithDefaults()).hasSize(1);
        assertThat(immutableResult.getPropertiesWithDefaults()).containsEntry("original", "value");
        assertThat(immutableResult.getPropertiesWithDefaults()).doesNotContainKey("new");
    }

    @Test
    public void emptyResult_WorksCorrectly() {
        DefaultApplicationResult emptyResult = new DefaultApplicationResult(
                new HashMap<>(),
                DefaultApplicationInfo.empty()
        );

        assertThat(emptyResult.getPropertiesWithDefaults()).isEmpty();
        assertThat(emptyResult.getApplicationInfo().getAppliedDefaultsCount()).isEqualTo(0);
    }
}
