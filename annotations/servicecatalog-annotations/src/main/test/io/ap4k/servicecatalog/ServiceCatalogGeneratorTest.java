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


package io.ap4k.servicecatalog;

import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.servicecatalog.config.EditableServiceCatalogConfig;


public class ServiceCatalogGeneratorTest {

  @Test
  public void shouldAccpetServiceCatalogConfig() {
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertTrue(generator.accepts(ServiceCatalogConfig.class));
  }

  @Test
  public void shouldAccpetEditableServiceCatalogConfig() {
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertTrue(generator.accepts(EditableServiceCatalogConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    ServiceCatalogGenerator generator = new ServiceCatalogGenerator();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
