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

import io.ap4k.deps.jackson.core.type.TypeReference;
import io.ap4k.utils.Serialization;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Project {

  public static String DEFAULT_AP4K_OUTPUT_DIR = "META-INF/ap4k";

  private Path root;
  private String ap4kInputDir;
  private String ap4kOutputDir;
  private BuildInfo buildInfo;

  public Project() {
  }

  public Project(Path root, BuildInfo buildInfo) {
    this(root, null, DEFAULT_AP4K_OUTPUT_DIR, buildInfo);
  }

  public Project(Path root, String ap4kInputDir, String ap4kOutputDir, BuildInfo buildInfo) {
    this.root = root;
    this.ap4kInputDir = ap4kInputDir;
    this.ap4kOutputDir = ap4kOutputDir;
    this.buildInfo = buildInfo;
  }

  public Path getRoot() {
    return root;
  }

  public BuildInfo getBuildInfo() {
    return buildInfo;
  }

  public String getAp4kInputDir() {
    return ap4kInputDir;
  }

  public String getAp4kOutputDir() {
    return ap4kOutputDir;
  }

  public Project withAp4kInputDir(String ap4kInputDir) {
   return new Project(root, ap4kInputDir, ap4kOutputDir, buildInfo);
  }

  public Project withAp4kOutputDir(String ap4kOutputDir) {
    return new Project(root, ap4kInputDir, ap4kOutputDir, buildInfo);
  }

  public Map<String, Object> parseAppResourceFile(String resourceName) throws IOException {
    final Path path = getBuildInfo().getApplicationResourceOutputDir().resolve(resourceName);
    if (!path.toFile().exists()) {
      throw new IllegalArgumentException("No application resources file found at: " + path.toFile().getAbsolutePath());
    }

    if (resourceName.endsWith(".properties")) {
      return Serialization.propertiesMapper().readValue(new FileInputStream(path.toFile().getAbsoluteFile()), new TypeReference<Map<String, Object>>(){});
    } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
      return Serialization.yamlMapper().readValue(new FileInputStream(path.toFile().getAbsoluteFile()), new TypeReference<Map<String, Object>>(){});
    } else {
      throw new IllegalArgumentException("resource type is not supported");
    }
  }
}
