/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.halkyon.handler;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.servicecatalog.config.EditableServiceCatalogConfig;
import io.dekorate.servicecatalog.config.ServiceCatalogConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentServiceCatalogHandlerTest {
  
  @Test
  public void shouldAcceptServiceCatalogConfig() {
    ComponentServiceCatalogHandler generator = new ComponentServiceCatalogHandler();
    assertTrue(generator.canHandle(ServiceCatalogConfig.class));
  }
  
  @Test
  public void shouldAcceptEditableServiceCatalogConfig() {
    ComponentServiceCatalogHandler generator = new ComponentServiceCatalogHandler();
    assertTrue(generator.canHandle(EditableServiceCatalogConfig.class));
  }
  
  @Test
  public void shouldNotAcceptBaseConfig() {
    ComponentServiceCatalogHandler generator = new ComponentServiceCatalogHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
