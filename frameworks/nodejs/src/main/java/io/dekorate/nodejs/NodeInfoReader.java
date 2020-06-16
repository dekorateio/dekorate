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
package io.dekorate.nodejs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import io.dekorate.DekorateException;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.BuildInfoBuilder;
import io.dekorate.project.BuildInfoReader;
import io.dekorate.utils.Exec;
import io.dekorate.utils.Exec.ProjectExec;
import io.dekorate.utils.Git;
import io.dekorate.utils.Strings;

public class NodeInfoReader implements BuildInfoReader {

  private final Logger LOGGER = LoggerFactory.getLogger();

  private static final String NPM = "npm";

  private static final String PACKAGE_JSON = "package.json";

  private static final String SRC = "src";

  private static final String OPEN_BRACKET = "{";
  private static final String CLOSE_BRACKET = "}";
  private static final String QUOTE = "'";

  private static final String EQUALS = "=";
  private static final String COLON = ":";
  private static final String DASH = "-";
  private static final String DOT = ".";

  private static final String NAME = "name";
  private static final String VERSION = "version";
  private static final String BIN = "bin";
  private static final String MAIN = "main";

  public static final String NEW_LINE = "[\\n\\r]+";

  private static final String INDEX_JS = "index.js";


  @Override
  public int order() {
    return 500;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(PACKAGE_JSON).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Path packageJson = root.resolve(PACKAGE_JSON);
    Map<String, String> properties = new HashMap<>();
    properties.putAll(readPackageJson(packageJson));

    String name = properties.getOrDefault(NAME, properties.getOrDefault(NAME, root.getFileName().toString()));
    String version = properties.get(VERSION);
    String main = properties.getOrDefault(MAIN, INDEX_JS);

    StringBuilder sb = new StringBuilder();
    sb.append(name);
    if (Strings.isNotNullOrEmpty(version)) {
      sb.append(DASH).append(version);
    }

    if (version == null) {
      LOGGER.warning("Could not detect project version. Using 'latest'.");
      version = "latest";
    }


    return new BuildInfoBuilder()
      .withName(name)
      .withVersion(version)
      .withPackaging("js")
      .withBuildTool(NPM)
      .withBuildToolVersion(getVersion(root))
      .withOutputFile(root.resolve(main))
      .withClassOutputDir(null)
      .withResourceDir(null)
      .build();
  }

  /**
   * Parse package.json and read the nodejs configuration as a {@link Map}.
   * In detail, it parses:
   * - name
   * - version
   * - main
   * @param path  The path to package.json.
   * @return A map containing all configuration.
   */
  protected static Map<String, String> readPackageJson(Path path) {
    AtomicInteger quotes = new AtomicInteger(0);

    Map<String, String> properties = new HashMap<>();
    try {
      Files.lines(path).map(l -> l.replaceAll("[ ]*","")).forEach(l ->  {
        if (l.contains(OPEN_BRACKET)) {
          quotes.incrementAndGet();
        }
        if (l.contains(CLOSE_BRACKET)) {
          quotes.decrementAndGet();
        }

        if (quotes.get() == 1 && l.contains(NAME)) {
          String name = readValue(l);
          properties.put(NAME, name);
        }

        if (quotes.get() == 1 && l.contains(VERSION)) {
          String version = readValue(l);
          properties.put(VERSION, version);
        }
        if (quotes.get() == 1 && l.contains(MAIN)) {
          String main = readValue(l);
          properties.put(MAIN, main);
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
    return properties;
  }


  private static String readValue(String l) {
    return l.substring(l.lastIndexOf(COLON) + 1)
      .replaceAll(QUOTE, "")
      .replaceAll(Pattern.quote("\""), "")
      .replaceAll(",$", "").trim();
  }

  public static String getVersion(Path modulePath) {
    Path packageJson = Git.getRoot(modulePath).orElse(modulePath).resolve(PACKAGE_JSON);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ProjectExec exec = Exec.inPath(modulePath).redirectingOutput(out);

    boolean success = exec.commands(NPM, VERSION);
    return getVersionFromOutput(new String(out.toByteArray()));
  }

  protected static String getVersionFromOutput(String output) {
    if (Strings.isNullOrEmpty(output)) {
      throw new IllegalArgumentException("nodejs version output should not be empty!");
    }
    return Arrays.stream(output.split(NEW_LINE))
      .filter(l -> l.contains("npm"))
      .map(NodeInfoReader::readValue)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Unknown nodejs version output format. Expected at least one line!"));
  }
}
