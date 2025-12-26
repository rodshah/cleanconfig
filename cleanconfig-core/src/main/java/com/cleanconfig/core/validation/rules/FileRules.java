package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.validation.ValidationError;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validation rules for file system paths and files.
 *
 * <p>Provides rules for validating file existence, permissions, and types.
 *
 * <p>Example usage:
 * <pre>
 * ValidationRule&lt;String&gt; configFileRule = FileRules.fileExists()
 *     .and(FileRules.readable());
 *
 * ValidationRule&lt;String&gt; logDirRule = FileRules.directoryExists()
 *     .and(FileRules.writable());
 * </pre>
 *
 * @since 0.1.0
 */
public final class FileRules {

    private FileRules() {
        // Utility class
    }

    /**
     * Validates that a file or directory exists.
     *
     * @return validation rule
     */
    public static ValidationRule<String> exists() {
        return (name, value, context) -> {
            if (value != null) {
                File file = new File(value);
                if (!file.exists()) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path does not exist")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file exists.
     * Alias for {@link #exists()}.
     *
     * @return validation rule
     */
    public static ValidationRule<String> fileExists() {
        return exists();
    }

    /**
     * Validates that a directory exists.
     *
     * @return validation rule
     */
    public static ValidationRule<String> directoryExists() {
        return (name, value, context) -> {
            if (value != null) {
                File file = new File(value);
                if (!file.exists()) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Directory does not exist")
                                    .actualValue(value)
                                    .build()
                    );
                }
                if (!file.isDirectory()) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path exists but is not a directory")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file is readable.
     *
     * @return validation rule
     */
    public static ValidationRule<String> readable() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isReadable(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("File is not readable")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file is writable.
     *
     * @return validation rule
     */
    public static ValidationRule<String> writable() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isWritable(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("File is not writable")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file is executable.
     *
     * @return validation rule
     */
    public static ValidationRule<String> executable() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isExecutable(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("File is not executable")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a path is a directory.
     *
     * @return validation rule
     */
    public static ValidationRule<String> isDirectory() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isDirectory(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path is not a directory")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a path is a regular file.
     *
     * @return validation rule
     */
    public static ValidationRule<String> isFile() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isRegularFile(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path is not a regular file")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a path is a symbolic link.
     *
     * @return validation rule
     */
    public static ValidationRule<String> isSymbolicLink() {
        return (name, value, context) -> {
            if (value != null) {
                Path path = Paths.get(value);
                if (!Files.isSymbolicLink(path)) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path is not a symbolic link")
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a path is hidden.
     *
     * @return validation rule
     */
    public static ValidationRule<String> isHidden() {
        return (name, value, context) -> {
            if (value != null) {
                try {
                    Path path = Paths.get(value);
                    if (!Files.isHidden(path)) {
                        return ValidationResult.failure(
                                ValidationError.builder()
                                        .propertyName(name)
                                        .errorMessage("Path is not hidden")
                                        .actualValue(value)
                                        .build()
                        );
                    }
                } catch (Exception e) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Cannot check if path is hidden: " + e.getMessage())
                                    .actualValue(value)
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a directory is empty (contains no files).
     *
     * @return validation rule
     */
    public static ValidationRule<String> isEmptyDirectory() {
        return (name, value, context) -> {
            if (value != null) {
                File file = new File(value);
                if (!file.isDirectory()) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Path is not a directory")
                                    .actualValue(value)
                                    .build()
                    );
                }
                String[] contents = file.list();
                if (contents != null && contents.length > 0) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("Directory is not empty")
                                    .actualValue(value + " (contains " + contents.length + " items)")
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file has a specific extension.
     *
     * @param extension the required extension (with or without leading dot)
     * @return validation rule
     */
    public static ValidationRule<String> hasExtension(String extension) {
        String ext = extension.startsWith(".") ? extension : "." + extension;
        return (name, value, context) -> {
            if (value != null && !value.endsWith(ext)) {
                return ValidationResult.failure(
                        ValidationError.builder()
                                .propertyName(name)
                                .errorMessage("File must have extension: " + ext)
                                .actualValue(value)
                                .build()
                );
            }
            return ValidationResult.success();
        };
    }

    /**
     * Validates that a file size is within a range (in bytes).
     *
     * @param minBytes minimum size in bytes (inclusive)
     * @param maxBytes maximum size in bytes (inclusive)
     * @return validation rule
     */
    public static ValidationRule<String> fileSizeBetween(long minBytes, long maxBytes) {
        return (name, value, context) -> {
            if (value != null) {
                File file = new File(value);
                if (!file.exists()) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("File does not exist")
                                    .actualValue(value)
                                    .build()
                    );
                }
                long size = file.length();
                if (size < minBytes || size > maxBytes) {
                    return ValidationResult.failure(
                            ValidationError.builder()
                                    .propertyName(name)
                                    .errorMessage("File size must be between " + minBytes + " and " + maxBytes + " bytes")
                                    .actualValue(size + " bytes")
                                    .expectedValue("[" + minBytes + ", " + maxBytes + "] bytes")
                                    .build()
                    );
                }
            }
            return ValidationResult.success();
        };
    }
}
