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
 * 
 **/

package io.ap4k.project;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sun.tools.javac.resources.version;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.Collections;

import io.ap4k.utils.Strings;
import io.ap4k.Ap4kException;

public class GradleInfoReader implements BuildInfoReader {

  private static final String BUILD_GRADLE = "build.gradle";
  private static final String GRADLE_PROPERTIES = "gradle.properties";
  private static final String BUILD = "build";
  private static final String LIBS = "libs";

  private static final String OPEN_BRACKET = "{";
  private static final String CLOSE_BRACKET = "}";
  private static final String QUOTE = "'";

  private static final String EQUALS = "=";
  private static final String DASH = "-";
  private static final String DOT = ".";

  private static final String BASENAME = "baseName";
  private static final String NAME = "name";
  private static final String VERSION = "version";
  private static final String CLASSIFIER = "classifier";
  private static final String EXTENSION = "extension";
  private static final String DESTINATION_DIR = "DESTINATION_DIR";

  private static final String JAR = "jar";

  @Override
  public int order() {
    return 200;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(BUILD_GRADLE).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Path gradlePath = root.resolve(BUILD_GRADLE);
    Path gradlePropertiesPath = root.resolve(GRADLE_PROPERTIES);

    Map<String, String> properties = new HashMap<>();
    properties.putAll(readGradleProperties(gradlePropertiesPath));
    properties.putAll(readBuildGradle(gradlePath));

    String name = properties.getOrDefault(NAME, properties.getOrDefault(BASENAME, root.getFileName().toString()));
    String version = properties.get(VERSION);
    String classifier = properties.get(CLASSIFIER);
    String extension = properties.getOrDefault(EXTENSION, JAR);
    String destinationDir = properties.getOrDefault(DESTINATION_DIR, BUILD + File.separator + LIBS + File.separator);

    Path outputDir = root.resolve(destinationDir);

    StringBuilder sb = new StringBuilder();
    sb.append(outputDir.toAbsolutePath().toString());
    sb.append(name);
    if (Strings.isNotNullOrEmpty(version)) {
      sb.append(DASH).append(version);
    }

    if (Strings.isNotNullOrEmpty(classifier)) {
      sb.append(DASH).append(classifier);
    }
    sb.append(DOT).append(extension);

    String outputFileName = sb.toString();

    return new BuildInfo(name, version, extension, outputFileName);
  }

  /**
   * Parse build.gradle and read the jar configuration as a {@link Map}.
   * @return A map containing all configuration found under jar.
   */
  protected static Map<String, String> readBuildGradle(Path gradlePath) {
    AtomicBoolean inJar = new AtomicBoolean();
    AtomicInteger quotes = new AtomicInteger(0);
    Map<String, String> properties = new HashMap<>();
    try {
      Files.lines(gradlePath).map(l -> l.replaceAll("[ ]*","")).forEach(l ->  {
          if (l.startsWith(JAR)) {
            inJar.set(true);
          }
          if (inJar.get() && l.contains(OPEN_BRACKET)) {
            quotes.incrementAndGet();
          }
          if (inJar.get() && l.contains(CLOSE_BRACKET)) {
            quotes.decrementAndGet();
          }
          if (inJar.get() && quotes.get() == 1 && l.contains(EQUALS)) {
            String key = l.substring(0 ,l.lastIndexOf(EQUALS));
            String value = l.substring(l.lastIndexOf(EQUALS) + 1).replaceAll(QUOTE, "");
            properties.put(key, value);
          }
        });
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
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
