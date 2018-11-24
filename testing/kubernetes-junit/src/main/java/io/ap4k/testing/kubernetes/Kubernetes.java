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
package io.ap4k.testing.kubernetes;

import io.ap4k.config.KubernetesConfig;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.client.DefaultKubernetesClient;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.VersionInfo;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.project.FileProjectFactory;
import io.ap4k.project.Project;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExecutionCondition;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Kubernetes implements ExecutionCondition, BeforeAllCallback, AfterAllCallback {

  private static final String MANIFEST_PATH = "META-INF/ap4k/kubernetes.yml";
  private static final String KUBERNETES_CONFIG_PATH = "META-INF/ap4k/.config/kubernetes.yml";
  private static final String DOCKER_CONFIG_PATH = "META-INF/ap4k/.config/dockerbuild.yml";

  private final KubernetesClient client = new DefaultKubernetesClient();
  private final List<HasMetadata> created = new ArrayList<>();

  private KubernetesConfig kubernetesConfig;
  private DockerBuildConfig dockerBuildConfig;
  private Project project;

  @Override
  public void afterAll(ExtensionContext context) {
    System.out.println("Deleting test resources");
    created.stream().forEach(r -> {
        System.out.println("Deleting: " + r.getKind() + " name:" +r.getMetadata().getName()+ " status:"+ client.resource(r).cascading(true).delete());
      });
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    project = FileProjectFactory.create(toFile(Kubernetes.class.getClassLoader().getResource(MANIFEST_PATH)));
    kubernetesConfig = Serialization.unmarshal(Kubernetes.class.getClassLoader().getResourceAsStream(KUBERNETES_CONFIG_PATH), KubernetesConfig.class);
    dockerBuildConfig = Serialization.unmarshal(Kubernetes.class.getClassLoader().getResourceAsStream(DOCKER_CONFIG_PATH), DockerBuildConfig.class);

    DockerBuildHook build = new DockerBuildHook(project, dockerBuildConfig);
    build.run();

    URL manifestUrl = Kubernetes.class.getClassLoader().getResource(MANIFEST_PATH);
    if (manifestUrl != null)  {
      File manifestFile = toFile(manifestUrl);
      System.out.println("Apply test resources from:" + manifestFile.getAbsolutePath());
      this.project = FileProjectFactory.create(manifestFile);
      try (InputStream is = manifestUrl.openStream()) {
        created.addAll(Serialization.unmarshal(is, KubernetesList.class).getItems());
        List<HasMetadata> items = client.resourceList(created).createOrReplace();
        for (HasMetadata i : items) {
          System.out.println("Created: " + i.getKind() + " name:" +i.getMetadata().getName()+ ".");
        }
      }
      client.resourceList(created).waitUntilReady(5, TimeUnit.MINUTES);
    }
  }

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    try {
      VersionInfo version = client.getVersion();
      return ConditionEvaluationResult.enabled("Found version:" + version);
    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not communicate with Kubernetes API server.");
    }
  }

  protected static File toFile(URL url) {
    String path = url.getPath();
    if  (path.contains("!")) {
      path = path.substring(0, path.indexOf("!"));
    }
    if (path.startsWith("jar:")) {
      path = path.substring(4);
    }
    if (path.startsWith("file:")) {
      path = path.substring(5);
    }
    return new File(path);
  }

}
