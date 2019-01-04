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

public class Project {

  private static String DEFAULT_GENERATOR_OUTPUT_PATH = "META-INF/ap4k";
  private Path root;
  private String resourceInputPath;
  private String resourceOutputPath;
  private BuildInfo buildInfo;

  public Project() {
  }

  public Project(Path root, BuildInfo buildInfo) {
    this(root, null, DEFAULT_GENERATOR_OUTPUT_PATH, buildInfo);
  }

  public Project(Path root, String resourceInputPath, String resourceOutputPath, BuildInfo buildInfo) {
    this.root = root;
    this.resourceInputPath = resourceInputPath;
    this.resourceOutputPath = resourceOutputPath;
    this.buildInfo = buildInfo;
  }

  public Path getRoot() {
    return root;
  }

  public BuildInfo getBuildInfo() {
    return buildInfo;
  }

  public String getResourceInputPath() {
    return resourceInputPath;
  }

  public String getResourceOutputPath() {
    return resourceOutputPath;
  }


  public Project withResourceInputPath(String resourceInputPath) {
   return new Project(root, resourceInputPath, resourceOutputPath, buildInfo);
  }

  public Project withResourceOutputPath(String resourceOutputPath) {
    return new Project(root, resourceInputPath, resourceOutputPath, buildInfo);
  }
}
