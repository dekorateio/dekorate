package io.dekorate.component.decorator;

import io.dekorate.component.model.ComponentSpecBuilder;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;

import java.nio.file.Path;

@Description("Add the build configuration to the component.")
public class AddBuildConfigToComponentDecorator extends Decorator<ComponentSpecBuilder> {

  private final String uri;
  private final String ref;
  private final String moduleDirName;
  private final String type;

  public AddBuildConfigToComponentDecorator(String uri, String ref, String moduleDirName, String type) {
    this.uri = uri;
    this.ref = ref;
    this.moduleDirName = moduleDirName;
    this.type = type;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component
      .withNewBuildConfig()
      .withUri(uri)
      .withRef(ref)
      .withModuleDirName(moduleDirName)
      .withType(type)
      .endBuildConfig();
  }

}
