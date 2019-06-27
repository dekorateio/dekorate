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
package io.ap4k.project;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class AptProjectFactory {

  private static Project PROJECT = null;

  /**
   * Creates a {@link Project} form the specified {@link ProcessingEnvironment}.
   * @param environment   The environment.
   * @return              The project.
   */
  public static Project create(ProcessingEnvironment environment) {
    if (PROJECT != null) {
      return PROJECT;
    }
    synchronized (AptProjectFactory.class) {
      if (PROJECT == null) {
        PROJECT = createInternal(environment);
      }
    }
    return PROJECT;
  }

  private static Project createInternal(ProcessingEnvironment environment) {
    FileObject f = null;
    try {
      f = environment.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", ".marker-" + UUID.randomUUID().toString());
      return FileProjectFactory.create(Paths.get(f.toUri()).toFile());
    } catch (IOException e) {
      throw new RuntimeException("Failed to determine the project root!", e);
    } finally {
      if (f != null) {
        try {
          f.delete();
        } catch (Exception e) {
          //Some environments do not support deleting FileObjects. Eclipse is such an environment.
          //So let's ignore.
        }
      }
    }
  }
}
