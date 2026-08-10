package io.dekorate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;

public class SerializationThreadContextClassLoaderConflictTest {

  @Test
  public void testCreateJsonMapperWithIsolatedTCCLDoesNotThrowServiceConfigurationError() throws Exception {
    // Given - Create isolated TCCL simulating Gradle's Jackson on TCCL
    String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
    URL[] classpathUrls = new URL[classpathEntries.length];
    for (int i = 0; i < classpathEntries.length; i++) {
      classpathUrls[i] = new File(classpathEntries[i]).toURI().toURL();
    }

    ClassLoader isolatedTCCL = new URLClassLoader(classpathUrls, null);
    ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();

    String[] enabledFeatures = new String[]{"INDENT_OUTPUT"};
    String[] disabledFeatures = new String[]{"FAIL_ON_EMPTY_BEANS", "WRITE_DATES_AS_TIMESTAMPS"};

    try {
      // When - Set isolated TCCL
      Thread.currentThread().setContextClassLoader(isolatedTCCL);

      // Create ObjectMapper - should NOT throw ServiceConfigurationError
      ObjectMapper mapper = Serialization.createJsonMapper(enabledFeatures, disabledFeatures);

      // Then - Verify mapper works
      Assertions.assertThat(mapper).isNotNull();

      // Verify JavaTimeModule is registered (JSR310) by testing LocalDateTime serialization
      LocalDateTime dateTime = LocalDateTime.of(2025, 10, 12, 10, 30, 0);
      String json = mapper.writeValueAsString(dateTime);

      Assertions.assertThat(json)
        .contains("2025")
        .contains("10")
        .describedAs("LocalDateTime should serialize correctly with JavaTimeModule from project classloader");

    } finally {
      Thread.currentThread().setContextClassLoader(originalTCCL);
    }
  }

  @Test
  public void testUsesProjectClassLoaderNotTCCL() throws Exception {
    // Given
    String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
    URL[] classpathUrls = new URL[classpathEntries.length];
    for (int i = 0; i < classpathEntries.length; i++) {
      classpathUrls[i] = new File(classpathEntries[i]).toURI().toURL();
    }

    ClassLoader isolatedTCCL = new URLClassLoader(classpathUrls, null);
    ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(isolatedTCCL);

      // When
      String[] empty = new String[]{};
      ObjectMapper mapper = Serialization.createJsonMapper(empty, empty);

      // Then - Basic serialization works
      Assertions.assertThat(mapper).isNotNull();
      Assertions.assertThat(mapper.writeValueAsString("test")).isEqualTo("\"test\"");
      Assertions.assertThat(mapper.writeValueAsString(123)).isEqualTo("123");

    } finally {
      Thread.currentThread().setContextClassLoader(originalTCCL);
    }
  }

  @Test
  public void testNoServiceConfigurationErrorWithDifferentTCCL() throws Exception {
    // Given - This is the main test that reproduces the original bug scenario:
    String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
    URL[] classpathUrls = new URL[classpathEntries.length];
    for (int i = 0; i < classpathEntries.length; i++) {
      classpathUrls[i] = new File(classpathEntries[i]).toURI().toURL();
    }

    ClassLoader isolatedTCCL = new URLClassLoader(classpathUrls, null);
    ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(isolatedTCCL);

      // When - this should NOT throw ServiceConfigurationError
      String[] empty = new String[]{};
      ObjectMapper mapper = Serialization.createJsonMapper(empty, empty);
      // Then - No ServiceConfigurationError was thrown
      Assertions.assertThat(mapper).isNotNull();
      Assertions.assertThat(mapper.getRegisteredModuleIds()).isNotEmpty();

    } finally {
      Thread.currentThread().setContextClassLoader(originalTCCL);
    }
  }

  @Test
  public void testJavaTimeModuleRegisteredFromProjectClassLoader() throws Exception {
    // Given
    String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
    URL[] classpathUrls = new URL[classpathEntries.length];
    for (int i = 0; i < classpathEntries.length; i++) {
      classpathUrls[i] = new File(classpathEntries[i]).toURI().toURL();
    }

    ClassLoader isolatedTCCL = new URLClassLoader(classpathUrls, null);
    ClassLoader originalTCCL = Thread.currentThread().getContextClassLoader();

    try {
      Thread.currentThread().setContextClassLoader(isolatedTCCL);

      // When
      String[] disabled = new String[]{"WRITE_DATES_AS_TIMESTAMPS"};
      ObjectMapper mapper = Serialization.createJsonMapper(new String[]{}, disabled);

      // Then - JavaTimeModule should be registered from project CL, not TCCL
      LocalDateTime now = LocalDateTime.of(2025, 10, 12, 15, 30);
      String json = mapper.writeValueAsString(now);

      Assertions.assertThat(json)
        .isNotNull()
        .contains("2025-10-12");

    } finally {
      Thread.currentThread().setContextClassLoader(originalTCCL);
    }
  }
}
