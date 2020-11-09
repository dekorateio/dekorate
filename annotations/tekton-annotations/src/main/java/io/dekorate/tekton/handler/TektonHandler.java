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
package io.dekorate.tekton.handler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.dekorate.BuildImage;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddDockerConfigJsonSecretDecorator;
import io.dekorate.kubernetes.decorator.AddRoleBindingResourceDecorator;
import io.dekorate.kubernetes.decorator.AddSecretToServiceAccountDecorator;
import io.dekorate.kubernetes.decorator.AddServiceAccountResourceDecorator;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.dekorate.project.ScmInfo;
import io.dekorate.tekton.config.EditableTektonConfig;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.tekton.config.TektonConfigBuilder;
import io.dekorate.tekton.decorator.AddArrayParamToTaskDecorator;
import io.dekorate.tekton.decorator.AddDeployStepDecorator;
import io.dekorate.tekton.decorator.AddImageBuildStepDecorator;
import io.dekorate.tekton.decorator.AddProjectBuildStepDecorator;
import io.dekorate.tekton.decorator.AddStringParamToTaskDecorator;
import io.dekorate.tekton.decorator.AddPvcToPipelineRunDecorator;
import io.dekorate.tekton.decorator.AddPvcToTaskRunDecorator;
import io.dekorate.tekton.decorator.AddResourceInputToTaskDecorator;
import io.dekorate.tekton.decorator.AddResourceOutputToTaskDecorator;
import io.dekorate.tekton.decorator.AddResourceToPipelineDecorator;
import io.dekorate.tekton.decorator.AddServiceAccountToTaskDecorator;
import io.dekorate.tekton.decorator.AddToArgsDecorator;
import io.dekorate.tekton.decorator.AddWorkspaceToPipelineDecorator;
import io.dekorate.tekton.decorator.AddWorkspaceToPipelineTaskDecorator;
import io.dekorate.tekton.decorator.AddWorkspaceToTaskDecorator;
import io.dekorate.tekton.step.DeployStep;
import io.dekorate.tekton.step.ImageBuildStep;
import io.dekorate.tekton.step.KanikoBuildStep;
import io.dekorate.tekton.step.ProjectBuildStep;
import io.dekorate.utils.Images;
import io.dekorate.utils.Jvm;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.QuantityBuilder;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRunBuilder;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTask;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTaskBuilder;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.pipeline.v1beta1.TaskRunBuilder;
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import io.fabric8.tekton.resource.v1alpha1.PipelineResourceBuilder;

public class TektonHandler implements Handler<TektonConfig>, HandlerFactory, WithProject {

  private static final Logger LOGGER = LoggerFactory.getLogger();

  private static final String TEKTON_PIPELINE = "tekton-pipeline";
  private static final String TEKTON_PIPELINE_RUN = "tekton-pipeline-run";

  private static final String TEKTON_TASK = "tekton-task";
  private static final String TEKTON_TASK_RUN = "tekton-task-run";

  private static final String AND = "and";

  private static final String GIT = "git";
  private static final String REVISION = "revision";
  private static final String URL = "url";
  private static final String IMAGE = "image";
  private static final String BUILD = "build";
  private static final String DEPLOY = "deploy";
  private static final String WORKSPACE = "workspace";
  private static final String RUN = "run";
  private static final String NOW = "now";

  private static final String GIT_SOURCE = "git-source";
  private static final String OUTPUT_IMAGE = "out-image";

  private static final String PIPELINE_SOURCE_WS = "pipeline-source-ws";
  private static final String PIPELINE_SOURCE_WS_DECSCRIPTION = "The workspace to share between pipeline steps";

  private static final String PIPELINE_M2_WS = "pipeline-m2-ws";
  private static final String PIPELINE_M2_WS_DECSCRIPTION = "The workspace to store m2 artifacts";

  private static final String PROJECT = "project";
  private static final String DASH = "-";

  private static final String MAVEN_LOCAL_REPO_SYS_PROPERTY = "-Dmaven.repo.local=%s";
  private static final String IMAGE_PULL_SECRETS_SYS_PROPERTY = "-Ddekorate.image-pull-secrets=";
  private static final String USER_NAME_SYS_PROP = "-Duser.name=";


  private static final String TEKTON = "tekton";
  private static final String DEFAULT_TIMEOUT = "1h0m0s";

  private final Resources resources;
  private final Configurators configurators;

