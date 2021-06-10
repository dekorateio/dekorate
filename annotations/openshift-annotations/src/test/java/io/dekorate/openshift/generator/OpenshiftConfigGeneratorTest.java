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
package io.dekorate.openshift.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.openshift.config.DefaultOpenshiftConfigGenerator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigGenerator;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.FileProjectFactory;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.DeploymentConfig;

class OpenshiftConfigGeneratorTest {
  static Path tempDir;

  @BeforeAll
  public static void setup() throws IOException {
    tempDir = Files.createTempDirectory("dekorate");
  }

  @Test
  public void shouldGenerateOpenshiftAndWriteToTheFilesystem() {
    WithProject withProject = new WithProject() {
    };

    withProject
        .setProject(FileProjectFactory.create(new File(".")).withDekorateOutputDir(tempDir.toAbsolutePath().toString())
            .withDekorateMetaDir(tempDir.toAbsolutePath().toString()));
    SessionWriter writer = new SimpleFileWriter(withProject.getProject());
    Session session = Session.getSession();
    session.setWriter(writer);

    OpenshiftConfigGenerator generator = new DefaultOpenshiftConfigGenerator(session.getConfigurationRegistry());

    Map<String, Object> openshiftConfig = new HashMap<String, Object>() {
      {
        put(OpenshiftConfig.class.getName(), new HashMap<String, Object>() {
          {
            put("name", "generator-test");
            put("version", "latest");
            put("replicas", 2);
          }
        });
      }
    };

    generator.addPropertyConfiguration(openshiftConfig);
    final Map<String, String> result = session.close();
    KubernetesList list = session.getGeneratedResources().get("openshift");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
        .filteredOn(i -> "DeploymentConfig".equals(i.getKind()))
        .filteredOn(i -> ((DeploymentConfig) i).getSpec().getReplicas() == 2)
        .isNotEmpty();

    assertThat(tempDir.resolve("openshift.json")).exists();
    assertThat(tempDir.resolve("openshift.yml")).exists();

    assertThat(result).hasSize(5);
  }
}
