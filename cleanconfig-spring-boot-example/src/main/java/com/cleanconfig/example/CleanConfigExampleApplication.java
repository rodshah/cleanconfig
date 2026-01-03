package com.cleanconfig.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example Spring Boot application demonstrating CleanConfig integration.
 *
 * <p>This application shows how to:
 * <ul>
 *   <li>Use the CleanConfig Spring Boot Starter</li>
 *   <li>Define property definitions as Spring beans</li>
 *   <li>Validate properties on application startup</li>
 *   <li>Use validated configuration in your application</li>
 * </ul>
 */
@SpringBootApplication
public class CleanConfigExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanConfigExampleApplication.class, args);
    }
}
