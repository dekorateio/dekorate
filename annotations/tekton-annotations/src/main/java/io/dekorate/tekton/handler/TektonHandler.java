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
import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.BuildServiceFactories;
import io.dekorate.BuildImage;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.EnvVarBuilder;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.tekton.annotation.TektonApplication;
import io.dekorate.tekton.config.EditableTektonConfig;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.tekton.config.TektonConfigBuilder;
import io.dekorate.tekton.decorator.AddToArgsDecorator;
import io.dekorate.tekton.decorator.AddWorkspaceToTaskDecorator;
import io.dekorate.deps.tekton.pipeline.v1beta1.Pipeline;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineRun;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineRunBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.Task;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskBuilder;
import io.dekorate.deps.tekton.resource.v1alpha1.PipelineResource;
import io.dekorate.deps.tekton.resource.v1alpha1.PipelineResourceBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.dekorate.project.ScmInfo;
import io.dekorate.utils.Images;
import io.dekorate.tekton.config.EditableTektonConfig;
import io.dekorate.tekton.config.TektonConfig;
import io.dekorate.tekton.config.TektonConfigBuilder;
import io.dekorate.utils.Jvm;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

public class TektonHandler implements Handler<TektonConfig>, HandlerFactory, WithProject {

  private static final String TEKTON = "tekton";
  private static final String GIT = "git";
  private static final String REVISION = "revision";
  private static final String URL = "url";
  private static final String IMAGE = "image";
  private static final String BUILD = "build";
  private static final String BUILD_AND_PUSH = "build-and-push";
  private static final String DEPLOY = "deploy";
  private static final String SOURCE = "source";

  private static final String JAVA = "java";

  private static final String DASH = "-";

  private static final String MAVEN_LOCAL_REPO_SYS_PROPERTY = "-Dmaven.repo.local=%s";

  private static final String PARAMS_FORMAT = "${params.%s}";
  private static final String RESOURCES_INPUTS_FORMAT = "$(resources.inputs.%s.path)";
  private static final String PATH_TO_FILE_FORMAT = "$(resources.inputs.source.path)/%s/$(inputs.params.pathToContext)/%s";

  private static final String PATH_TO_YML_PARAM_NAME = "pathToYml";
  private static final String PATH_TO_YML_DESCRIPTION = "Path to yml";
  private static final String PATH_TO_YML_DEFAULT = "target/classes/META-INF/dekorate/kubernetes.yml";

  private static final String PATH_TO_CONTEXT_PARAM_NAME = "pathToContext";
  private static final String PATH_TO_CONTEXT_DESCRIPTION = "Path to context. Usually refers to module directory";

  private static final String PATH_TO_DOCKERFILE_PARAM_NAME = "pathToDockerfile";
  private static final String PATH_TO_DOCKERFILE_DESCRIPTION = "Path to Dockerfile";
  private static final String PATH_TO_DOCKERFILE_DEFAULT = "Dockerfile";

  private static final String BUILDER_IMAGE_PARAM_NAME = "builderImage";
  private static final String BUILDER_IMAGE_DESCRIPTION = "The image to use for performing image build";
  private static final String BUILDER_IMAGE_DEFAULT = "gcr.io/kaniko-project/executor:latest";
  private static final String BUILDER_IMAGE_REF = "$(inputs.params.builderImage)";

  private static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private static final String KANIKO_CMD = "/kaniko/executor";
  private static final String DOCKERFILE_ARG = "--dockerfile=$(inputs.params.pathToDockerfile)";
  private static final String CONTEXT_ARG = "--context=$(inputs.params.pathToContext)";
  private static final String IMAGE_DESTINATION_ARG = "--destination=$(outputs.resources.image.url)";


