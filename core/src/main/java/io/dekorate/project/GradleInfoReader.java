/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.project;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Collections;

import io.dekorate.utils.Strings;
import io.dekorate.DekorateException;

public class GradleInfoReader implements BuildInfoReader {

  private static final String BUILD_GRADLE = "build.gradle";
  private static final String SETTINGS_GRADLE = "settings.gradle";
  private static final String GRADLE_PROPERTIES = "gradle.properties";
  private static final String SRC = "src";
  private static final String MAIN = "main";
  private static final String RESOURCES = "resources";

  private static final String BUILD = "build";
  private static final String LIBS = "libs";

  private static final String CLASSES = "classes";
  private static final String GROOVY = "groovy";
  private static final String JAVA = "groovy";

  private static final String OPEN_BRACKET = "{";
  private static final String CLOSE_BRACKET = "}";
  private static final String QUOTE = "'";

  private static final String EQUALS = "=";
  private static final String DASH = "-";
  private static final String DOT = ".";

  private static final String BASENAME = "baseName";

  private static final String SHADOW_JAR = "shadowJar";

  private static final String ROOT_PROJECT_PREFIX = "rootProject.";

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(BUILD_GRADLE).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Path buildGradle = root.resolve(BUILD_GRADLE);
    Path settingsGradle = root.resolve(SETTINGS_GRADLE);
    Path gradleProperties = root.resolve(GRADLE_PROPERTIES);

    Map<String, String> properties = new HashMap<>();
    properties.putAll(readSettingsGradle(settingsGradle));
    properties.putAll(readGradleProperties(gradleProperties));
    properties.putAll(readBuildGradle(buildGradle));

    String name = properties.getOrDefault(NAME, properties.getOrDefault(BASENAME, root.getFileName().toString()));
    String version = properties.get(VERSION);
    String classifier = properties.get(CLASSIFIER);
    String extension = properties.getOrDefault(EXTENSION, JAR);
    String destinationDir = properties.getOrDefault(DESTINATION_DIR, BUILD + File.separator + LIBS + File.separator);

    Path outputDir = root.resolve(destinationDir);

    StringBuilder sb = new StringBuilder();
    sb.append(name);
    if (Strings.isNotNullOrEmpty(version)) {
      sb.append(DASH).append(version);
    }

    if (Strings.isNotNullOrEmpty(classifier)) {
      sb.append(DASH).append(classifier);
    }
    sb.append(DOT).append(extension);

    return new BuildInfo(name, version, extension,
      outputDir.resolve(sb.toString()),
                         //TODO: This need to be smarter and also cover groovy code.
                         root.resolve(BUILD).resolve(CLASSES).resolve(JAVA).resolve(MAIN),
                         root.resolve(SRC).resolve(MAIN).resolve(RESOURCES)
    );
  }

  /**
   * Read settings.gradle and get root project properties.
   * @param path  The path to settings.gralde.
   * @return
   */
  protected static Map<String, String> readSettingsGradle(Path path) {
    Map<String, String> properties = new HashMap<>();
    if (path.toFile().exists()) {
      try {
        Files.lines(path)
          .map(l -> l.replaceAll("[ ]*", ""))
          .forEach(l -> {
            String key = l.substring(0, l.lastIndexOf(EQUALS));
            if (key.startsWith(ROOT_PROJECT_PREFIX)) {
              key = key.substring(ROOT_PROJECT_PREFIX.length());
              String value = l.substring(l.lastIndexOf(EQUALS) + 1).replaceAll(QUOTE, "");
              properties.put(key, value);
            }
          });
      } catch (IOException e) {
        throw DekorateException.launderThrowable(e);
      }
    }
    return properties;
  }

  /**
   * Parse build.gradle and read the jar configuration as a {@link Map}.
   * @param path  The path to build.gralde.
   * @return A map containing all configuration found under jar.
   */
  protected static Map<String, String> readBuildGradle(Path path) {
    AtomicBoolean inJar = new AtomicBoolean();
    AtomicBoolean inShadowJar = new AtomicBoolean();
    AtomicInteger quotes = new AtomicInteger(0);
    Map<String, String> properties = new HashMap<>();
    try {
      Files.lines(path).map(l -> l.replaceAll("[ ]*","")).forEach(l ->  {
        if (l.startsWith(JAR)) {
          inJar.set(true);
        }
        if (l.startsWith(SHADOW_JAR)) {
          inShadowJar.set(true);
          properties.put(CLASSIFIER, "all");
        }
        if (l.contains(OPEN_BRACKET)) {
          quotes.incrementAndGet();
        }
        if (l.contains(CLOSE_BRACKET)) {
          quotes.decrementAndGet();
        }
        if (quotes.get() == 0) {
          inJar.set(false);
          inShadowJar.set(false);
        }

        if ((inShadowJar.get() || inJar.get() || quotes.get() == 0) && l.contains(EQUALS)) {
          String key = l.substring(0 ,l.lastIndexOf(EQUALS));
          String value = l.substring(l.lastIndexOf(EQUALS) + 1).replaceAll(QUOTE, "");
          properties.put(key, value);
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
    return properties;
  }

  /**
   * Parse gradle.properties into {@link Map}.
   * @return A map containing all configuration found it the properties file.
   */
  protected static Map<String, String> readGradleProperties(Path gradlePropertiesPath) {
    Map<String, String> result = new HashMap<>();
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(gradlePropertiesPath.toFile())) {
      properties.load(fis);
      properties.forEach( (k,v) -> result.put(String.valueOf(k), String.valueOf(v)));
      return result;
    } catch (IOException e) {
      return Collections.emptyMap();
    }
  }

}
