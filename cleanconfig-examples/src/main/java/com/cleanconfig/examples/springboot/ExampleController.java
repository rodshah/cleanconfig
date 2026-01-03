package com.cleanconfig.examples.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller demonstrating how to use validated properties.
 *
 * <p>Properties can be accessed in two ways:
 * <ol>
 *   <li>Using {@code @Value} annotation - Spring's standard approach</li>
 *   <li>Using {@link Environment} - programmatic access</li>
 * </ol>
 *
 * <p>CleanConfig ensures these properties are validated before the application starts,
 * so you can be confident they meet your validation rules.
 */
@RestController
@RequestMapping("/api")
public class ExampleController {

    private final Environment environment;

    // Inject validated properties using @Value
    @Value("${app.name}")
    private String appName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.database.max-connections}")
    private int maxConnections;

    public ExampleController(Environment environment) {
        this.environment = environment;
    }

    /**
     * Returns current application configuration.
     * All values have been validated by CleanConfig on startup.
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();

        // Using @Value injected fields
        config.put("appName", appName);
        config.put("adminEmail", adminEmail);
        config.put("maxConnections", maxConnections);

        // Using Environment for programmatic access
        config.put("serverPort", environment.getProperty("server.port"));
        config.put("databaseUrl", environment.getProperty("app.database.url"));
        config.put("apiKey", maskApiKey(environment.getProperty("app.api.key")));

        return config;
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", appName);
        return response;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