  private static final String DEPLOY_CMD = "kubectl";

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

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(TektonConfig.class) ||
      type.equals(EditableTektonConfig.class);
  }

  public void handle(TektonConfig config) {
    Optional<Task> buildTask = createJavaBuildTask(config);
    Optional<PipelineResource> gitResource = createGitResource(config);
    ImageConfiguration imageConfiguration = getImageConfiguration(getProject(), config, configurators);

    resources.add(TEKTON, createOutputImageResource(config, imageConfiguration));

    buildTask.ifPresent(t -> resources.add(TEKTON, t));
    gitResource.ifPresent(t -> {
        resources.add(TEKTON, t);
        resources.add(TEKTON, createImageBuildTask(config));
        resources.add(TEKTON, createDeployTask(config));
        resources.add(TEKTON, createPipeline(config));
    });

    if (Strings.isNotNullOrEmpty(config.getArtifactRepositoryWorkspace())) {
      String taskName = javaBuildTaskName(config);
      String stepName = javaBuildStepName(config);
      resources.decorate(TEKTON, new AddWorkspaceToTaskDecorator(taskName, config.getArtifactRepositoryWorkspace(), "Local maven repository workspace", false, config.getArtifactRepositoryPath()));
      resources.decorate(TEKTON, new AddToArgsDecorator(taskName, stepName, String.format(MAVEN_LOCAL_REPO_SYS_PROPERTY, config.getArtifactRepositoryPath())));
    }
  }

  public Optional<PipelineResource> createGitResource(TektonConfig config) {
    Optional<ScmInfo> scmInfo = Optional.ofNullable(config.getProject().getScmInfo());
    return scmInfo.map(s ->  new PipelineResourceBuilder()
                       .withNewMetadata()
                       .withName(gitResourceName(config))
                       .endMetadata()
                       .withNewSpec()
                       .withType(GIT)
                       .addNewParam()
                       .withName(URL)
                       .withValue(s.getUrl())
                       .endParam()
                       .addNewParam()
                       .withName(REVISION)
                       .withValue(s.getCommit())
                       .endParam()
                       .endSpec()
                       .build());
  }


  public PipelineResource createOutputImageResource(TektonConfig config, ImageConfiguration imageConfig) {
    String image = Images.getImage(Strings.isNotNullOrEmpty(imageConfig.getRegistry()) ? imageConfig.getRegistry() : "docker.io",
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

  public Optional<Task> createJavaBuildTask(TektonConfig config) {
    BuildInfo build = config.getProject().getBuildInfo();
    Optional<BuildImage> image = BuildImage.find(build.getBuildTool(), build.getBuildToolVersion(), Jvm.getVersion(), null);
    return image.map(i -> new TaskBuilder()
      .withNewMetadata()
      .withName(javaBuildTaskName(config))
      .endMetadata()
      .withNewSpec()
        .withNewResources()
          .addNewInput()
            .withName(SOURCE)
             .withType(GIT)
          .endInput()
        .endResources()
        .addNewParam()
          .withName(PATH_TO_CONTEXT_PARAM_NAME)
          .withDescription(PATH_TO_CONTEXT_DESCRIPTION)
          .withNewDefault()
            .withStringVal(getContextPath(config.getProject()))
          .endDefault()
        .endParam()
      .addNewStep()
        .withName(JAVA + DASH + BUILD)
        .withImage(i.getImage())
        .withCommand(i.getCommand())
        .withWorkingDir(String.format(RESOURCES_INPUTS_FORMAT, SOURCE))
      .endStep()
      .endSpec()
      .build());
  }

  public Task createImageBuildTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
      .withName(imageBuildTaskName(config))
      .endMetadata()
        .withNewSpec()
          .withNewResources()
            .addNewInput()
              .withName(SOURCE)
               .withType(GIT)
            .endInput()
            .addNewOutput()
              .withName(IMAGE)
              .withType(IMAGE)
            .endOutput()
          .endResources()
          .addNewParam()
            .withName(PATH_TO_CONTEXT_PARAM_NAME)
            .withDescription(PATH_TO_CONTEXT_DESCRIPTION)
            .withNewDefault()
              .withStringVal(getContextPath(config.getProject()))
            .endDefault()
          .endParam()
          .addNewParam()
            .withName(PATH_TO_DOCKERFILE_PARAM_NAME)
            .withDescription(PATH_TO_DOCKERFILE_DESCRIPTION)
            .withNewDefault()
              .withStringVal(PATH_TO_DOCKERFILE_DEFAULT)
            .endDefault()
          .endParam()
         .addNewParam()
           .withName(BUILDER_IMAGE_PARAM_NAME)
           .withDescription(BUILDER_IMAGE_DESCRIPTION)
            .withNewDefault()
              .withStringVal(BUILDER_IMAGE_DEFAULT)
            .endDefault()
         .endParam()
         .addNewStep()
          .withName(BUILD_AND_PUSH)
          .withImage(BUILDER_IMAGE_REF)
          .addToEnv(new EnvVarBuilder().withName(DOCKER_CONFIG).withValue(DOCKER_CONFIG_DEFAULT).build())
          .withCommand(KANIKO_CMD)
          .addToArgs(DOCKERFILE_ARG)
          .addToArgs(CONTEXT_ARG)
          .addToArgs(IMAGE_DESTINATION_ARG)
          .withWorkingDir(String.format(RESOURCES_INPUTS_FORMAT, SOURCE))
         .endStep()
        .endSpec()
      .build();
  }

  public Task createDeployTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
        .withName(deployTaskName(config))
      .endMetadata()
        .withNewSpec()
          .withNewResources()
            .addNewInput()
              .withName(SOURCE)
               .withType(GIT)
            .endInput()
          .endResources()
          .addNewParam()
            .withName(PATH_TO_CONTEXT_PARAM_NAME)
            .withDescription(PATH_TO_CONTEXT_DESCRIPTION)
            .withNewDefault()
              .withStringVal(getContextPath(config.getProject()))
            .endDefault()
          .endParam()
          .addNewParam()
            .withName(PATH_TO_YML_PARAM_NAME)
            .withDescription(PATH_TO_YML_DESCRIPTION)
            .withNewDefault()
             .withStringVal(String.format(PATH_TO_FILE_FORMAT, SOURCE, PATH_TO_YML_DEFAULT))
            .endDefault()
          .endParam()
          .addNewStep()
            .withName(DEPLOY)
            .withImage(config.getDeployerImage())
            .withCommand(DEPLOY_CMD)
            .withArgs(deployArgs(config))
            .withWorkingDir(String.format(RESOURCES_INPUTS_FORMAT, SOURCE))
          .endStep()
        .endSpec()
      .build();
  }

  public Pipeline createPipeline(TektonConfig config) {
    return new PipelineBuilder()
      .withNewMetadata()
        .withName(config.getName())
      .endMetadata()
      .withNewSpec()
        .addNewResource()
          .withType(GIT)
          .withName(gitResourceName(config))
        .endResource()
        .addNewResource()
          .withType(IMAGE)
         .withName(outputImageResourceName(config))
        .endResource()
          .addNewParam()
            .withName(PATH_TO_CONTEXT_PARAM_NAME)
            .withDescription(PATH_TO_CONTEXT_DESCRIPTION)
            .withNewDefault()
              .withStringVal(getContextPath(config.getProject()))
            .endDefault()
          .endParam()
          .addNewParam()
            .withName(PATH_TO_DOCKERFILE_PARAM_NAME)
            .withDescription(PATH_TO_DOCKERFILE_DESCRIPTION)
            .withNewDefault()
              .withStringVal(String.format(PATH_TO_FILE_FORMAT, SOURCE, PATH_TO_DOCKERFILE_DEFAULT))
            .endDefault()
          .endParam()
          .addNewParam()
            .withName(PATH_TO_YML_PARAM_NAME)
            .withDescription(PATH_TO_YML_DESCRIPTION)
            .withNewDefault()
             .withStringVal(String.format(PATH_TO_FILE_FORMAT, SOURCE, PATH_TO_YML_DEFAULT))
            .endDefault()
          .endParam()
        .addNewTask()
          .withName(BUILD)
            .withNewTaskRef()
              .withName(javaBuildTaskName(config))
            .endTaskRef()
        .withNewResources().addNewInput().withName(SOURCE).withResource(gitResourceName(config)).endInput().endResources()
        .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(String.format(PARAMS_FORMAT, PATH_TO_CONTEXT_PARAM_NAME)).endParam()
        .endTask()
        .addNewTask()
          .withName(IMAGE)
            .withNewTaskRef()
              .withName(imageBuildTaskName(config))
            .endTaskRef()
          .withNewResources()
            .addNewInput().withName(SOURCE).withResource(gitResourceName(config)).endInput()
            .addNewOutput().withName(IMAGE).withResource(outputImageResourceName(config)).endOutput()
          .endResources()
        .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(String.format(PARAMS_FORMAT, PATH_TO_CONTEXT_PARAM_NAME)).endParam()
        .withRunAfter(BUILD)
        .endTask()
        .addNewTask()
          .withName(DEPLOY)
            .withNewTaskRef()
              .withName(deployTaskName(config))
            .endTaskRef()
        .withNewResources().addNewInput().withName(SOURCE).withResource(gitResourceName(config)).endInput().endResources()
        .addNewParam().withName(PATH_TO_YML_PARAM_NAME).withNewValue(String.format(PARAMS_FORMAT, PATH_TO_YML_PARAM_NAME)).endParam()
        .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(String.format(PARAMS_FORMAT, PATH_TO_CONTEXT_PARAM_NAME)).endParam()
        .withRunAfter(BUILD, IMAGE)
        .endTask()
      .endSpec()
      .build();
  }

  public PipelineRun createPipelineRun(TektonConfig config) {
    return new PipelineRunBuilder()
      .withNewMetadata()
        .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .endSpec()
      .build();
  }


  @Override
  public ConfigurationSupplier<TektonConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<TektonConfig>(new TektonConfigBuilder().accept(new ApplyDeployToApplicationConfiguration()).accept(new ApplyProjectInfo(p)));
  }

  private static ImageConfiguration getImageConfiguration(Project project, TektonConfig tektonConfig, Configurators configurators) {
    Optional<ImageConfiguration> origin = configurators.getImageConfig(BuildServiceFactories.supplierMatches(project));
    return configurators.getImageConfig(BuildServiceFactories.supplierMatches(project)).map(i -> merge(tektonConfig, i)).orElse(ImageConfiguration.from(tektonConfig));
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

    if (root != null && module != null) {
      return module.toAbsolutePath().toString().substring(root.toAbsolutePath().toString().length());
    } else {
      return "";
    }
  }

  public static final String[] deployArgs(TektonConfig config ) {
    return new String[] {"apply", "-f", String.format(PARAMS_FORMAT, PATH_TO_YML_PARAM_NAME)};
  }

  public static final String gitResourceName(TektonConfig config) {
    return config.getName() + DASH + GIT;
  }

  public static final String outputImageResourceName(TektonConfig config) {
    return config.getName() + DASH + IMAGE;
  }

  public static final String imageBuildTaskName(TektonConfig config) {
    return config.getName() + DASH + IMAGE + DASH + BUILD;
  }

  public static final String javaBuildTaskName(TektonConfig config) {
    return config.getName() + DASH + JAVA + DASH + BUILD;
  }

  public static final String javaBuildStepName(TektonConfig config) {
    return JAVA + DASH + BUILD;
  }

  public static final String deployTaskName(TektonConfig config) {
    return config.getName() + DASH + DEPLOY;
  }
}
