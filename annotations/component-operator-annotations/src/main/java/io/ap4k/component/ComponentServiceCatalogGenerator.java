
package io.ap4k.component;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.component.visitor.AddServiceInstanceToComponent;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.config.Configuration;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;

public class ComponentServiceCatalogGenerator implements Generator<ServiceCatalogConfig> {

  private static final String COMPONENT = "component";
  
  private final Resources resources;

  public ComponentServiceCatalogGenerator () {
    this(new Resources());
  }
  public ComponentServiceCatalogGenerator (Resources resources) {
    this.resources = resources;
  }
  
  public void generate(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.accept(COMPONENT, new AddServiceInstanceToComponent(instance));
    }
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }
}
