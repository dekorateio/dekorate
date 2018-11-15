package io.ap4k.servicecatalog.config;

import io.ap4k.servicecatalog.adapter.ServiceCatalogInstanceAdapter;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ServiceCatalogConfigAdapter {

  public static ServiceCatalogConfigBuilder newBuilder(ServiceCatalog serviceCatalog) {
    return new ServiceCatalogConfigBuilder()
      .withInstances(Arrays.asList(serviceCatalog.instances())
        .stream()
        .map(ServiceCatalogInstanceAdapter::adapt)
        .collect(Collectors.toList()));

  }

  public static ServiceCatalogConfigBuilder newServiceCatalogBuilder()  {
    return new ServiceCatalogConfigBuilder();
  }

}
