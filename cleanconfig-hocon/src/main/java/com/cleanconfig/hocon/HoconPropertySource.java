package com.cleanconfig.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

import java.util.Map;
import java.util.Objects;

/**
 * Main entry point for loading HOCON configuration and converting it to a flat
 * {@code Map<String, String>} suitable for use with CleanConfig's validation pipeline.
 *
 * <p>All methods resolve HOCON substitutions (e.g., {@code ${variable}} references)
 * before flattening, and return unmodifiable maps.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Load default application.conf from classpath
 * Map<String, String> props = HoconPropertySource.load();
 *
 * // Load a specific resource
 * Map<String, String> props = HoconPropertySource.load("my-config.conf");
 *
 * // Parse from a string
 * Map<String, String> props = HoconPropertySource.loadFromString("server.port = 8080");
 * }</pre>
 *
 * <p>This class is stateless and all methods are static. It cannot be instantiated.</p>
 *
 * @since 0.1.0
 */
public final class HoconPropertySource {

    private HoconPropertySource() {
        // Utility class — prevent instantiation
    }

    /**
     * Loads the default {@code application.conf} from the classpath using
     * {@link ConfigFactory#load()} and returns a flattened, unmodifiable map.
     *
     * @return an unmodifiable map of dot-separated keys to string values
     * @throws HoconLoadException if parsing or resolution fails
     */
    public static Map<String, String> load() {
        try {
            final Config config = ConfigFactory.load();
            return HoconFlattener.flatten(config);
        } catch (ConfigException e) {
            throw new HoconLoadException(
                    "Failed to load default application.conf from classpath: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a specific HOCON resource from the classpath and returns a flattened,
     * unmodifiable map.
     *
     * @param resourcePath the classpath resource path (e.g., "my-config.conf")
     * @return an unmodifiable map of dot-separated keys to string values
     * @throws NullPointerException if resourcePath is null
     * @throws HoconLoadException if the resource is not found, or parsing/resolution fails
     */
    public static Map<String, String> load(String resourcePath) {
        Objects.requireNonNull(resourcePath, "Resource path must not be null");

        try {
            final Config config = ConfigFactory
                    .parseResources(resourcePath, ConfigParseOptions.defaults())
                    .resolve(ConfigResolveOptions.defaults());

            if (config.isEmpty()) {
                throw new HoconLoadException(
                        "HOCON resource not found or empty on classpath: " + resourcePath);
            }

            return HoconFlattener.flatten(config);
        } catch (HoconLoadException e) {
            throw e;
        } catch (ConfigException e) {
            throw new HoconLoadException(
                    "Failed to load HOCON resource '" + resourcePath + "': " + e.getMessage(), e);
        }
    }

    /**
     * Parses HOCON content from a string and returns a flattened, unmodifiable map.
     *
     * @param hoconContent the HOCON content string to parse
     * @return an unmodifiable map of dot-separated keys to string values
     * @throws NullPointerException if hoconContent is null
     * @throws HoconLoadException if parsing or resolution fails
     */
    public static Map<String, String> loadFromString(String hoconContent) {
        Objects.requireNonNull(hoconContent, "HOCON content must not be null");

        try {
            final Config config = ConfigFactory.parseString(hoconContent)
                    .resolve(ConfigResolveOptions.defaults());
            return HoconFlattener.flatten(config);
        } catch (ConfigException e) {
            throw new HoconLoadException(
                    "Failed to parse HOCON content from string: " + e.getMessage(), e);
        }
    }
}
