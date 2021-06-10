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

package io.dekorate.kubernetes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.ResourceRegistry;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.EditableKubernetesConfig;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.kubernetes.manifest.KubernetesManifestGenerator;
import io.dekorate.project.FileProjectFactory;

class KubernetesManifestGeneratorTest {

  @BeforeAll
  public static void setup() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");
    WithProject withProject = new WithProject() {
    };
    withProject.setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
        .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
  }

  @Test
  public void shouldAcceptKubernetesConfig() {
    KubernetesManifestGenerator generator = new KubernetesManifestGenerator(new ResourceRegistry(),
        new ConfigurationRegistry());
    assertTrue(generator.accepts(KubernetesConfig.class));
  }

  @Test
  public void shouldAcceptEditableKubernetesConfig() {
    KubernetesManifestGenerator generator = new KubernetesManifestGenerator(new ResourceRegistry(),
        new ConfigurationRegistry());
    assertTrue(generator.accepts(EditableKubernetesConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfigSubclasses() {
    KubernetesManifestGenerator generator = new KubernetesManifestGenerator(new ResourceRegistry(),
        new ConfigurationRegistry());
    assertFalse(generator.accepts(KubernetesConfigSubclass.class));
  }

  private abstract class KubernetesConfigSubclass extends KubernetesConfig {
  }
}
