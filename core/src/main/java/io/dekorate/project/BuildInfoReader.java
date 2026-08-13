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

public interface BuildInfoReader {

  String NAME = "name";
  String VERSION = "version";
  String CLASSIFIER = "classifier";
  String EXTENSION = "extension";
  String DESTINATION_DIR = "DESTINATION_DIR";

  String JAR = "jar";

  /**
   * The order the reader will be applied.
   * Info readers will be sorted in ascending order.
   * 
   * @return The order.
   */
  int order();

  /**
   * Checks if the reader can be applied to the current project.
   * 
   * @param root The project root.
   * @return Returns true if applicable.
   */
  boolean isApplicable(Path root);

  /**
   * Reads all related project info from filesystem.
   * 
   * @param root The project root.
   * @return The {@link BuildInfo} instance.
   */
  BuildInfo getInfo(Path root);

}
