package io.ap4k.component.decorator;

import io.ap4k.component.config.Link;
import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.component.model.Env;
import io.ap4k.doc.Description;
import io.ap4k.kubernetes.decorator.Decorator;

import java.util.Arrays;
import java.util.stream.Collectors;

@Description("Add link variable to component.")
public class AddLinkToComponent extends Decorator<ComponentSpecBuilder> {

  private final Link link;

  public AddLinkToComponent (Link link) {
    this.link = link;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.addNewLink()
      .withKind(kindOf(link.getKind()))
      .withName(link.getName())
      .withRef(link.getRef())
      .withTargetComponentName(link.getTargetcomponentname())
      .addAllToEnvs(Arrays.asList(link.getEnvVars()).stream().map(e -> new Env(e.getName(), e.getValue())).collect(Collectors.toList()))
      .endLink();
  }

  private static io.ap4k.component.model.Link.Kind kindOf(String s) {
    for (io.ap4k.component.model.Link.Kind e : io.ap4k.component.model.Link.Kind.values()) {
      if (s.equalsIgnoreCase(e.name())) {
        return e;
      }
    }
    return null;
  }
}
