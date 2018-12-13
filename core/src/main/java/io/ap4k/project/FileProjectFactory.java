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

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class FileProjectFactory {

  private static Project PROJECT = null;

  /**
   * Creates a {@link Project} form the specified {@link ProcessingEnvironment}.
   * @param file          A file within the project.
   * @return              The project.
   */
  public static Project create(File file) {
    if (PROJECT != null) {
      return PROJECT;
    }
    synchronized (FileProjectFactory.class) {
      if (PROJECT == null) {
        PROJECT = createInternal(file);
      }
    }
    return PROJECT;
  }

  private static Project createInternal(File f) {
    Path path = f.toPath();
    Optional<BuildInfo> info = getProjectInfo(path);
    while (path != null && !info.isPresent()) {
      path = path.getParent();
      info = getProjectInfo(path);
    }
    return new Project(path, info.orElseThrow(() -> new IllegalStateException("Could not find matching project info read")));
  }

  /**
   * Read the {@link BuildInfo} from the specified path.
   * @param path  The path.
   * @return      An {@link Optional} {@link BuildInfo}.
   */
  private static Optional<BuildInfo> getProjectInfo(Path path) {
    if (path == null) {
      return Optional.empty();
    }

    return StreamSupport.stream(ServiceLoader.load(BuildInfoReader.class, FileProjectFactory.class.getClassLoader()).spliterator(), false)
      .filter(r -> r.isApplicable(path))
      .sorted(Comparator.comparingInt(BuildInfoReader::order))
      .findFirst()
      .map(r -> r.getInfo(path));
  }

}
