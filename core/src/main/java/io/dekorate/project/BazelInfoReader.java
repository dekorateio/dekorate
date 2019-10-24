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

import io.dekorate.DekorateException;
import io.dekorate.utils.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BazelInfoReader implements BuildInfoReader {

  private static final String BAZEL = "bazel";
  private static final String BUILD = "BUILD";

  private static final String OPEN_BRACKET = "{";
  private static final String CLOSE_BRACKET = "}";
  private static final String DOUBLE_QUOTE = "\"";

  private static final String EQUALS = "=";
  private static final String DASH = "-";
  private static final String DOT = ".";

  private static final String JAVA_BINARY = "java_binary";
  private static final String NAME = "name";

  private static final Pattern NAME_AND_VERSION = Pattern.compile("(?<name>[^ ]+)-(?<version>[0-9\\.]+)");


  private static final String BAZEL_BIN = "bazel-bin";
  private static final String BAZEL_OUT = "bazel-out";

  @Override
  public int order() {
    return 300;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(BUILD).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Path build = root.resolve(BUILD);

    Map<String, String> properties = new HashMap<>();
    properties.putAll(readBuild(build));

    String name = properties.getOrDefault(NAME, properties.getOrDefault(NAME, root.getFileName().toString()));
    String version = properties.get(VERSION);
    String extension = properties.getOrDefault(EXTENSION, JAR);
    String classifier = properties.get(CLASSIFIER);

    String destinationDir = properties.getOrDefault(DESTINATION_DIR, BAZEL_BIN);

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

    return new BuildInfo(name, version, extension, BAZEL, outputDir.resolve(sb.toString()),
      root.resolve(BAZEL_OUT)
    );
  }


  /**
   * Parse BUILD and read the jar configuration as a {@link Map}.
   * @param path  The path to BUILD.
   * @return A map containing all configuration found under jar.
   */
  protected static Map<String, String> readBuild(Path path) {
    AtomicBoolean inJavaBinary = new AtomicBoolean();
    AtomicInteger quotes = new AtomicInteger(0);
    Map<String, String> properties = new HashMap<>();
    try {
      Files.lines(path).map(l -> l.replaceAll("[ ]*","")).forEach(l ->  {
        if (l.startsWith(JAVA_BINARY)) {
          inJavaBinary.set(true);
        }
        if (l.contains(OPEN_BRACKET)) {
          quotes.incrementAndGet();
        }
        if (l.contains(CLOSE_BRACKET)) {
          quotes.decrementAndGet();
        }
        if (quotes.get() == 0) {
          inJavaBinary.set(false);
        }

        if ((inJavaBinary.get() || quotes.get() == 0) && l.contains(EQUALS)) {
          String key = l.substring(0 ,l.lastIndexOf(EQUALS));
          String value = l.substring(l.lastIndexOf(EQUALS) + 1).replaceAll(DOUBLE_QUOTE, "").replaceAll(",$", "");
          Matcher matcher = NAME_AND_VERSION.matcher(value);
          if (key.equals(NAME) && matcher.matches()) {
            properties.put(NAME, matcher.group(NAME));
            properties.put(VERSION, matcher.group(VERSION));
          } else {
            properties.put(key, value);
          }
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
    return properties;
  }
}
