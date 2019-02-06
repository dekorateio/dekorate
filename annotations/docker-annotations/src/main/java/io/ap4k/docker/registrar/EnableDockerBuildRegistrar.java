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
import io.ap4k.docker.hook.DockerBuildHook;

import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Optional;

public interface EnableDockerBuildRegistrar extends Generator, SessionListener, WithProject {

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
      session.addListener(this);
  }

  default void onClosed() {
    Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
      if (config.isPresent() && config.get().isAutoBuildEnabled()) {
        DockerBuildHook hook = new DockerBuildHook(getProject(), config.get());
        hook.register();
      }
  }
}