  public TektonHandler() {
    this(new Resources(), new Configurators());
  }

  public TektonHandler(Resources resources, Configurators configurators) {
    this.resources = resources;
    this.configurators = configurators;
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new TektonHandler(resources, configurators);
  }

  @Override
  public int order() {
    return 500;
  }

  @Override
  public String getKey() {
    return TEKTON;
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(TektonConfig.class) ||
        type.equals(EditableTektonConfig.class);
  }

  public void handle(TektonConfig config) {

    ImageConfiguration imageConfiguration = getImageConfiguration(getProject(), config, configurators);

    generateCommon(TEKTON_PIPELINE, config, imageConfiguration);
    generatePipelineResources(config);

    generateCommon(TEKTON_TASK, config, imageConfiguration);
    generateTaskResources(config);
  }

  public void generateCommon(String group, TektonConfig config, ImageConfiguration imageConfiguration) {
    if (Strings.isNotNullOrEmpty(config.getExternalGitPipelineResource())) {
      //Do nothing
      LOGGER.info("Tekton " + group.split("-")[1] + " expects existing git pipeline resource named: "
          + config.getExternalGitPipelineResource() + "!");
    } else if (config.getProject().getScmInfo() != null) {
      resources.add(group, createGitResource(config));
    } else {
      throw new IllegalStateException(
          "Project is not under version control, or unsupported version control system. Aborting generation of tekton resources!");
    }

    resources.add(group, createOutputImageResource(config, imageConfiguration));
    resources.add(group, createRole(config));

    resources.decorate(group, new AddServiceAccountResourceDecorator(config.getName()));
    resources.decorate(group, new AddRoleBindingResourceDecorator(config.getName() + ":deployer", config.getName(),
        "pipeline-deployer", AddRoleBindingResourceDecorator.RoleKind.Role));

    //All Tasks
    resources.decorate(group, new AddWorkspaceToTaskDecorator(null, config.getSourceWorkspace(),
        "The workspace to hold all project sources", false, null));
    resources.decorate(group, new AddStringParamToTaskDecorator(null, ImageBuildStep.PATH_TO_CONTEXT_PARAM_NAME, ImageBuildStep.PATH_TO_CONTEXT_PARAM_DESCRIPTION,
        getContextPath(getProject())));

    String monolithTaskName = monolithTaskName(config);

    String projectBuildTaskName = projectBuildTaskName(config);
    String projectBuildStepName = projectBuildStepName(config);
    String imageBuildTaskName = imageBuildTaskName(config);
    String deployTaskName = deployTaskName(config);

    if (group.equals(TEKTON_TASK)) { //We just a single task
      projectBuildTaskName = monolithTaskName;
      imageBuildTaskName = monolithTaskName;
      deployTaskName = monolithTaskName;
    }

    //Java Build
    BuildInfo build = config.getProject().getBuildInfo();
    BuildImage projectBuildImage = Strings.isNotNullOrEmpty(config.getProjectBuilderImage())
        && Strings.isNotNullOrEmpty(config.getProjectBuilderCommand())
            ? new BuildImage(config.getProjectBuilderImage(), config.getProjectBuilderCommand(), config.getProjectBuilderArguments())
            : BuildImage.find(build.getBuildTool(), build.getBuildToolVersion(), Jvm.getVersion(), null)
                .orElseThrow(() -> new IllegalStateException("No project builder image was found!"));

    resources.decorate(group, new AddResourceInputToTaskDecorator(projectBuildTaskName, GIT, GIT_SOURCE, "/source/" + config.getName()));
    resources.decorate(group, new AddStringParamToTaskDecorator(projectBuildTaskName, ProjectBuildStep.IMAGE_PARAM_NAME, ProjectBuildStep.IMAGE_PARAM_DESCRIPTION, projectBuildImage.getImage()));
    resources.decorate(group, new AddStringParamToTaskDecorator(projectBuildTaskName, ProjectBuildStep.COMMAND_PARAM_NAME, ProjectBuildStep.COMMAND_PARAM_DESCRIPTION, projectBuildImage.getCommand()));
    resources.decorate(group, new AddArrayParamToTaskDecorator(projectBuildTaskName, ProjectBuildStep.ARGS_PARAM_NAME, ProjectBuildStep.ARGS_PARAM_DESCRIPTION, projectBuildImage.getArguments()));

    //This is needed so that we pass a sensible group to the `in-cluster build`.
    resources.decorate(group, new AddToArgsDecorator(projectBuildTaskName, projectBuildStepName, USER_NAME_SYS_PROP + imageConfiguration.getGroup()));
    resources.decorate(group, new AddProjectBuildStepDecorator(projectBuildTaskName, ProjectBuildStep.ID, config.getName()));

    //Image Build
    String imageBuilderImage = Strings.isNotNullOrEmpty(config.getImageBuilderImage()) ? config.getImageBuilderImage() : KanikoBuildStep.IMAGE_PARAM_DEFAULT;
    String imageBuilderCommand = Strings.isNotNullOrEmpty(config.getImageBuilderCommand()) ? config.getImageBuilderCommand() : KanikoBuildStep.COMMAND_PARAM_DEFAULT;
    String[] imageBuilderArgs = config.getImageBuilderArguments() != null && config.getImageBuilderArguments().length > 0 ? config.getImageBuilderArguments(): KanikoBuildStep.getDefaultArguments(config.getDockerfile(), getContextPath(getProject()));

    resources.decorate(group, new AddStringParamToTaskDecorator(imageBuildTaskName, ImageBuildStep.IMAGE_PARAM_NAME, ImageBuildStep.IMAGE_PARAM_DESCRIPTION, imageBuilderImage));
    resources.decorate(group, new AddStringParamToTaskDecorator(imageBuildTaskName, ImageBuildStep.COMMAND_PARAM_NAME, ImageBuildStep.COMMAND_PARAM_DESCRIPTION, imageBuilderCommand));
    resources.decorate(group, new AddArrayParamToTaskDecorator(imageBuildTaskName, ImageBuildStep.ARGS_PARAM_NAME, ImageBuildStep.ARGS_PARAM_DESCRIPTION, imageBuilderArgs));
    resources.decorate(group, new AddStringParamToTaskDecorator(imageBuildTaskName, KanikoBuildStep.PATH_TO_DOCKERFILE_PARAM_NAME, KanikoBuildStep.PATH_TO_DOCKERFILE_PARAM_DESCRIPTION, config.getDockerfile()));
    resources.decorate(group, new AddResourceOutputToTaskDecorator(imageBuildTaskName, IMAGE, IMAGE));
    resources.decorate(group, new AddImageBuildStepDecorator(imageBuildTaskName, config.getName()));
   
    //Deploy Task
    resources.decorate(group, new AddStringParamToTaskDecorator(deployTaskName, DeployStep.PATH_TO_YML_PARAM_NAME, DeployStep.PATH_TO_YML_PARAM_DESCRIPTION, DeployStep.PATH_TO_YML_PARAM_DEFAULT));
    resources.decorate(group, new AddDeployStepDecorator(deployTaskName, config.getName(), config.getDeployerImage()));

    Map<String, String> annotations = new HashMap<String, String>() {
      {
        put("tekton.dev/docker-0", "https://" + imageConfiguration.getRegistry());
      }
    };

    String m2WorkspaceClaimName = m2WorkspaceClaimName(config);
    if (Strings.isNotNullOrEmpty(m2WorkspaceClaimName)) {
      String m2Path = "/workspaces/" + config.getM2Workspace();
      resources.add(group, createM2WorkspacePvc(config));
      resources.decorate(group, new AddWorkspaceToTaskDecorator(projectBuildTaskName, config.getM2Workspace(),
          "Local maven repository workspace", false, m2Path));
      resources.decorate(group, new AddToArgsDecorator(projectBuildTaskName, projectBuildStepName,
          String.format(MAVEN_LOCAL_REPO_SYS_PROPERTY, m2Path)));
    }

    if (Strings.isNotNullOrEmpty(config.getImagePushServiceAccount())) {
      resources.decorate(group + DASH + RUN, new AddServiceAccountToTaskDecorator(IMAGE + DASH + BUILD, config.getImagePushServiceAccount()));
    } else {
      String generatedServiceAccount = config.getName();
      resources.decorate(group + DASH + RUN, new AddServiceAccountToTaskDecorator(IMAGE + DASH + BUILD, generatedServiceAccount));
      if (Strings.isNotNullOrEmpty(config.getImagePushSecret())) {
        resources.decorate(group,
            new AddSecretToServiceAccountDecorator(generatedServiceAccount, config.getImagePushSecret()));
        resources.decorate(group, new AddToArgsDecorator(projectBuildTaskName, projectBuildStepName,
            IMAGE_PULL_SECRETS_SYS_PROPERTY + config.getImagePushSecret()));
      } else if (config.isUseLocalDockerConfigJson()) {
        String generatedSecret = config.getName() + "-registry-credentials";
        Path dockerConfigJson = Paths.get(System.getProperty("user.home"), ".docker", "config.json");
        if (!dockerConfigJson.toFile().exists()) {
          throw new IllegalStateException(
              "User requested to use the local `.docker/config.json` file, but it doesn't exist!");
        } else {
          LOGGER.warning(dockerConfigJson.toAbsolutePath().normalize().toString()
              + " is going to be added as part of Secret: " + generatedSecret);
        }
        resources.decorate(group, new AddToArgsDecorator(projectBuildTaskName, projectBuildStepName,
            IMAGE_PULL_SECRETS_SYS_PROPERTY + generatedSecret));
        resources.decorate(group,
            new AddDockerConfigJsonSecretDecorator(generatedSecret, dockerConfigJson, annotations));
        resources.decorate(group, new AddSecretToServiceAccountDecorator(generatedServiceAccount, generatedSecret));
      } else if (Strings.isNotNullOrEmpty(config.getRegistryUsername())
          && Strings.isNotNullOrEmpty(config.getRegistryPassword())) {
        String generatedSecret = config.getName() + "-registry-credentials";
        resources.decorate(group, new AddToArgsDecorator(projectBuildTaskName, projectBuildStepName,
            IMAGE_PULL_SECRETS_SYS_PROPERTY + generatedSecret));
        resources.decorate(group, new AddDockerConfigJsonSecretDecorator(generatedSecret, config.getRegistry(),
            config.getRegistryUsername(), config.getRegistryPassword(), annotations));
        resources.decorate(group, new AddSecretToServiceAccountDecorator(generatedServiceAccount, generatedSecret));
      } else {
        LOGGER.error(
            "An existing builder image service account or secret is required! Alternatively, you can specify a registry username and password!");
      }
    }
  }

