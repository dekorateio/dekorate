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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.dekorate.BuildImage;
import io.dekorate.BuildServiceFactories;
import io.dekorate.Configurators;
import io.dekorate.DekorateException;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.EnvVarBuilder;
import io.dekorate.deps.kubernetes.api.model.PersistentVolume;
import io.dekorate.deps.kubernetes.api.model.PersistentVolumeBuilder;
import io.dekorate.deps.kubernetes.api.model.PersistentVolumeClaim;
import io.dekorate.deps.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.dekorate.deps.kubernetes.api.model.Quantity;
import io.dekorate.deps.kubernetes.api.model.QuantityBuilder;
import io.dekorate.deps.kubernetes.api.model.rbac.Role;
import io.dekorate.deps.kubernetes.api.model.rbac.RoleBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.Pipeline;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineRun;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineRunBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.Step;
import io.dekorate.deps.tekton.pipeline.v1beta1.StepBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.Task;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskBuilder;
import io.dekorate.deps.tekton.resource.v1alpha1.PipelineResource;
import io.dekorate.deps.tekton.resource.v1alpha1.PipelineResourceBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.kubernetes.config.ImageConfigurationBuilder;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.decorator.AddBasicAuthSecretDecorator;
import io.dekorate.kubernetes.decorator.AddDockerConfigJsonSecretDecorator;
import io.dekorate.kubernetes.decorator.AddImagePullSecretDecorator;
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
import io.dekorate.tekton.decorator.AddContextParamToTaskDecorator;
import io.dekorate.tekton.decorator.AddDeployStepDecorator;
import io.dekorate.tekton.decorator.AddImageBuildStepDecorator;
import io.dekorate.tekton.decorator.AddInitStepDecorator;
import io.dekorate.tekton.decorator.AddJavaBuildStepDecorator;
import io.dekorate.tekton.decorator.AddParamToTaskDecorator;
import io.dekorate.tekton.decorator.AddResourceToTaskDecorator;
import io.dekorate.tekton.decorator.AddServiceAccountToTaskDecorator;
import io.dekorate.tekton.decorator.AddToArgsDecorator;
import io.dekorate.tekton.decorator.AddWorkspaceToTaskDecorator;
import io.dekorate.utils.Images;
import io.dekorate.utils.Jvm;
import io.dekorate.utils.Strings;

public class TektonHandler implements Handler<TektonConfig>, HandlerFactory, WithProject {

  private static final Logger LOGGER = LoggerFactory.getLogger();

  private static final String TEKTON = "tekton";
  private static final String TEKTON_RUN = "tekton-run";

  private static final String GIT = "git";
  private static final String REVISION = "revision";
  private static final String URL = "url";
  private static final String IMAGE = "image";
  private static final String BUILD = "build";
  private static final String DEPLOY = "deploy";
  private static final String SOURCE = "source";
  private static final String WORKSPACE = "workspace";
  private static final String INIT = "init";
  private static final String RUN = "run";
  private static final String NOW = "now";

  private static final String GIT_SOURCE = "git-source";
  private static final String OUTPUT_IMAGE = "out-image";

  private static final String PIPELINE_SOURCE_WS = "pipeline-source-ws";

  private static final String JAVA = "java";
  private static final String DASH = "-";

  private static final String MAVEN_LOCAL_REPO_SYS_PROPERTY = "-Dmaven.repo.local=%s";
  private static final String IMAGE_PULL_SECRETS_SYS_PROPERTY = "-Ddekorate.image-pull-secrets=";
  private static final String USER_NAME_SYS_PROP = "-Duser.name=";

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
  private static final String BUILDER_IMAGE_DEFAULT = "gcr.io/kaniko-project/executor:v0.18.0";

