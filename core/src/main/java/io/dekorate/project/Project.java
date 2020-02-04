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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import io.dekorate.deps.jackson.core.type.TypeReference;
import io.dekorate.deps.jackson.databind.ObjectMapper;
import io.dekorate.utils.Serialization;

import io.sundr.builder.annotations.Buildable;

public class Project {

  private static String DEFAULT_DEKORATE_OUTPUT_DIR = "META-INF/dekorate";

  private final Path root;
  private final String dekorateInputDir;
  private final String dekorateOutputDir;
  private final BuildInfo buildInfo;
  private final ScmInfo scmInfo;

  public Project() {
    this(null, null, null);
  }

  public Project(Path root, BuildInfo buildInfo, ScmInfo scmInfo) {
    this(root, null, DEFAULT_DEKORATE_OUTPUT_DIR, buildInfo, scmInfo);
  }

  public Project(Path root, String dekorateInputDir, String dekorateOutputDir, BuildInfo buildInfo) {
    this(root, dekorateInputDir, dekorateOutputDir, buildInfo, null);
  }

  @Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
  public Project(Path root, String dekorateInputDir, String dekorateOutputDir, BuildInfo buildInfo, ScmInfo scmInfo) {
    this.root = root;
    this.dekorateInputDir = dekorateInputDir;
    this.dekorateOutputDir = dekorateOutputDir;
    this.buildInfo = buildInfo;
    this.scmInfo = scmInfo;
  }

  public ProjectBuilder edit() {
    return new ProjectBuilder(this);
  }

  public Path getRoot() {
    return root;
  }

  public BuildInfo getBuildInfo() {
    return buildInfo;
  }

  public String getDekorateInputDir() {
    return dekorateInputDir;
  }

  public String getDekorateOutputDir() {
    return dekorateOutputDir;
  }

  public Project withDekorateInputDir(String dekorateInputDir) {
   return new Project(root, dekorateInputDir, dekorateOutputDir, buildInfo);
  }

  public Project withDekorateOutputDir(String dekorateOutputDir) {
    return new Project(root, dekorateInputDir, dekorateOutputDir, buildInfo);
  }

  public Map<String, Object> parseResourceFile(String resourceName) {
    final Path path = getBuildInfo().getResourceDir().resolve(resourceName);
    if (!path.toFile().exists()) {
      return new HashMap<>();
    }

    if (resourceName.endsWith(".properties")) {
      return parse(path, Serialization.propertiesMapper());
    } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
      return parse(path, Serialization.yamlMapper());
    } else {
      throw new IllegalArgumentException("resource type is not supported");
    }
  }

  private Map<String, Object> parse(Path path, ObjectMapper javaPropsMapper) {
    try {
      return javaPropsMapper.readValue(new FileInputStream(path.toFile().getAbsoluteFile()), new TypeReference<Map<String, Object>>() {});
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ScmInfo getScmInfo() {
    return scmInfo;
  }

}
