package io.dekorate.component.decorator;

import io.dekorate.component.model.ComponentSpecBuilder;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

@Description("Add the build configuration to the component.")
public class AddBuildConfigToComponentDecorator extends Decorator<ComponentSpecBuilder> {

  private final Path modulePath;
  private final String uri;
  private final String ref;
  private final String type;

  public AddBuildConfigToComponentDecorator(Path modulePath, String uri, String ref, String type) {
    this.modulePath = modulePath;
    this.uri = uri;
    this.ref = ref;
    this.type = type;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component
      .withNewBuildConfig()
      .withUri(uri)
      .withRef(ref)
      .withType(type)
      .withContextPath(toContextPath(modulePath))
      .withModuleDirName(toModuleDirName(modulePath))
      .endBuildConfig();
  }

  /**
   * Get the context path of the current module.
   * This is the relative path from project root, excluding the module directory.
   * @param modulePath The relative path from the project root.
   * @return the name of module directory.
   */
  private static String toContextPath(Path modulePath) {
    StringBuilder sb = new StringBuilder();
    Iterator<Path> iterator = modulePath.iterator();
    while (iterator.hasNext()) {
      String current = iterator.next().toString();
      if (iterator.hasNext()) {
        sb.append(current);
        sb.append(File.separator);
      }
    }
    return sb.toString();
  }

  /**
   * Get the directory name of the current module.
   * @param modulePath The relative path from the project root.
   * @return the name of module directory.
   */
  private static String toModuleDirName(Path modulePath) {
    Iterator<Path> iterator = modulePath.iterator();
    while (iterator.hasNext()) {
      String current = iterator.next().toString();
      if (!iterator.hasNext()) {
        return current;
      }
    }
    return null;
  }


}
