
package io.ap4k.component;

import io.ap4k.Processor;
import io.ap4k.Resources;
import io.ap4k.component.decorator.AddServiceInstanceToComponent;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.config.Configuration;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;

public class ComponentServiceCatalogProcessor implements Processor<ServiceCatalogConfig> {

  private static final String COMPONENT = "component";
  
  private final Resources resources;

  public ComponentServiceCatalogProcessor() {
    this(new Resources());
  }
  public ComponentServiceCatalogProcessor(Resources resources) {
    this.resources = resources;
  }
  
  public void process(ServiceCatalogConfig config) {
    for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.accept(COMPONENT, new AddServiceInstanceToComponent(instance));
    }
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(ServiceCatalogConfig.class) ||
      type.equals(EditableServiceCatalogConfig.class);
  }
}
