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
package io.dekorate.kubernetes.generator;

import io.dekorate.Session;
import io.dekorate.SessionWriter;
import io.dekorate.WithProject;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.api.model.apps.Deployment;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.processor.SimpleFileWriter;
import io.dekorate.project.FileProjectFactory;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesApplicationGeneratorTest {

  @Test
  public void shouldGenerateKubernetesWithoutWritingToFileSystem() throws IOException {
    Path tempDir = Files.createTempDirectory("dekorate");

    WithProject withProject = new WithProject() {};
    withProject.setProject(FileProjectFactory.create(new File(".")));

    SessionWriter writer = new SimpleFileWriter(tempDir, false);
    Session session = Session.getSession();
    session.setWriter(writer);

    KubernetesApplicationGenerator generator = new KubernetesApplicationGenerator() {};

    Map<String, Object> map = new HashMap<String, Object>() {{
      put(KubernetesApplication.class.getName(), new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("group", "generator-test-group");
        put("version", "latest");
        put("replicas", 2);
        put("ports", new Map[] {new HashMap<String, Object>(){{
          put("containerPort", 8080);
          put("name", "HTTP");
        }}});
        put("livenessProbe", new HashMap<String, Object>() {{
          put("httpActionPath", "/health");
        }});
      }});
    }};

    generator.add(map);
    final Map<String, String> result = session.close();
    KubernetesList list=session.getGeneratedResources().get("kubernetes");
    assertThat(list).isNotNull();
    assertThat(list.getItems())
            .filteredOn(i -> "Deployment".equals(i.getKind()))
            .hasOnlyOneElementSatisfying(item -> {
              assertThat(item).isInstanceOfSatisfying(Deployment.class, dep -> {
                assertThat(dep.getSpec()).satisfies(spec -> {
                  assertThat(spec.getReplicas()).isEqualTo(2);
                  assertThat(spec.getTemplate().getSpec()).satisfies(podSpec -> {
                    assertThat(podSpec.getContainers()).hasOnlyOneElementSatisfying(container -> {
                      assertThat(container.getPorts()).hasOnlyOneElementSatisfying(port -> {
                        assertThat(port.getContainerPort()).isEqualTo(8080);
                        assertThat(port.getName()).isEqualTo("HTTP");
                      });
                      assertThat(container.getLivenessProbe().getHttpGet()).satisfies(httpGetAction -> {
                        assertThat(httpGetAction.getPath()).isEqualTo("/health");
                        assertThat(httpGetAction.getPort().getIntVal()).isEqualTo(8080);
                      });
                    });
                  });
                });
              });
            });

    assertThat(tempDir.resolve("kubernetes.json")).doesNotExist();
    assertThat(tempDir.resolve("kubernetes.yml")).doesNotExist();

    assertThat(result).hasSize(5);
  }
}
