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

public class BuildInfo {

  public static String DEFAULT_PACKAGING = "jar";
  public static String DEFAULT_BUILD_TOOL = "generic";

  private String name;
  private String version;
  private String packaging;
  private String buildTool;
  private Path outputFile;
  private Path classOutputDir;
  private Path resourceDir;

  public BuildInfo() {
  }

  /**
   * Constructor
   * @param name                  The project name (e.g. maven artifactId).
   * @param version               The project version (e.g. maven version).
   * @param packaging             The project packaging (e.g. jar, war).
   * @param outputFile            The output file (the path to the actual jar, war etc).
   * @param classOutputDir        The resource output directory (e.g. target/classes, build/classes/main/java etc).
   */
  public BuildInfo(String name, String version, String packaging, String buildTool, Path outputFile, Path classOutputDir) {
    this(name, version, packaging, buildTool, outputFile, classOutputDir, classOutputDir);
  }

  /**
   * Constructor
   * @param name                  The project name (e.g. maven artifactId).
   * @param version               The project version (e.g. maven version).
   * @param packaging             The project packaging (e.g. jar, war).
   * @param outputFile            The output file (the path to the actual jar, war etc).
   * @param classOutputDir        The resource output directory (e.g. target/classes, build/classes/main/java etc).
   * @param resourceDir           The directory from which application resources should be read. (e.g. target/classes for maven, src/main/resources from gralde and so on).
   */
  public BuildInfo(String name, String version, String packaging, String buildTool, Path outputFile, Path classOutputDir, Path resourceDir) {
    this.name = name;
    this.version = version;
    this.packaging = packaging;
    this.buildTool = buildTool;
    this.outputFile = outputFile;
    this.classOutputDir = classOutputDir;
    this.resourceDir = resourceDir;
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

  /**
   * Set the build tool name.
   */
  public void setBuildTool(String buildTool) {
    this.buildTool = buildTool;
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

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setPackaging(String packaging) {
    this.packaging = packaging;
  }

  public void setOutputFile(Path outputFile) {
    this.outputFile = outputFile;
  }

  public void setResourceOutputDir(Path classOutputDir) {
    this.classOutputDir = classOutputDir;
  }

  public void setApplicationResourceOutputDir(Path resourceDir) {
    this.resourceDir = resourceDir;
  }

}
