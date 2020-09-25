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

public class BuildInfo {

  public static String DEFAULT_PACKAGING = "jar";
  public static String DEFAULT_BUILD_TOOL = "generic";

  private final String name;
  private final String version;
  private final String packaging;
  private final String buildTool;
  private final String buildToolVersion;
  private final Path outputFile;
  private final Path classOutputDir;
  private final Path resourceDir;

  public BuildInfo() {
    this(null, null, DEFAULT_PACKAGING, DEFAULT_BUILD_TOOL, null, null, null, null);
  }

  /**
   * Constructor
   * 
   * @param name The project name (e.g. maven artifactId).
   * @param version The project version (e.g. maven version).
   * @param packaging The project packaging (e.g. jar, war).
   * @param buildTool The project build tool (e.g. maven, gralde, sbt)
   * @param buildToolVersion The build tool version
   * @param outputFile The output file (the path to the actual jar, war etc).
   * @param classOutputDir The resource output directory (e.g. target/classes, build/classes/main/java etc).
   * @param resourceDir The directory from which application resources should be read. (e.g. target/classes for maven,
   *        src/main/resources from gralde and so on).
   */
  @Buildable(builderPackage = "io.dekorate.deps.kubernetes.api.builder")
  public BuildInfo(String name, String version, String packaging, String buildTool, String buildToolVersion, Path outputFile, Path classOutputDir, Path resourceDir) {
    this.name = name;
    this.version = version;
    this.packaging = packaging;
    this.buildTool = buildTool;
    this.buildToolVersion = buildToolVersion;
    this.outputFile = outputFile;
    this.classOutputDir = classOutputDir;
    this.resourceDir = resourceDir;
  }

  public BuildInfoBuilder edit() {
    return new BuildInfoBuilder(this);
  }

  /**
   * Get the project name.
   * @return The project name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the project version.
   * @return The project version.
   */
  public String getVersion() {
    return version;
  }


  /**
   * Get the project packaging.
   * @return The project packaging.
   */
  public String getPackaging() {
    return packaging;
  }

  /**
   * Get the build tool name.
   * @return the name of the build tool.
   */
  public String getBuildTool() {
    return buildTool;
  }


  /*
   * Get the build tool version.
   * @return the version of the build tool.
   */
  public String getBuildToolVersion() {
    return this.buildToolVersion;
  }


  /**
   * Get the output file name.
   * @return  The output file name.
   */
  public Path getOutputFile() {
    return outputFile;
  }

  public Path getClassOutputDir() {
    return classOutputDir;
  }

  public Path getResourceDir() {
    return resourceDir;
  }

}
