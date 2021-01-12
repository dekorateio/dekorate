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
package io.dekorate.testing.openshift;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactories;
import io.dekorate.BuildServiceFactory;
import io.dekorate.DekorateException;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.annotation.Internal;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.project.Project;
import io.dekorate.testing.Diagnostics;
import io.dekorate.testing.Pods;
import io.dekorate.testing.WithEvents;
import io.dekorate.testing.WithKubernetesClient;
import io.dekorate.testing.WithPod;
import io.dekorate.testing.WithProject;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfig;
import io.dekorate.utils.Packaging;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.client.OpenShiftClient;

@Internal
public class OpenshiftExtension implements ExecutionCondition, BeforeAllCallback, AfterAllCallback,
    WithOpenshiftIntegrationTest, WithPod, WithKubernetesClient, WithOpenshiftResources, WithProject, WithEvents,
    WithOpenshiftConfig {

  private final Logger LOGGER = LoggerFactory.getLogger();

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    try {
      KubernetesClient client = getKubernetesClient(context);
      if (!client.isAdaptable(OpenShiftClient.class)) {
        String reason = "Could not detect Openshift!";
        return ConditionEvaluationResult.disabled(reason);
      }
      VersionInfo version = getKubernetesClient(context).getVersion();
      String message = "Found version:" + version.getMajor() + "." + version.getMinor();
      LOGGER.info(message);
      return ConditionEvaluationResult.enabled(message);
    } catch (Throwable t) {
      String reason = "Could not communicate with Openshift API server.";
      LOGGER.error(reason);
      return ConditionEvaluationResult.disabled(reason);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    OpenshiftIntegrationTestConfig config = getOpenshiftIntegrationTestConfig(context);
    KubernetesClient client = getKubernetesClient(context);
    KubernetesList list = getOpenshiftResources(context);

    OpenshiftConfig openshiftConfig = getOpenshiftConfig();
    ImageConfiguration imageConfiguration = ImageConfiguration.from(openshiftConfig);

    BuildService buildService = null;
    try {
      BuildServiceFactory buildServiceFactory = BuildServiceFactories.find(getProject(), imageConfiguration)
          .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
      buildService = buildServiceFactory.create(getProject(), imageConfiguration, list.getItems());
    } catch (Exception e) {
      throw DekorateException.launderThrowable("Failed to lookup BuildService.", e);
    }
    if (config.isPushEnabled()) {
      buildService.prepare();
      buildService.build();
      buildService.push();
    } else if (config.isBuildEnabled()) {
      buildService.prepare();
      buildService.build();
    }

    if (config.isDeployEnabled()) {
      //Create the remaining resources.
      list.getItems().stream()
          .filter(i -> !(i instanceof BuildConfig))
          .forEach(i -> {
            try {
              HasMetadata r = client.resource(i).fromServer().get();
              if (r == null) {
                client.resource(i).apply();
              } else if (r instanceof ImageStream) {
                //let's not delete image streams at this point
              } else if (deleteAndWait(context, i, 1, TimeUnit.MINUTES)) {
                client.resource(i).apply();
              }
            } catch (Exception e) {
              e.printStackTrace(System.err);
            }
            LOGGER.info("Created: " + i.getKind() + " name:" + i.getMetadata().getName() + ".");
          });

      List<HasMetadata> waitables = list.getItems().stream().filter(i -> i instanceof Deployment ||
          i instanceof DeploymentConfig ||
          i instanceof Pod ||
          i instanceof ReplicaSet ||
          i instanceof ReplicationController).collect(Collectors.toList());
      long started = System.currentTimeMillis();
      LOGGER.info("Waiting until ready (" + config.getReadinessTimeout() + " ms)...");
      waitUntilCondition(context, waitables, i -> OpenshiftReadiness.isReady(i), config.getReadinessTimeout(),
          TimeUnit.MILLISECONDS);
      long ended = System.currentTimeMillis();
      LOGGER.info("Waited: " + (ended - started) + " ms.");
      //Display the item status
      waitables.stream().map(r -> client.resource(r).fromServer().get())
          .forEach(i -> {
            if (!OpenshiftReadiness.isReady(i)) {
              LOGGER.warning(i.getKind() + ":" + i.getMetadata().getName() + " not ready!");
            }
          });
    }
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    Arrays.stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> {
          injectKubernetesClient(context, testInstance, f);
          injectOpenshiftResources(context, testInstance, f);
          injectPod(context, testInstance, f);
        });
  }

  @Override
  public void afterAll(ExtensionContext context) {
    OpenShiftClient client = getKubernetesClient(context).adapt(OpenShiftClient.class);
    try {
      boolean failed = context.getExecutionException().isPresent();
      final Diagnostics diagnostics = new Diagnostics(client);
      if (failed) {
        displayDiagnostics(context);
      }
      getOpenshiftResources(context).getItems().stream().filter(r -> !(r instanceof ImageStream)).forEach(r -> {
        try {
          LOGGER.info("Deleting: " + r.getKind() + " name:" + r.getMetadata().getName() + ". Deleted:"
              + client.resource(r).delete());
        } catch (Exception e) {
        }
      });

      OpenshiftConfig openshiftConfig = getOpenshiftConfig();
      List<HasMetadata> buildPods = client.pods().list().getItems().stream()
          .filter(i -> i.getMetadata().getName().matches(openshiftConfig.getName() + "-\\d-build"))
          .collect(Collectors.toList());

      try {
        client.resourceList(buildPods).delete();
        client.deploymentConfigs().withName(openshiftConfig.getName()).delete();
      } catch (Exception e) {
      }
    } finally {
      closeKubernetesClient(context);
    }
  }

  public void displayDiagnostics(ExtensionContext context) {
    KubernetesClient client = getKubernetesClient(context);
    List<? extends HasMetadata> resources = getOpenshiftResources(context).getItems();
    Pods pods = new Pods(client);
    PodList podList = pods.list(resources);

    final Diagnostics diagnostics = new Diagnostics(client, pods);
    if (podList == null || podList.getItems().isEmpty()) {
      diagnostics.displayAll();
    } else {
      getOpenshiftResources(context).getItems().stream().forEach(r -> diagnostics.display(r));
    }
  }

  public void build(ExtensionContext context, Project project) {
    KubernetesList kubernetesList = getOpenshiftResources(context);
    KubernetesClient client = getKubernetesClient(context);
    Path path = project.getBuildInfo().getOutputFile();
    File tar = Packaging.packageFile(path.getParent().toAbsolutePath());

    kubernetesList.getItems().stream()
        .filter(i -> i instanceof BuildConfig)
        .map(i -> (BuildConfig) i)
        .forEach(bc -> binaryBuild(client.adapt(OpenShiftClient.class), bc, tar));
  }

  /**
   * Performs the binary build of the specified {@link BuildConfig} with the given binary input.
   * 
   * @param buildConfig The build config.
   * @param binaryFile The binary file.
   */
  private void binaryBuild(OpenShiftClient client, BuildConfig buildConfig, File binaryFile) {
    LOGGER.info("Running binary build:" + buildConfig.getMetadata().getName() + " for:" + binaryFile.getAbsolutePath());
    Build build = client.buildConfigs().withName(buildConfig.getMetadata().getName()).instantiateBinary()
        .fromFile(binaryFile);
    try (BufferedReader reader = new BufferedReader(
        client.builds().withName(build.getMetadata().getName()).getLogReader())) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        System.out.println(line);
      }
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  @Override
  public String getName() {
    return getOpenshiftConfig().getName();
  }
}
