package io.ap4k.servicecatalog;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.servicecatalog.config.Parameter;
import io.ap4k.servicecatalog.config.ServiceCatalogConfig;
import io.ap4k.servicecatalog.config.ServiceCatalogInstance;
import io.ap4k.utils.Strings;
import me.snowdrop.servicecatalog.api.model.ServiceBindingBuilder;
import me.snowdrop.servicecatalog.api.model.ServiceInstanceBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceCatalogGenerator implements Generator<ServiceCatalogConfig> {

  private final Resources resources;

  public ServiceCatalogGenerator(Resources resources) {
    this.resources = resources;
  }

  @Override
  public void generate(ServiceCatalogConfig config) {
     for (ServiceCatalogInstance instance : config.getInstances()) {
      resources.add(new ServiceInstanceBuilder()
        .withNewMetadata()
        .withName(instance.getName())
        .endMetadata()
        .withNewSpec()
        .withClusterServiceClassExternalName(instance.getServiceClass())
        .withClusterServicePlanExternalName(instance.getServicePlan())
        .withParameters(toMap(instance.getParameters()))
        .endSpec()
        .build());

      if (!Strings.isNullOrEmpty(instance.getBindingSecret())) {
        resources.add(new ServiceBindingBuilder()
          .withNewMetadata()
          .withName(instance.getName())
          .endMetadata()
          .withNewSpec()
          .withNewInstanceRef(instance.getName())
          .withSecretName(instance.getBindingSecret())
          .endSpec()
          .build());
      }
    }
  }

  @Override
  public Class<? extends ServiceCatalogConfig> getType() {
    return ServiceCatalogConfig.class;
  }



  /**
   * Converts an array of {@link Parameter} to a {@link Map}.
   * @param parameters    The parameters.
   * @return              A map.
   */
  protected static Map<String, Object> toMap(Parameter... parameters) {
    return Arrays.asList(parameters).stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }
}
