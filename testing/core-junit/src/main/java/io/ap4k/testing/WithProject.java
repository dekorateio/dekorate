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
package io.ap4k.testing;

import io.ap4k.project.FileProjectFactory;
import io.ap4k.project.Project;

import java.io.File;
import java.net.URL;

import static io.ap4k.utils.Urls.toFile;

public interface WithProject {

  default Project getProject(String manifestPath) {
    URL manifestUrl = WithProject.class.getClassLoader().getResource(manifestPath);
    if (manifestUrl != null) {
      File manifestFile = toFile(manifestUrl);
      return FileProjectFactory.create(manifestFile);
    }
    throw new IllegalStateException("Expected to find manifest at: "+manifestPath+"!");
  }
}
