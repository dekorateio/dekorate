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
package io.ap4k.testing.openshift;

import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.kubernetes.client.VersionInfo;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.kubernetes.annotation.Internal;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.openshift.api.model.Build;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.utils.OpenshiftUtils;
import io.ap4k.project.Project;
import io.ap4k.testing.WithKubernetesClient;
import io.ap4k.testing.WithPod;
import io.ap4k.testing.WithProject;
import io.ap4k.utils.Packaging;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Internal
public class OpenshiftExtension implements ExecutionCondition, BeforeAllCallback, AfterAllCallback, WithPod, WithKubernetesClient, WithOpenshiftResources, WithProject, WithSourceToImageConfig {

  private static final String MANIFEST_PATH = "META-INF/ap4k/openshift.yml";
  private static final String S2I_CONFIG_PATH = "META-INF/ap4k/.config/sourcetoimage.yml";
  private static final String TARGET = "target";

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    try {
      KubernetesClient client = getKubernetesClient(context);
      if (!client.isAdaptable(OpenShiftClient.class)) {
        return ConditionEvaluationResult.disabled("Could not detect openshift.");
      }
      VersionInfo version = getKubernetesClient(context).getVersion();
      return ConditionEvaluationResult.enabled("Found version:" + version);
    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not communicate with KubernetesExtension API server.");
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    KubernetesClient client = getKubernetesClient(context);
    KubernetesList list = getOpenshiftResources(context);

    List<HasMetadata> buildResources = list.getItems().stream()
      .filter(i -> i.getKind().equals("BuildConfig") || i.getKind().equals("ImageStream"))
      .collect(Collectors.toList());

    //Apply build resources upfront
    buildResources.stream()
      .forEach(i -> {
        client.resourceList(i).deletingExisting().createOrReplace();
        System.out.println("Created: " + i.getKind() + " name:" + i.getMetadata().getName() + ".");
      });
    OpenshiftUtils.waitForImageStreamTags(buildResources, 2, TimeUnit.MINUTES);

    //Create the remaining resources.
    List<HasMetadata> remainingResources = new ArrayList<>(list.getItems());
    remainingResources.removeAll(buildResources);
    remainingResources.stream()
      .forEach(i -> {
        client.resourceList(i).deletingExisting().createOrReplace();
        System.out.println("Created: " + i.getKind() + " name:" + i.getMetadata().getName() + ".");
      });

    Project project = getProject(MANIFEST_PATH);
    S2iConfig s2iConfig = getSourceToImageConfig();

    build(context, project);
    client.adapt(OpenShiftClient.class).deploymentConfigs().withName(s2iConfig.getName()).waitUntilReady(5, TimeUnit.MINUTES);
  }


  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    Arrays.stream( testInstance.getClass().getDeclaredFields())
      .forEach(f -> {
        injectKubernetesClient(context, testInstance, f);
        injectOpenshiftResources(context, testInstance, f);
        injectPod(context, testInstance, f);
      });
  }

  @Override
  public void afterAll(ExtensionContext context) {
    OpenShiftClient client = getKubernetesClient(context).adapt(OpenShiftClient.class);

    getOpenshiftResources(context).getItems().stream().forEach(r -> {
      try {
        System.out.println("Deleting: " + r.getKind() + " name:" + r.getMetadata().getName() + " status:" + client.resource(r).delete());
      } catch (Exception e) {}
    });

    S2iConfig s2iConfig = getSourceToImageConfig();
    List<HasMetadata> buildPods = client.pods().list()
      .getItems()
      .stream()
      .filter(i -> i.getMetadata().getName().matches(s2iConfig.getName() + "-\\d-build"))
      .collect(Collectors.toList());

     try {
       client.resourceList(buildPods).delete();
       client.deploymentConfigs().withName(s2iConfig.getName()).delete();
     } catch (Exception e) {}
  }


  public void build(ExtensionContext context, Project project) {
    KubernetesList kubernetesList = getOpenshiftResources(context);
    KubernetesClient client = getKubernetesClient(context);
    Path path = project.getRoot().resolve(TARGET).resolve(project.getBuildInfo().getOutputFileName());
    File tar = Packaging.packageFile(path.toAbsolutePath().toString());

    kubernetesList.getItems().stream()
      .filter(i -> i instanceof BuildConfig)
      .map(i -> (BuildConfig)i)
      .forEach( bc -> binaryBuild(client.adapt(OpenShiftClient.class), bc, tar) );
  }

  /**
   * Performs the binary build of the specified {@link BuildConfig} with the given binary input.
   * @param buildConfig The build config.
   * @param binaryFile  The binary file.
   */
  private void binaryBuild(OpenShiftClient client, BuildConfig buildConfig, File binaryFile) {
    System.out.println("Running binary build:"+buildConfig.getMetadata().getName()+ " for:" +binaryFile.getAbsolutePath());
    Build build = client.buildConfigs().withName(buildConfig.getMetadata().getName()).instantiateBinary().fromFile(binaryFile);
    try  (BufferedReader reader = new BufferedReader(client.builds().withName(build.getMetadata().getName()).getLogReader())) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        System.out.println(line);
      }
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }
}
