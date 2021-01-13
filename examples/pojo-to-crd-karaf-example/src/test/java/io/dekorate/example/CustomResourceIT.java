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
package io.dekorate.example;

import io.dekorate.testing.WithProject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.dekorate.utils.Exec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesIntegrationTest(deployEnabled = false)
class CustomResourceIT implements WithProject {

  Path testResources = getProject().getRoot().resolve("src").resolve("test").resolve("resources");
  Path crdPath = getProject().getBuildInfo().getClassOutputDir().resolve(getProject().getDekorateOutputDir()).resolve("kubernetes.yml");

  @Test
  void shouldApplyKaraf() {
    assertTrue(Exec.inProject(getProject()).commands("oc", "apply", "-f", crdPath.toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "create", "-f", testResources.resolve("test-karaf.yml").toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "delete", "-f", testResources.resolve("test-karaf.yml").toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "delete", "-f", crdPath.toAbsolutePath().toString()));
  }
}
