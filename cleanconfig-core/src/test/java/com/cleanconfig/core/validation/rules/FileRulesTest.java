package com.cleanconfig.core.validation.rules;

import com.cleanconfig.core.PropertyContext;
import com.cleanconfig.core.ValidationContextType;
import com.cleanconfig.core.validation.ValidationResult;
import com.cleanconfig.core.validation.ValidationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileRules}.
 */
public class FileRulesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private PropertyContext context;

    @Before
    public void setUp() {
        context = new TestPropertyContext();
    }

    // exists() tests
    @Test
    public void exists_ExistingFile_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.exists();
        assertThat(rule.validate("path", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void exists_NonExistingFile_ReturnsFailure() {
        ValidationRule<String> rule = FileRules.exists();
        ValidationResult result = rule.validate("path", "/non/existing/path", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("does not exist");
    }

    // fileExists() tests
    @Test
    public void fileExists_ExistingFile_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.fileExists();
        assertThat(rule.validate("path", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    // directoryExists() tests
    @Test
    public void directoryExists_ExistingDirectory_ReturnsSuccess() {
        File tempDir = tempFolder.getRoot();
        ValidationRule<String> rule = FileRules.directoryExists();
        assertThat(rule.validate("path", tempDir.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void directoryExists_NonExistingDirectory_ReturnsFailure() {
        ValidationRule<String> rule = FileRules.directoryExists();
        ValidationResult result = rule.validate("path", "/non/existing/dir", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("does not exist");
    }

    @Test
    public void directoryExists_FileInsteadOfDirectory_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.directoryExists();
        ValidationResult result = rule.validate("path", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not a directory");
    }

    // readable() tests
    @Test
    public void readable_ReadableFile_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.readable();
        assertThat(rule.validate("path", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    // writable() tests
    @Test
    public void writable_WritableFile_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.writable();
        assertThat(rule.validate("path", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    // isDirectory() tests
    @Test
    public void isDirectory_Directory_ReturnsSuccess() {
        File tempDir = tempFolder.getRoot();
        ValidationRule<String> rule = FileRules.isDirectory();
        assertThat(rule.validate("path", tempDir.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void isDirectory_RegularFile_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.isDirectory();
        ValidationResult result = rule.validate("path", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not a directory");
    }

    // isFile() tests
    @Test
    public void isFile_RegularFile_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.isFile();
        assertThat(rule.validate("path", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void isFile_Directory_ReturnsFailure() {
        File tempDir = tempFolder.getRoot();
        ValidationRule<String> rule = FileRules.isFile();
        ValidationResult result = rule.validate("path", tempDir.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not a regular file");
    }

    // isEmptyDirectory() tests
    @Test
    public void isEmptyDirectory_EmptyDirectory_ReturnsSuccess() throws IOException {
        File emptyDir = tempFolder.newFolder("empty");
        ValidationRule<String> rule = FileRules.isEmptyDirectory();
        assertThat(rule.validate("path", emptyDir.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void isEmptyDirectory_NonEmptyDirectory_ReturnsFailure() throws IOException {
        File dir = tempFolder.newFolder("nonempty");
        boolean created = new File(dir, "file.txt").createNewFile();
        assertThat(created).isTrue();
        ValidationRule<String> rule = FileRules.isEmptyDirectory();
        ValidationResult result = rule.validate("path", dir.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not empty");
    }

    @Test
    public void isEmptyDirectory_RegularFile_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.isEmptyDirectory();
        ValidationResult result = rule.validate("path", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("not a directory");
    }

    // hasExtension() tests
    @Test
    public void hasExtension_CorrectExtension_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.hasExtension(".txt");
        assertThat(rule.validate("file", tempFile.getAbsolutePath(), context).isValid()).isTrue();

        // Test without leading dot
        ValidationRule<String> rule2 = FileRules.hasExtension("txt");
        assertThat(rule2.validate("file", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void hasExtension_WrongExtension_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        ValidationRule<String> rule = FileRules.hasExtension(".md");
        ValidationResult result = rule.validate("file", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("must have extension: .md");
    }

    // fileSizeBetween() tests
    @Test
    public void fileSizeBetween_ValidSize_ReturnsSuccess() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        Files.write(tempFile.toPath(), "Hello World".getBytes(StandardCharsets.UTF_8)); // 11 bytes

        ValidationRule<String> rule = FileRules.fileSizeBetween(5, 20);
        assertThat(rule.validate("file", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    @Test
    public void fileSizeBetween_SizeTooSmall_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        Files.write(tempFile.toPath(), "Hi".getBytes(StandardCharsets.UTF_8)); // 2 bytes

        ValidationRule<String> rule = FileRules.fileSizeBetween(5, 20);
        ValidationResult result = rule.validate("file", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("between 5 and 20 bytes");
    }

    @Test
    public void fileSizeBetween_SizeTooLarge_ReturnsFailure() throws IOException {
        File tempFile = tempFolder.newFile("test.txt");
        Files.write(tempFile.toPath(), "This is a very long string".getBytes(StandardCharsets.UTF_8)); // 26 bytes

        ValidationRule<String> rule = FileRules.fileSizeBetween(5, 20);
        ValidationResult result = rule.validate("file", tempFile.getAbsolutePath(), context);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void fileSizeBetween_NonExistentFile_ReturnsFailure() {
        ValidationRule<String> rule = FileRules.fileSizeBetween(5, 20);
        ValidationResult result = rule.validate("file", "/non/existent/file.txt", context);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getErrorMessage()).contains("does not exist");
    }

    // Test composition
    @Test
    public void composition_MultipleRules_Works() throws IOException {
        File tempFile = tempFolder.newFile("config.txt");
        Files.write(tempFile.toPath(), "test".getBytes(StandardCharsets.UTF_8));

        ValidationRule<String> rule = FileRules.fileExists()
                .and(FileRules.readable())
                .and(FileRules.hasExtension(".txt"));

        assertThat(rule.validate("config", tempFile.getAbsolutePath(), context).isValid()).isTrue();
    }

    // Null value tests
    @Test
    public void allRules_NullValue_ReturnsSuccess() {
        // All file rules should pass for null values
        assertThat(FileRules.exists().validate("v", null, context).isValid()).isTrue();
        assertThat(FileRules.readable().validate("v", null, context).isValid()).isTrue();
        assertThat(FileRules.isDirectory().validate("v", null, context).isValid()).isTrue();
    }

    // Simple test context implementation
    private static class TestPropertyContext implements PropertyContext {
        private final Map<String, String> properties = new HashMap<>();

        @Override
        public Optional<String> getProperty(String propertyName) {
            return Optional.ofNullable(properties.get(propertyName));
        }

        @Override
        public <T> Optional<T> getTypedProperty(String propertyName, Class<T> targetType) {
            return Optional.empty();
        }

        @Override
        public Map<String, String> getAllProperties() {
            return Collections.unmodifiableMap(properties);
        }

        @Override
        public ValidationContextType getContextType() {
            return ValidationContextType.STARTUP;
        }

        @Override
        public Optional<String> getMetadata(String key) {
            return Optional.empty();
        }

        @Override
        public boolean hasProperty(String propertyName) {
            return properties.containsKey(propertyName);
        }
    }
}
