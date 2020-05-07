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
package io.dekorate.testing;

import io.dekorate.DekorateException;
import io.dekorate.project.FileProjectFactory;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface WithProject {

  String PROJECT_YML = ".project.yml";

  default Project getProject() {
    Project p =  new FileProjectFactory().create(new File("."));
    return getProject(p.getBuildInfo().getClassOutputDir().resolve(p.getDekorateMetaDir()).resolve(PROJECT_YML).toAbsolutePath().toString());
  }

  default Project getProject(String projectDescriptorPath) {
    System.err.println("Getting project from:" + projectDescriptorPath);
    if (projectDescriptorPath != null) {
      try (InputStream is = new FileInputStream(new File(projectDescriptorPath)))  {
        return Serialization.unmarshal(is, Project.class);
      } catch (IOException e) {
        throw DekorateException.launderThrowable(e);
      }
    }
    throw new IllegalStateException("Expected to find manifest at: "+projectDescriptorPath+"!");
  }
}
