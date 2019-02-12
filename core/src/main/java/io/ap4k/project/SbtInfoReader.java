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

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import io.ap4k.Ap4kException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import java.util.Arrays;

public class SbtInfoReader implements BuildInfoReader {

  private static final String BUILD_SBT = "build.sbt";

  private static final String NAME = "name";
  private static final String VERSION = "version";
  private static final String SCALA_VERSION = "scalaVersion";
  private static final String SET = ":=";
  private static final String TARGET = "target";
  private static final String CLASSES = "classes";

  private static final String DOUBLE_QUOTE = "\"";
  private static final String DASH = "-";
  private static final String DOT = ".";
  private static final String UNDERSCORE = "_";
  private static final String NEWLINE = "\n";


  protected static final String JAR = "jar";

  protected static final String DEFAULT_VERSION = "0.1.0-SNAPSHOT";
  protected static final String DEFAULT_SCALA_VERSION = "2.12";

  private static final String[] SCALA_VERSION_CMD = new String[]{"scala", "-version"};
  private static final String VERSION_PATTERN = "^(\\d+\\.\\d+\\.\\d+).*";

  private static final int MAJOR = 0;
  private static final int MINOR = 1;

  @Override
  public int order() {
    return 400;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(BUILD_SBT).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Path gradlePath = root.resolve(BUILD_SBT);
    Map<String, String> properties = new HashMap<>();

    try {
      Files.lines(gradlePath).map(l -> l.replaceAll("[ ]*","")).filter(l -> l.contains(SET)).forEach(l ->  {
          String key = l.substring(0, l.lastIndexOf(SET));
          String value = l.substring(l.lastIndexOf(SET) + 2).replaceAll(DOUBLE_QUOTE, "");
          properties.put(key, value);
        });
    } catch (IOException e)  {
      throw Ap4kException.launderThrowable(e);
    }

    String name = properties.getOrDefault(NAME, root.getFileName().toString());
    String version = properties.getOrDefault(VERSION, DEFAULT_VERSION);
    String scalaVersion = properties.getOrDefault(SCALA_VERSION, getSystemScalaVersion());
    String extension = JAR;
    Path outputFile = root.resolve(TARGET).resolve(name + UNDERSCORE + scalaVersion + DASH + version + DOT + extension);
    Path resourceOutputDir = root.resolve(TARGET).resolve(CLASSES);
    return new BuildInfo(name, version, JAR, outputFile, resourceOutputDir);
  }

  /**
   * Get the system scala version.
   * @return The scala version found using exec, or fallback to the default version.
   */
  protected static String getSystemScalaVersion() {
    try {
      Process p = new ProcessBuilder()
        .command(SCALA_VERSION_CMD)
        .redirectErrorStream(true)
        .start();
      

      BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String output = buffer.lines().collect(Collectors.joining(NEWLINE));
      String fullVersion = Arrays.stream(output.split(" ")).filter(w -> w.matches(VERSION_PATTERN)).findFirst().orElse(DEFAULT_VERSION);
      String[] version = fullVersion.split("\\.");
      if  (version.length >= 2) {
        return version[MAJOR] + DOT + version[MINOR];
      }
    } catch (IOException e) {
      return DEFAULT_SCALA_VERSION;
    }
    return DEFAULT_VERSION;
  }
}
