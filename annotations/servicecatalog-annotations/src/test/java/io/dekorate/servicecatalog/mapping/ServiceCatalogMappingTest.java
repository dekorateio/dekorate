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
 */
package io.dekorate.servicecatalog.mapping;

import io.dekorate.deps.kubernetes.api.KubernetesResourceMappingProvider;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.kubernetes.client.DefaultKubernetesClient;
import io.dekorate.deps.kubernetes.client.KubernetesClient;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceCatalogMappingTest {

  @Test
  void shouldLoadProvider() {
    ServiceLoader loader = ServiceLoader.load(KubernetesResourceMappingProvider.class);
    loader.forEach(l -> System.out.println("Found loader:" + l ));
  }

  @Test
  public void shouldUnmarshall() {
    KubernetesList list = Serialization.unmarshalAsList(ServiceCatalogMappingTest.class.getClassLoader().getResourceAsStream("svcat.yml"));
    assertNotNull(list);
  }

  @Test
  public void shouldUnmarshallUsingTheClient() {
    KubernetesClient client = new DefaultKubernetesClient();
    List<HasMetadata> list = client.load(ServiceCatalogMappingTest.class.getClassLoader().getResourceAsStream("svcat.yml")).get();
    assertNotNull(list);
  }
}
