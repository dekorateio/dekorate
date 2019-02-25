package io.ap4k.docker.registrar;

import io.ap4k.SessionListener;
import io.ap4k.WithProject;
import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.docker.adapter.DockerBuildConfigAdapter;
import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.configurator.ApplyDockerBuildHook;
import io.ap4k.docker.configurator.ApplyProjectInfoToDockerBuildConfigDecorator;
import io.ap4k.docker.decorator.ApplyRegistryToImageDecorator;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.docker.hook.DockerPushHook;
import io.ap4k.hook.OrderedHook;
import io.ap4k.utils.Strings;

import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Optional;

public interface EnableDockerBuildRegistrar extends Generator, SessionListener, WithProject {

  String DEFAULT_REGISTRY = "docker.io";

  default void add(Element mainClass) {
    EnableDockerBuild enableDockerBuild = mainClass.getAnnotation(EnableDockerBuild.class);
    on(new ConfigurationSupplier<DockerBuildConfig>(DockerBuildConfigAdapter
      .newBuilder(enableDockerBuild)
      .accept(new ApplyProjectInfoToDockerBuildConfigDecorator(getProject()))
      .accept(new ApplyDockerBuildHook())));
  }


  default void add(Map map) {
    on(new ConfigurationSupplier<DockerBuildConfig>(DockerBuildConfigAdapter
      .newBuilder(propertiesMap(map, EnableDockerBuild.class))
      .accept(new ApplyProjectInfoToDockerBuildConfigDecorator(getProject()))
      .accept(new ApplyDockerBuildHook())));
  }

  default void on(ConfigurationSupplier<DockerBuildConfig> config) {
      session.configurators().add(config);
      DockerBuildConfig buildConfig = config.get();
      if  (Strings.isNotNullOrEmpty(buildConfig.getRegistry()))  {
        session.resources().decorate(new ApplyRegistryToImageDecorator(buildConfig.getRegistry(), buildConfig.getGroup(), buildConfig.getName(), buildConfig.getVersion()));
      } else if (buildConfig.isAutoPushEnabled()) {
        session.resources().decorate(new ApplyRegistryToImageDecorator(DEFAULT_REGISTRY, buildConfig.getGroup(), buildConfig.getName(), buildConfig.getVersion()));
      }
      session.addListener(this);
  }

  default void onClosed() {
    Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
      if (!config.isPresent()) {
        return;
      }
      DockerBuildConfig dockerBuildConfig = config.get();
      if (dockerBuildConfig.isAutoPushEnabled()) {
        DockerBuildHook buildHook = new DockerBuildHook(getProject(), config.get());
        DockerPushHook pushHook = new DockerPushHook(getProject(), config.get());
        OrderedHook hook = OrderedHook.create(buildHook, pushHook);
        hook.register();
      } else if (dockerBuildConfig.isAutoBuildEnabled()) {
        DockerBuildHook hook = new DockerBuildHook(getProject(), config.get());
        hook.register();
      }
  }
}