  public void generatePipelineResources(TektonConfig config) {
    String projectBuildTaskName = projectBuildTaskName(config);
    String imageBuildTaskName = imageBuildTaskName(config);
    String deployTaskName = deployTaskName(config);

    String m2WorkspaceClaimName = m2WorkspaceClaimName(config);

    resources.add(TEKTON_PIPELINE, createTask(projectBuildTaskName));
    resources.add(TEKTON_PIPELINE, createTask(imageBuildTaskName));
    resources.add(TEKTON_PIPELINE, createTask(deployTaskName));

    String pipelineName = config.getName();
    resources.add(TEKTON_PIPELINE, createPipeline(config));
    resources.decorate(TEKTON_PIPELINE, new AddResourceToPipelineDecorator(pipelineName, GIT, GIT_SOURCE, false));
    resources.decorate(TEKTON_PIPELINE, new AddResourceToPipelineDecorator(pipelineName, IMAGE, OUTPUT_IMAGE, false));

    if (Strings.isNullOrEmpty(config.getExternalSourceWorkspaceClaim())) {
      resources.add(TEKTON_PIPELINE, createSourceWorkspacePvc(config));
    }

    resources.add(TEKTON_PIPELINE_RUN, createPipelineRun(config));

    if (Strings.isNotNullOrEmpty(m2WorkspaceClaimName)) {
      resources.decorate(TEKTON_PIPELINE,
          new AddWorkspaceToPipelineTaskDecorator(null, BUILD, config.getM2Workspace(), PIPELINE_M2_WS));
      resources.decorate(TEKTON_PIPELINE,
          new AddWorkspaceToPipelineDecorator(null, PIPELINE_M2_WS, "Local maven repository workspace"));
      resources.decorate(TEKTON_PIPELINE_RUN,
          new AddPvcToPipelineRunDecorator(null, PIPELINE_M2_WS, m2WorkspaceClaimName, false));
    }
  }

