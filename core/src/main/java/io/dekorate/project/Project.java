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

import java.nio.file.Path;

import io.sundr.builder.annotations.Buildable;

public class Project {

  private static String DEFAULT_DEKORATE_OUTPUT_DIR = "META-INF/dekorate";
  private static String DEFAULT_DEKORATE_META_DIR = "../.dekorate";

  private final Path root;
  private final String dekorateInputDir;
  private final String dekorateOutputDir;
  private final String dekorateMetaDir;
  private final BuildInfo buildInfo;
  private final ScmInfo scmInfo;

  public Project() {
    this(null, null, null);
  }

  public Project(Path root, BuildInfo buildInfo, ScmInfo scmInfo) {
    this(root, null, DEFAULT_DEKORATE_META_DIR, DEFAULT_DEKORATE_OUTPUT_DIR, buildInfo, scmInfo);
  }

  public Project(Path root, String dekorateInputDir, String dekorateMetaDir, String dekorateOutputDir, BuildInfo buildInfo) {
    this(root, dekorateInputDir, dekorateMetaDir, dekorateOutputDir, buildInfo, null);
  }

  @Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
  public Project(Path root, String dekorateInputDir, String dekorateMetaDir, String dekorateOutputDir, BuildInfo buildInfo,
      ScmInfo scmInfo) {
    this.root = root;
    this.dekorateInputDir = dekorateInputDir;
    this.dekorateMetaDir = dekorateMetaDir;
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

  public String getDekorateMetaDir() {
    return this.dekorateMetaDir;
  }

  public String getDekorateOutputDir() {
    return dekorateOutputDir;
  }

  public Project withDekorateInputDir(String dekorateInputDir) {
    return new Project(root, dekorateInputDir, dekorateMetaDir, dekorateOutputDir, buildInfo);
  }

  public Project withDekorateMetaDir(String dekorateMetaDir) {
    return new Project(root, dekorateInputDir, dekorateMetaDir, dekorateOutputDir, buildInfo);
  }

  public Project withDekorateOutputDir(String dekorateOutputDir) {
    return new Project(root, dekorateInputDir, dekorateMetaDir, dekorateOutputDir, buildInfo);
  }

  public ScmInfo getScmInfo() {
    return scmInfo;
  }

}
