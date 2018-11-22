package io.ap4k.docker.hook;

import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.hook.ProjectHook;
import io.ap4k.project.Project;
import io.ap4k.utils.Strings;

import java.io.File;

public class DockerBuildHook extends ProjectHook {

  private final DockerBuildConfig config;
  private final File dockerFile;
  private final String image;

  public DockerBuildHook(Project project, DockerBuildConfig config) {
    super(project);
    this.config = config;
    this.dockerFile = project.getRoot().resolve(config.getDockerFile()).toFile();
    this.image = Strings.isNotNullOrEmpty(config.getGroup())
      ? config.getGroup() + "/" + config.getName() + ":" + config.getVersion()
      : config.getName()  + ":" +  config.getVersion();
  }

  @Override
  public void init() {

  }

  @Override
  public void warmup() {

  }

  @Override
  public void run() {
    exec("docker", "build", "-t" + image, project.getRoot().toAbsolutePath().toString());
  }
}