  public void generateTaskResources(TektonConfig config) {
    String monolithTaskName = monolithTaskName(config);
    String m2WorkspaceClaimName = m2WorkspaceClaimName(config);
    resources.add(TEKTON_TASK, createTask(monolithTaskName));
    resources.add(TEKTON_TASK_RUN, createTaskRun(config));

    if (Strings.isNotNullOrEmpty(m2WorkspaceClaimName)) {
      resources.decorate(TEKTON_TASK_RUN,
          new AddPvcToTaskRunDecorator(null, config.getM2Workspace(), m2WorkspaceClaimName, false));
    }
  }

  public PipelineResource createGitResource(TektonConfig config) {
    ScmInfo scm = Optional.ofNullable(config.getProject().getScmInfo())
        .orElseThrow(() -> new IllegalStateException("No scm info found!"));
    return new PipelineResourceBuilder()
        .withNewMetadata()
        .withName(gitResourceName(config))
        .endMetadata()
        .withNewSpec()
        .withType(GIT)
        .addNewParam()
        .withName(URL)
        .withValue(scm.getUrl())
        .endParam()
        .addNewParam()
        .withName(REVISION)
        .withValue(scm.getCommit())
        .endParam()
        .endSpec()
        .build();
  }

  public PipelineResource createOutputImageResource(TektonConfig config, ImageConfiguration imageConfig) {
    String image = Images.getImage(
        Strings.isNotNullOrEmpty(imageConfig.getRegistry()) ? imageConfig.getRegistry() : "docker.io",
        imageConfig.getGroup(), imageConfig.getName(), imageConfig.getVersion());
    return new PipelineResourceBuilder()
        .withNewMetadata()
        .withName(outputImageResourceName(config))
        .endMetadata()
        .withNewSpec()
        .withType(IMAGE)
        .addNewParam()
        .withName(URL)
        .withValue(image)
        .endParam()
        .endSpec()
        .build();
  }

