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

public class BuildInfo {

  public static String DEFAULT_PACKAGING = "jar";


  private String name;
  private String version;
  private String packaging;
  private Path outputFile;

  public BuildInfo() {
  }

  public BuildInfo(String name, String version, String packaging, Path outputFile) {
    this.name = name;
    this.version = version;
    this.packaging = packaging;
    this.outputFile = outputFile;
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
   * Get the output file name.
   * @return  The output file name.
   */
  public Path getOutputFile() {
    return outputFile;
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
}
