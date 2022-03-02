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

package io.dekorate.testing.knative;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import io.dekorate.DekorateException;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;

public interface WithKnativeConfig {

  String CONFIG_DIR = "config";
  String KUBERNETES_YML = "knative.yml";

  default boolean hasKubernetesConfig(Project project) {
    return getKubernetesConfigPath(project).toFile().exists();
  }

  default KnativeConfig getKubernetesConfig(Project project) {
    return getKubernetesConfig(getKubernetesConfigPath(project));
  }

  default Path getKubernetesConfigPath(Project project) {
    return project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateMetaDir()).resolve(CONFIG_DIR)
        .resolve(KUBERNETES_YML);
  }

  default KnativeConfig getKubernetesConfig(Path path) {
    File f = path.toFile();
    if (f.exists()) {
      try (InputStream is = new FileInputStream(f)) {
        return Serialization.unmarshal(is, KnativeConfig.class);
      } catch (IOException e) {
        throw DekorateException.launderThrowable(e);
      }
    }

    throw new IllegalStateException("Expected to find kubernetes config at: " + path + "!");
  }
}