  private static final String BUSYBOX = "busybox";
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

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(TektonConfig.class) ||
      type.equals(EditableTektonConfig.class);
  }

  public void handle(TektonConfig config) {
    PipelineResource gitResource = createGitResource(config).orElse(null);

    if (gitResource == null) {
      LOGGER.warning("Project is not under version control, or unsupported version control system. Aborting generation of tekton resources!");
      return;
    }

    ImageConfiguration imageConfiguration = getImageConfiguration(getProject(), config, configurators);
    Map<String, String> annotations = new HashMap<String, String>() {{
        put("tekton.dev/docker-0", "https://" + imageConfiguration.getRegistry());
    }};

    resources.add(TEKTON, gitResource);
    resources.add(TEKTON, createOutputImageResource(config, imageConfiguration));
    resources.add(TEKTON, createPvc(config));
    resources.add(TEKTON, createRole(config));

    //All Tasks
    resources.decorate(TEKTON, new AddWorkspaceToTaskDecorator(null, SOURCE, "The workspace to hold all project sources", false, null));
    resources.decorate(TEKTON, new AddParamToTaskDecorator(null, PATH_TO_CONTEXT_PARAM_NAME, PATH_TO_CONTEXT_DESCRIPTION, getContextPath(getProject())));

    //Workspace Init
    String initTaskName = workspaceInitTaskName(config);
    resources.add(TEKTON, createWorkspaceInitTask(config));
    resources.decorate(TEKTON, new AddInitStepDecorator(initTaskName, GIT_SOURCE, config.getName()));
    resources.decorate(TEKTON, new AddResourceToTaskDecorator(initTaskName, GIT, GIT_SOURCE));

    //Java Build
    BuildInfo build = config.getProject().getBuildInfo();
    BuildImage image = BuildImage.find(build.getBuildTool(), build.getBuildToolVersion(), Jvm.getVersion(), null).orElseThrow(() -> new IllegalStateException("No java builder image was found!"));

    String javaBuildTaskName = javaBuildTaskName(config);
    String javaBuildStepName = javaBuildStepName(config);
    resources.add(TEKTON, createJavaBuildTask(config));
    resources.decorate(TEKTON, new AddJavaBuildStepDecorator(javaBuildTaskName(config), config.getName(), image));

    //Image Build
    String imageBuildTaskName = imageBuildTaskName(config);
    resources.add(TEKTON, createImageBuildTask(config));
    resources.decorate(TEKTON, new AddImageBuildStepDecorator(imageBuildTaskName, config.getName()));
    resources.decorate(TEKTON, new AddParamToTaskDecorator(imageBuildTaskName, PATH_TO_DOCKERFILE_PARAM_NAME, PATH_TO_DOCKERFILE_DESCRIPTION, PATH_TO_DOCKERFILE_DEFAULT));
    resources.decorate(TEKTON, new AddParamToTaskDecorator(imageBuildTaskName, BUILDER_IMAGE_PARAM_NAME, BUILDER_IMAGE_DESCRIPTION, BUILDER_IMAGE_DEFAULT));

    //Deploy Task
    String deployTaskName = deployTaskName(config);
    resources.add(TEKTON, createDeployTask(config));
    resources.decorate(TEKTON, new AddDeployStepDecorator(deployTaskName, config.getName(), config.getDeployerImage()));
    resources.decorate(TEKTON, new AddParamToTaskDecorator(deployTaskName, PATH_TO_YML_PARAM_NAME, PATH_TO_YML_DESCRIPTION, PATH_TO_YML_DEFAULT));

    resources.add(TEKTON, createPipeline(config));
    resources.decorate(TEKTON, new AddServiceAccountResourceDecorator());
    resources.decorate(TEKTON, new AddRoleBindingResourceDecorator(AddRoleBindingResourceDecorator.RoleKind.Role, "pipeline-deployer"));

    resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, String.format(MAVEN_LOCAL_REPO_SYS_PROPERTY, config.getArtifactRepositoryPath())));
    resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, USER_NAME_SYS_PROP + imageConfiguration.getGroup()));

    if (Strings.isNotNullOrEmpty(config.getArtifactRepositoryWorkspace())) {
      resources.decorate(TEKTON, new AddWorkspaceToTaskDecorator(javaBuildTaskName, config.getArtifactRepositoryWorkspace(), "Local maven repository workspace", false, config.getArtifactRepositoryPath()));
      resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, String.format(MAVEN_LOCAL_REPO_SYS_PROPERTY, config.getArtifactRepositoryPath())));
    }

    if (Strings.isNotNullOrEmpty(config.getImageBuilderServiceAccount())) {
      resources.decorate(TEKTON_RUN, new AddServiceAccountToTaskDecorator(imageBuildTaskName(config), config.getImageBuilderServiceAccount()));
    } else {
      String generatedServiceAccount = config.getName();
      resources.decorate(TEKTON_RUN, new AddServiceAccountToTaskDecorator(imageBuildTaskName(config), generatedServiceAccount));
      if (Strings.isNotNullOrEmpty(config.getImageBuilderSecret())) {
        resources.decorate(TEKTON, new AddSecretToServiceAccountDecorator(generatedServiceAccount, config.getImageBuilderSecret()));
        resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, IMAGE_PULL_SECRETS_SYS_PROPERTY + config.getImageBuilderSecret()));
      } else if (config.isUseLocalDockerConfigJson()) {
        String generatedSecret = config.getName() + "-registry-credentials" ;
        Path dockerConfigJson = Paths.get(System.getProperty("user.home"), ".docker", "config.json");
        if (!dockerConfigJson.toFile().exists()) {
          throw new IllegalStateException("User requested to use the local `.docker/config.json` file, but it doesn't exist!");
        } else {
          LOGGER.warning(dockerConfigJson.toAbsolutePath().toString() + " is going to be added as part of Secret: "+ generatedSecret);
        }
        resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, IMAGE_PULL_SECRETS_SYS_PROPERTY + generatedSecret));
        resources.decorate(TEKTON, new AddDockerConfigJsonSecretDecorator(generatedSecret, dockerConfigJson, annotations));
        resources.decorate(TEKTON, new AddSecretToServiceAccountDecorator(generatedServiceAccount, generatedSecret));
      } else if (Strings.isNotNullOrEmpty(config.getRegistryUsername()) && Strings.isNotNullOrEmpty(config.getRegistryPassword())) {
        String generatedSecret = config.getName() + "-registry-credentials" ;
        resources.decorate(TEKTON, new AddToArgsDecorator(javaBuildTaskName, javaBuildStepName, IMAGE_PULL_SECRETS_SYS_PROPERTY + generatedSecret));
        resources.decorate(TEKTON, new AddDockerConfigJsonSecretDecorator(generatedSecret, config.getRegistry(), config.getRegistryUsername(), config.getRegistryPassword(), annotations));
        resources.decorate(TEKTON, new AddSecretToServiceAccountDecorator(generatedServiceAccount, generatedSecret));
      } else {
        LOGGER.error("An existing builder image service account or secret is required! Alternatively, you can specify a registry username and password!");
      }
    }
    resources.add(TEKTON_RUN, createPipelineRun(config));
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

  public Task createWorkspaceInitTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
        .withName(workspaceInitTaskName(config))
      .endMetadata()
      .withNewSpec()
      .endSpec()
      .build();
  }

  public Task createJavaBuildTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
      .withName(javaBuildTaskName(config))
      .endMetadata()
      .withNewSpec()
      .endSpec()
      .build();
  }

  public Task createImageBuildTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
      .withName(imageBuildTaskName(config))
      .endMetadata()
        .withNewSpec()
          .withNewResources()
            .addNewOutput()
              .withName(IMAGE)
              .withType(IMAGE)
            .endOutput()
          .endResources()
        .endSpec()
      .build();
  }

  public Task createDeployTask(TektonConfig config) {
    return new TaskBuilder()
      .withNewMetadata()
        .withName(deployTaskName(config))
      .endMetadata()
        .withNewSpec()
        .endSpec()
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
        .addNewTask()
          .withName(INIT)
          .withNewTaskRef()
            .withName(workspaceInitTaskName(config))
          .endTaskRef()
          .addNewWorkspace().withName(SOURCE).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
          .withNewResources().addNewInput().withName(GIT_SOURCE).withResource(GIT_SOURCE).endInput().endResources()
        .endTask()
        .addNewTask()
          .withName(BUILD)
          .withNewTaskRef()
            .withName(javaBuildTaskName(config))
          .endTaskRef()
          .addNewWorkspace().withName(SOURCE).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
          .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
          .withRunAfter(INIT)
        .endTask()
        .addNewTask()
          .withName(IMAGE)
            .withNewTaskRef()
              .withName(imageBuildTaskName(config))
            .endTaskRef()
          .withNewResources()
            .addNewOutput().withName(IMAGE).withResource(OUTPUT_IMAGE).endOutput()
          .endResources()
        .addNewWorkspace().withName(SOURCE).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
        .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
        .withRunAfter(INIT, BUILD)
        .endTask()
        .addNewTask()
          .withName(DEPLOY)
            .withNewTaskRef()
              .withName(deployTaskName(config))
            .endTaskRef()
        .addNewWorkspace().withName(SOURCE).withWorkspace(PIPELINE_SOURCE_WS).endWorkspace()
        .addNewParam().withName(PATH_TO_YML_PARAM_NAME).withNewValue(getYamlPath(getProject())).endParam()
        .addNewParam().withName(PATH_TO_CONTEXT_PARAM_NAME).withNewValue(getContextPath(getProject())).endParam()
        .withRunAfter(INIT, BUILD, IMAGE)
        .endTask()
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
      .withResources("deployments", "services", "ingresses", "serviceaccounts", "rolebindings", "persistentvolumeclaims", "configmaps", "secrets")
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
          .withNewPersistentVolumeClaim(config.getName(), false)
        .endWorkspace()
        .withNewPipelineRef().withName(config.getName()).endPipelineRef()
        .addNewResource().withName(GIT_SOURCE).withNewResourceRef().withName(gitResourceName(config)).endResourceRef().endResource()
        .addNewResource().withName(OUTPUT_IMAGE).withNewResourceRef().withName(outputImageResourceName(config)).endResourceRef().endResource()
        .withTimeout(DEFAULT_TIMEOUT)
      .endSpec()
      .build();
  }

  public PersistentVolumeClaim createPvc(TektonConfig config) {
    Map<String, Quantity> requests = new HashMap<String, Quantity>() {{
        put("storage", new QuantityBuilder().withAmount("1Gi").build());
    }};
    return new PersistentVolumeClaimBuilder()
      .withNewMetadata()
        .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withAccessModes("ReadWriteOnce")
      .withStorageClassName("standard")
      .withNewResources().withRequests(requests).endResources()
      .endSpec()
      .build();
  }

public PersistentVolume createHostPathPv(TektonConfig config) {
    Map<String, Quantity> capacity = new HashMap<String, Quantity>() {{
        put("storage", new QuantityBuilder().withAmount("1Gi").build());
    }};
    return new PersistentVolumeBuilder()
      .withNewMetadata()
        .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withNewHostPath("/home/docker/tekton", "DirectoryOrCreate")
      .withStorageClassName("standard")
      .withVolumeMode("Filesystem")
      .withAccessModes("ReadWriteOnce")
      .withCapacity(capacity)
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
      return module.toAbsolutePath().resolve(PATH_TO_YML_DEFAULT).toAbsolutePath().toString().substring(root.toAbsolutePath().toString().length() + 1);
    } else {
      return PATH_TO_YML_DEFAULT;
    }
  }


  public static final String gitResourceName(TektonConfig config) {
    return config.getName() + DASH + GIT;
  }

  public static final String outputImageResourceName(TektonConfig config) {
    return config.getName() + DASH + IMAGE;
  }

  public static final String workspaceInitTaskName(TektonConfig config) {
    return config.getName() + DASH + WORKSPACE + DASH + INIT;
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
