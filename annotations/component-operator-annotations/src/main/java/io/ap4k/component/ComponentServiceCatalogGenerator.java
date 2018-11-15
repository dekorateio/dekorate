
package io.ap4k.component;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.component.visitor.AddServiceInstanceToComponent;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;

public class ComponentServiceCatalogGenerator implements Generator<ServiceCatalogConfig> {

  private static final String COMPONENT = "component";
  
  private final Resources resources;

  public ComponentServiceCatalogGenerator (Resources resources) {
    this.resources = resources;
  }
  
  public void generate(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.accept(COMPONENT, new AddServiceInstanceToComponent(instance));
    }
  }

  public Class<? extends ServiceCatalogConfig> getType() {
    return ServiceCatalogConfig.class;
  }

}
