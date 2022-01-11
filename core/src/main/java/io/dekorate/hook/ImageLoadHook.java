package io.dekorate.hook;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import io.dekorate.ImageLoader;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Images;

public class ImageLoadHook extends ProjectHook {

  private final String image;
  private final Optional<ImageLoader> imageLoader;

  public ImageLoadHook(Project project, ImageConfiguration config) {
    super(project);
    this.image = Images.getImage(config.getRegistry(), config.getGroup(), config.getName(), config.getVersion());
    this.imageLoader = StreamSupport
        .stream(ServiceLoader.load(ImageLoader.class, ImageLoader.class.getClassLoader()).spliterator(), false).findFirst();
  }

  @Override
  public void init() {
  }

  @Override
  public void warmup() {
  }

  @Override
  public void run() {
    imageLoader.ifPresent(loader -> loader.load(project, image));
  }
}
