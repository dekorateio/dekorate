package io.ap4k.docker.registrar;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.SessionListener;
import io.ap4k.WithProject;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.docker.adapter.DockerBuildConfigAdapter;
import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.configurator.ApplyDockerBuildHook;
import io.ap4k.docker.decorator.ApplyRegistryToImageDecorator;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.docker.hook.DockerPushHook;
import io.ap4k.docker.hook.ScaleDeploymentHook;
import io.ap4k.hook.OrderedHook;
import io.ap4k.hook.ProjectHook;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.utils.Strings;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EnableDockerBuildRegistrar extends Generator, SessionListener, WithProject {

  String DEFAULT_REGISTRY = "docker.io";

  default void add(Element mainClass) {
    EnableDockerBuild enableDockerBuild = mainClass.getAnnotation(EnableDockerBuild.class);
    on(new ConfigurationSupplier<>(DockerBuildConfigAdapter
      .newBuilder(enableDockerBuild)
      .accept(new ApplyDockerBuildHook())));
  }


  default void add(Map map) {
    on(new ConfigurationSupplier<>(DockerBuildConfigAdapter
      .newBuilder(propertiesMap(map, EnableDockerBuild.class))
      .accept(new ApplyDockerBuildHook())));
  }

  default void on(ConfigurationSupplier<DockerBuildConfig> config) {
      session.configurators().add(config);
      DockerBuildConfig buildConfig = config.get();
      if  (Strings.isNotNullOrEmpty(buildConfig.getRegistry()))  {
        session.resources().decorate(new ApplyRegistryToImageDecorator(session.resources(), buildConfig.getRegistry()));
      } else if (buildConfig.isAutoPushEnabled()) {
        session.resources().decorate(new ApplyRegistryToImageDecorator(session.resources(), DEFAULT_REGISTRY));
      }
      session.addListener(this);
  }

  default void onClosed() {
    Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
    if (!config.isPresent()) {
      return;
    }
    DockerBuildConfig dockerBuildConfig = config.get();
    Resources resources = session.resources();
    if (dockerBuildConfig.isAutoPushEnabled()) {
      // When deploy is enabled, we scale the Deployment down before push
      // then scale it back up once the image has been successfully pushed
      // This ensure that the pod runs the proper image
      List<ProjectHook> hooks = new ArrayList<>();
      if (isDeployEnabled()) {
        hooks.add(new ScaleDeploymentHook(getProject(), session.resources().getName(), 0));
      }
      hooks.add(new DockerBuildHook(getProject(), resources, config.get()));
      hooks.add(new DockerPushHook(getProject(), resources, config.get()));
      if (isDeployEnabled()) {
        hooks.add(new ScaleDeploymentHook(getProject(), session.resources().getName(), 1));
      }
      OrderedHook hook = OrderedHook.create(hooks.toArray(new ProjectHook[hooks.size()]));
      hook.register();
    } else if (dockerBuildConfig.isAutoBuildEnabled()) {
      DockerBuildHook hook = new DockerBuildHook(getProject(), resources, config.get());
      hook.register();
    }
  }

  default boolean isDeployEnabled() {
    Optional<KubernetesConfig> config = session.configurators().get(KubernetesConfig.class);
    return config.map(KubernetesConfig::isAutoDeployEnabled).orElse(false);
  }
}
