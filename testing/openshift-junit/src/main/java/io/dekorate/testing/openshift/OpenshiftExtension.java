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
import io.dekorate.testing.WithEvents;
import io.dekorate.testing.WithKubernetesClient;
import io.dekorate.testing.WithPod;
import io.dekorate.testing.WithProject;
import io.dekorate.testing.openshift.config.OpenshiftIntegrationTestConfig;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.client.OpenShiftClient;

@Internal
public class OpenshiftExtension implements ExecutionCondition, BeforeAllCallback, AfterAllCallback,
    WithOpenshiftIntegrationTest, WithPod, WithKubernetesClient, WithOpenshiftResources, WithProject, WithEvents,
    WithOpenshiftConfig, WithRoute {

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
    List<Project> projects = getProjects(context);
    for (Project project : projects) {
      startProject(context, project);
    }
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    Arrays.stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> {
          injectKubernetesClient(context, testInstance, f);
          injectOpenshiftResources(context, testInstance, f);
          injectPod(context, testInstance, f);
          injectRoute(context, testInstance, f);
        });

    if (hasExtensionError(context)) {
      displayDiagnostics(context);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    OpenshiftIntegrationTestConfig config = getOpenshiftIntegrationTestConfig(context);
    OpenShiftClient client = getKubernetesClient(context).adapt(OpenShiftClient.class);
    try {
      if (shouldDisplayDiagnostics(context)) {
        displayDiagnostics(context);
      }

      if (config.isDeployEnabled()) {
        List<Project> projects = getProjects(context);
        for (Project project : projects) {
          deleteProject(context, project, client);
        }
      }
    } finally {
      closeKubernetesClient(context);
    }
  }

  @Override
  public String getName(ExtensionContext context) {
    List<Project> projects = getProjects(context);
    if (projects.size() != 1) {
      throw new IllegalStateException(
          "Multiple projects found, can't use default name. Please, use `@Named` annotations for injecting instances.");
    }

    return getOpenshiftConfig(projects.get(0)).getName();
  }

  @Override
  public String[] getAdditionalModules(ExtensionContext context) {
    return getOpenshiftIntegrationTestConfig(context).getAdditionalModules();
  }

  private void startProject(ExtensionContext context, Project project) throws InterruptedException {
    LOGGER.info("Starting project at " + project.getRoot());
    OpenshiftIntegrationTestConfig config = getOpenshiftIntegrationTestConfig(context);
    KubernetesClient client = getKubernetesClient(context);
    KubernetesList list = getOpenshiftResources(context, project);

    OpenshiftConfig openshiftConfig = getOpenshiftConfig(project);
    ImageConfiguration imageConfiguration = ImageConfiguration.from(openshiftConfig);

    BuildService buildService = null;
    try {
      BuildServiceFactory buildServiceFactory = BuildServiceFactories.find(project, imageConfiguration)
          .orElseThrow(() -> new IllegalStateException("No applicable BuildServiceFactory found."));
      buildService = buildServiceFactory.create(project, imageConfiguration, list.getItems());
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
              readinessFailed(context);
              LOGGER.warning(i.getKind() + ":" + i.getMetadata().getName() + " not ready!");
            }
          });

      if (hasReadinessFailed(context)) {
        throw new IllegalStateException("Readiness Failed");
      }
    }
  }

  private void deleteProject(ExtensionContext context, Project project, OpenShiftClient client) {
    getOpenshiftResources(context, project).getItems().stream().filter(r -> !(r instanceof ImageStream)).forEach(r -> {
      try {
        LOGGER.info("Deleting: " + r.getKind() + " name:" + r.getMetadata().getName() + ". Deleted:"
            + client.resource(r).delete());
      } catch (Exception e) {
      }
    });

    OpenshiftConfig openshiftConfig = getOpenshiftConfig(project);
    List<HasMetadata> buildPods = client.pods().list().getItems().stream()
        .filter(i -> i.getMetadata().getName().matches(openshiftConfig.getName() + "-\\d-build"))
        .collect(Collectors.toList());

    try {
      client.resourceList(buildPods).delete();
      client.deploymentConfigs().withName(openshiftConfig.getName()).delete();
    } catch (Exception e) {
    }
  }
}
