package io.ap4k.component.visitor;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

public class AddServiceInstanceToComponent extends TypedVisitor<ComponentSpecBuilder> {

  private final ServiceCatalogInstance instance;

  public AddServiceInstanceToComponent(ServiceCatalogInstance instance) {
    this.instance = instance;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.addNewService()
      .withName(instance.getName())
      .withServiceClass(instance.getServiceClass())
      .withServicePlan(instance.getServicePlan())
      .withSecretName(instance.getBindingSecret())
      .endService();
  }
}
