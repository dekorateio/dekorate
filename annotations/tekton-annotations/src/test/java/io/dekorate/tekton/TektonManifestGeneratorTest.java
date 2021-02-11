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

package io.dekorate.tekton;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.project.FileProjectFactory;
import io.dekorate.tekton.config.EditableTektonConfig;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.tekton.manifest.TektonManifestGenerator;

class TektonManifestGeneratorTest {

  @BeforeAll
  public static void setup() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");
    WithProject withProject = new WithProject() {};
    withProject.setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString()).withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
  }
 
  @Test
  public void shouldAcceptTektonConfig() {
    Session session = Session.getSession();
    TektonManifestGenerator generator = new TektonManifestGenerator(session.getResourceRegistry(), session.getConfigurationRegistry());
    assertTrue(generator.canHandle(TektonConfig.class));
  }

  @Test
  public void shouldAcceptEditableTektonConfig() {
    Session session = Session.getSession();
    TektonManifestGenerator generator = new TektonManifestGenerator(session.getResourceRegistry(), session.getConfigurationRegistry());
    assertTrue(generator.canHandle(EditableTektonConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfig() {
    Session session = Session.getSession();
    TektonManifestGenerator generator = new TektonManifestGenerator(session.getResourceRegistry(), session.getConfigurationRegistry());
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