  public Task createTask(String name) {
    return new TaskBuilder()
        .withNewMetadata().withName(name).endMetadata().withNewSpec().endSpec().build();
  }

  public PipelineTask createProjectBuildPipelineTask(TektonConfig config) {
    return new PipelineTaskBuilder()
        .withName(PROJECT + DASH + BUILD)
        .withNewTaskRef()
        .withName(projectBuildTaskName(config))
        .endTaskRef()
        .addNewWorkspace().withName(config.getSourceWorkspace()).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
        .withNewResources().addNewInput().withName(GIT_SOURCE).withResource(GIT_SOURCE).endInput().endResources()
        .addNewParam().withName(ProjectBuildStep.PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
        .build();
  }

  public PipelineTask createImageBuildPipelineTask(TektonConfig config) {
    return new PipelineTaskBuilder()
        .withName(IMAGE + DASH + BUILD)
        .withNewTaskRef()
        .withName(imageBuildTaskName(config))
        .endTaskRef()
        .withNewResources()
        .addNewOutput().withName(IMAGE).withResource(OUTPUT_IMAGE).endOutput()
        .endResources()
        .addNewWorkspace().withName(config.getSourceWorkspace()).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
        .addNewParam().withName(ImageBuildStep.PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
        .withRunAfter(PROJECT + DASH + BUILD)
        .build();
  }

  public PipelineTask createDeployPipelineTask(TektonConfig config) {
    return new PipelineTaskBuilder()
        .withName(DEPLOY)
        .withNewTaskRef()
        .withName(deployTaskName(config))
        .endTaskRef()
        .addNewWorkspace().withName(config.getSourceWorkspace()).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
        .addNewParam().withName(DeployStep.PATH_TO_YML_PARAM_NAME).withNewValue(getYamlPath(getProject())).endParam()
        .addNewParam().withName(DeployStep.PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
        .withRunAfter(PROJECT + DASH + BUILD, IMAGE + DASH + BUILD)
        .build();
  }

  public Pipeline createPipeline(TektonConfig config) {
    return new PipelineBuilder()
        .withNewMetadata()
        .withName(config.getName())
        .endMetadata()
        .withNewSpec()
        .addNewWorkspace().withName(PIPELINE_SOURCE_WS).endWorkspace()
        .addNewResource()
        .withType(GIT)
        .withName(GIT_SOURCE)
        .endResource()
        .addNewResource()
        .withType(IMAGE)
        .withName(OUTPUT_IMAGE)
        .endResource()
        .addToTasks(createProjectBuildPipelineTask(config), createImageBuildPipelineTask(config),
            createDeployPipelineTask(config))
        .endSpec()
        .build();
  }

  public Role createRole(TektonConfig config) {
    return new RoleBuilder()
        .withNewMetadata()
        .withName("pipeline-deployer")
        .endMetadata()
        .addNewRule()
        .withApiGroups("", "apps", "extensions", "serving.knative.dev", "apps.openshift.io")
        .withResources("deployments", "services", "ingresses", "serviceaccounts", "rolebindings",
            "persistentvolumeclaims", "configmaps", "secrets")
        .withVerbs("get", "create", "update", "patch")
        .endRule()
        .build();
  }

  public PipelineRun createPipelineRun(TektonConfig config) {
    return new PipelineRunBuilder()
        .withNewMetadata()
        .withName(config.getName() + DASH + RUN + DASH + NOW)
        .endMetadata()
        .withNewSpec()
        .withServiceAccountName(config.getName())
        .addNewWorkspace().withName(PIPELINE_SOURCE_WS)
        .withNewPersistentVolumeClaim(sourceWorkspaceClaimName(config), false)
        .endWorkspace()
        .withNewPipelineRef().withName(config.getName()).endPipelineRef()
        .addNewResource().withName(GIT_SOURCE).withNewResourceRef().withName(gitResourceName(config)).endResourceRef()
        .endResource()
        .addNewResource().withName(OUTPUT_IMAGE).withNewResourceRef().withName(outputImageResourceName(config))
        .endResourceRef().endResource()
        .withTimeout(DEFAULT_TIMEOUT)
        .endSpec()
        .build();
  }

  public TaskRun createTaskRun(TektonConfig config) {
    return new TaskRunBuilder()
        .withNewMetadata()
        .withName(config.getName() + DASH + RUN + DASH + NOW)
        .endMetadata()
        .withNewSpec()
        .withServiceAccountName(config.getName())
        .addNewWorkspace().withName(config.getSourceWorkspace())
        .withEmptyDir(new EmptyDirVolumeSourceBuilder().withMedium("Memory").build()).endWorkspace()
        .withNewTaskRef().withName(monolithTaskName(config)).endTaskRef()
        .withNewResources()
        .addNewInput().withName(GIT_SOURCE).withNewResourceRef().withName(gitResourceName(config)).endResourceRef()
        .endInput()
        .addNewOutput().withName(IMAGE).withNewResourceRef().withName(outputImageResourceName(config)).endResourceRef()
        .endOutput()
        .endResources()
        .withTimeout(DEFAULT_TIMEOUT)
        .endSpec()
        .build();
  }

  public PersistentVolumeClaim createSourceWorkspacePvc(TektonConfig config) {
    Map<String, Quantity> requests = new HashMap<String, Quantity>() {
      {
        put("storage", new QuantityBuilder().withAmount(String.valueOf(config.getSourceWorkspaceClaim().getSize()))
            .withFormat(config.getSourceWorkspaceClaim().getUnit()).build());
      }
    };
    LabelSelector selector = null;

    if (config.getSourceWorkspaceClaim().getMatchLabels().length != 0) {
      selector = new LabelSelectorBuilder()
          .withMatchLabels(Arrays.stream(config.getSourceWorkspaceClaim().getMatchLabels())
              .collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue())))
          .build();
    }
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(sourceWorkspaceClaimName(config))
        .endMetadata()
        .withNewSpec()
        .withAccessModes(config.getSourceWorkspaceClaim().getAccessMode().name())
        .withStorageClassName(config.getSourceWorkspaceClaim().getStorageClass())
        .withNewResources().withRequests(requests).endResources()
        .withSelector(selector)
        .endSpec()
        .build();
  }

  public PersistentVolumeClaim createM2WorkspacePvc(TektonConfig config) {
    Map<String, Quantity> requests = new HashMap<String, Quantity>() {
      {
        put("storage", new QuantityBuilder().withAmount(String.valueOf(config.getM2WorkspaceClaim().getSize()))
            .withFormat(config.getM2WorkspaceClaim().getUnit()).build());
      }
    };
    LabelSelector selector = null;
    if (config.getM2WorkspaceClaim().getMatchLabels().length != 0) {
      selector = new LabelSelectorBuilder()
          .withMatchLabels(Arrays.stream(config.getM2WorkspaceClaim().getMatchLabels())
              .collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue())))
          .build();
    }

    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(m2WorkspaceClaimName(config))
        .endMetadata()
        .withNewSpec()
        .withAccessModes(config.getM2WorkspaceClaim().getAccessMode().name())
        .withStorageClassName(config.getM2WorkspaceClaim().getStorageClass())
        .withNewResources().withRequests(requests).endResources()
        .withSelector(selector)
        .endSpec()
        .build();
  }

  @Override
  public ConfigurationSupplier<TektonConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<TektonConfig>(
        new TektonConfigBuilder().accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }

  private static ImageConfiguration getImageConfiguration(Project project, TektonConfig tektonConfig,
      Configurators configurators) {
    return configurators.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(tektonConfig, i))
        .orElse(ImageConfiguration.from(tektonConfig));
  }

  private static ImageConfiguration merge(TektonConfig tektonConfig, ImageConfiguration imageConfig) {
    if (tektonConfig == null) {
      throw new NullPointerException("KubernetesConfig is null.");
    }
    if (imageConfig == null) {
      return ImageConfiguration.from(tektonConfig);
    }
    return new ImageConfigurationBuilder()
        .withProject(imageConfig.getProject() != null ? imageConfig.getProject() : tektonConfig.getProject())
        .withGroup(imageConfig.getGroup() != null ? imageConfig.getGroup() : null)
        .withName(imageConfig.getName() != null ? imageConfig.getName() : tektonConfig.getName())
        .withVersion(imageConfig.getVersion() != null ? imageConfig.getVersion() : tektonConfig.getVersion())
        .withRegistry(imageConfig.getRegistry() != null ? imageConfig.getRegistry() : "docker.io")
        .withDockerFile(imageConfig.getDockerFile() != null ? imageConfig.getDockerFile() : "Dockerfile")
        .withAutoBuildEnabled(imageConfig.isAutoBuildEnabled() ? imageConfig.isAutoBuildEnabled() : false)
        .withAutoPushEnabled(imageConfig.isAutoPushEnabled() ? imageConfig.isAutoPushEnabled() : false)
        .build();
  }

  public static final String getContextPath(Project project) {
    Path root = project != null && project.getScmInfo() != null ? project.getScmInfo().getRoot() : null;
    Path module = project != null ? project.getRoot() : null;

    String result = "";
    if (root != null && module != null) {
      result = module.toAbsolutePath().toString().substring(root.toAbsolutePath().toString().length());
    }
    if (Strings.isNullOrEmpty(result)) {
      result = "./";
    }
    return result;
  }

  public static final String getYamlPath(Project project) {
    Path root = project != null && project.getScmInfo() != null ? project.getScmInfo().getRoot() : null;
    Path module = project != null ? project.getRoot() : null;

    if (root != null && module != null) {
      return module.toAbsolutePath().resolve(DeployStep.PATH_TO_YML_PARAM_DEFAULT).toAbsolutePath().toString()
          .substring(root.toAbsolutePath().toString().length() + 1);
    } else {
      return DeployStep.PATH_TO_YML_PARAM_DEFAULT;
    }
  }

  public static final String gitResourceName(TektonConfig config) {
    return Strings.isNotNullOrEmpty(config.getExternalGitPipelineResource()) ? config.getExternalGitPipelineResource()
        : config.getName() + DASH + GIT;
  }

  public static final String sourceWorkspaceClaimName(TektonConfig config) {
    return Strings.isNotNullOrEmpty(config.getExternalSourceWorkspaceClaim())
        ? config.getExternalSourceWorkspaceClaim()
        : (Strings.isNotNullOrEmpty(config.getSourceWorkspaceClaim().getName())
            ? config.getSourceWorkspaceClaim().getName()
            : config.getName());
  }

  public static final String m2WorkspaceClaimName(TektonConfig config) {
    return Strings.isNotNullOrEmpty(config.getExternalM2WorkspaceClaim())
        ? config.getExternalM2WorkspaceClaim()
        : (Strings.isNotNullOrEmpty(config.getM2WorkspaceClaim().getName()) ? config.getM2WorkspaceClaim().getName()
            : null);
  }

  public static final String projectBuildStepName(TektonConfig config) {
    return ProjectBuildStep.ID;
  }

  public static final String outputImageResourceName(TektonConfig config) {
    return config.getName() + DASH + IMAGE;
  }

  public static final String imageBuildTaskName(TektonConfig config) {
    return config.getName() + DASH + IMAGE + DASH + BUILD;
  }

  public static final String projectBuildTaskName(TektonConfig config) {
    return config.getName() + DASH + PROJECT + DASH + BUILD;
  }

  public static final String deployTaskName(TektonConfig config) {
    return config.getName() + DASH + DEPLOY;
  }

  public static final String monolithTaskName(TektonConfig config) {
    return config.getName() + DASH + BUILD + DASH + AND + DASH + DEPLOY;
  }
}
