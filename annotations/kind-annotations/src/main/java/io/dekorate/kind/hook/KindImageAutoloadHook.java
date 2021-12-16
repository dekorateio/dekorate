package io.dekorate.kind.hook;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.hook.ProjectHook;
import io.dekorate.project.Project;

public class KindImageAutoloadHook extends ProjectHook {

  private final String image;
  private Logger LOGGER = LoggerFactory.getLogger();

  public KindImageAutoloadHook(Project project, String image) {
    super(project);
    this.image = image;
  }

  @Override
  public void init() {
  }

  @Override
  public void warmup() {
  }

  @Override
  public void run() {
    LOGGER.info("Performing docker image loading with KiND.");
    exec("kind", "load docker-image", image);
  }
}
