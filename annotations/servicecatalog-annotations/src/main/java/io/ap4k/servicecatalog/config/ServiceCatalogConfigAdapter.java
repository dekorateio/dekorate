/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
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
