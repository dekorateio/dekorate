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

package io.dekorate.openshift;

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
import io.dekorate.openshift.config.EditableOpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.manifest.OpenshiftManifestGenerator;
import io.dekorate.project.FileProjectFactory;

class OpenshiftManifestGeneratorTest {

  @BeforeAll
  public static void setup() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");
    WithProject withProject = new WithProject() {
    };
    withProject.setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
        .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
  }

  @Test
  public void shouldAcceptOpenshiftConfig() {
    Session session = Session.getSession();
    OpenshiftManifestGenerator generator = new OpenshiftManifestGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(generator.accepts(OpenshiftConfig.class));
  }

  @Test
  public void shouldAcceptEditableOpenshiftConfig() {
    Session session = Session.getSession();
    OpenshiftManifestGenerator generator = new OpenshiftManifestGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertTrue(generator.accepts(EditableOpenshiftConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfig() {
    Session session = Session.getSession();
    OpenshiftManifestGenerator generator = new OpenshiftManifestGenerator(session.getResourceRegistry(),
        session.getConfigurationRegistry());
    assertFalse(generator.accepts(BaseConfig.class));
  }
}
