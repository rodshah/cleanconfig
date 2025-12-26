package com.cleanconfig.core;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultApplicationInfo}.
 */
public class DefaultApplicationInfoTest {

    private Map<String, String> appliedDefaults;
    private DefaultApplicationInfo info;

    @Before
    public void setUp() {
        appliedDefaults = new HashMap<>();
        appliedDefaults.put("prop1", "default1");
        appliedDefaults.put("prop2", "default2");

        info = new DefaultApplicationInfo(appliedDefaults);
    }

    @Test
    public void constructor_NullAppliedDefaults_ThrowsException() {
        assertThatThrownBy(() -> new DefaultApplicationInfo(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Applied defaults cannot be null");
    }

    @Test
    public void wasDefaultApplied_ExistingProperty_ReturnsTrue() {
        assertThat(info.wasDefaultApplied("prop1")).isTrue();
        assertThat(info.wasDefaultApplied("prop2")).isTrue();
    }

    @Test
    public void wasDefaultApplied_NonExistingProperty_ReturnsFalse() {
        assertThat(info.wasDefaultApplied("nonexistent")).isFalse();
    }

    @Test
    public void getAppliedValue_ExistingProperty_ReturnsValue() {
        assertThat(info.getAppliedValue("prop1")).hasValue("default1");
        assertThat(info.getAppliedValue("prop2")).hasValue("default2");
    }

    @Test
    public void getAppliedValue_NonExistingProperty_ReturnsEmpty() {
        assertThat(info.getAppliedValue("nonexistent")).isEmpty();
    }

    @Test
    public void getPropertiesWithDefaults_ReturnsAllPropertyNames() {
        assertThat(info.getPropertiesWithDefaults())
                .hasSize(2)
                .containsExactlyInAnyOrder("prop1", "prop2");
    }

    @Test
    public void getPropertiesWithDefaults_ReturnsImmutableSet() {
        assertThatThrownBy(() -> info.getPropertiesWithDefaults().add("new"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void getAppliedDefaultsCount_ReturnsCorrectCount() {
        assertThat(info.getAppliedDefaultsCount()).isEqualTo(2);
    }

    @Test
    public void getAllAppliedDefaults_ReturnsAllDefaults() {
        Map<String, String> all = info.getAllAppliedDefaults();

        assertThat(all).hasSize(2);
        assertThat(all).containsEntry("prop1", "default1");
        assertThat(all).containsEntry("prop2", "default2");
    }

    @Test
    public void getAllAppliedDefaults_ReturnsImmutableMap() {
        Map<String, String> all = info.getAllAppliedDefaults();

        assertThatThrownBy(() -> all.put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void empty_ReturnsEmptyInfo() {
        DefaultApplicationInfo emptyInfo = DefaultApplicationInfo.empty();

        assertThat(emptyInfo.getAppliedDefaultsCount()).isEqualTo(0);
        assertThat(emptyInfo.getPropertiesWithDefaults()).isEmpty();
        assertThat(emptyInfo.wasDefaultApplied("any")).isFalse();
        assertThat(emptyInfo.getAppliedValue("any")).isEmpty();
    }

    @Test
    public void constructor_EmptyMap_CreatesEmptyInfo() {
        DefaultApplicationInfo emptyInfo = new DefaultApplicationInfo(new HashMap<>());

        assertThat(emptyInfo.getAppliedDefaultsCount()).isEqualTo(0);
        assertThat(emptyInfo.getPropertiesWithDefaults()).isEmpty();
    }

    @Test
    public void constructor_PreservesInsertionOrder() {
        Map<String, String> ordered = new java.util.LinkedHashMap<>();
        ordered.put("z", "value-z");
        ordered.put("a", "value-a");
        ordered.put("m", "value-m");

        DefaultApplicationInfo orderedInfo = new DefaultApplicationInfo(ordered);

        // LinkedHashMap should preserve insertion order
        assertThat(orderedInfo.getAllAppliedDefaults().keySet())
                .containsExactly("z", "a", "m");
    }

    @Test
    public void toString_ContainsUsefulInformation() {
        String str = info.toString();

        assertThat(str).contains("DefaultApplicationInfo");
        assertThat(str).contains("appliedDefaultsCount=2");
        assertThat(str).contains("prop1");
        assertThat(str).contains("prop2");
    }

    @Test
    public void modifyingInputMap_DoesNotAffectInfo() {
        Map<String, String> mutableMap = new HashMap<>();
        mutableMap.put("original", "value");

        DefaultApplicationInfo immutableInfo = new DefaultApplicationInfo(mutableMap);

        // Modify the original map
        mutableMap.put("new", "new-value");
        mutableMap.remove("original");

        // Info should be unchanged
        assertThat(immutableInfo.getAppliedDefaultsCount()).isEqualTo(1);
        assertThat(immutableInfo.wasDefaultApplied("original")).isTrue();
        assertThat(immutableInfo.wasDefaultApplied("new")).isFalse();
    }
}
