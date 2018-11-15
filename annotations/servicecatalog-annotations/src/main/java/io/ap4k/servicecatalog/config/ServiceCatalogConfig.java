package io.ap4k.servicecatalog.config;

import io.ap4k.config.ConfigKey;
import io.ap4k.config.Configuration;
import io.ap4k.project.Project;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

import java.util.List;
import java.util.Map;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = @BuildableReference(Configuration.class))
public class ServiceCatalogConfig extends Configuration {

  private final List<ServiceCatalogInstance> instances;

  public ServiceCatalogConfig(Project project, Map<ConfigKey, Object> attributes, List<ServiceCatalogInstance> instances) {
    super(project, attributes);
    this.instances = instances;
  }

  public List<ServiceCatalogInstance> getInstances() {
    return instances;
  }
}
