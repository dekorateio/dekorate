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


package io.ap4k.examples.openshift;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.Service;
import io.ap4k.deps.openshift.api.model.Route;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

public class BuildRouteIT {

  @Test
  public void shouldContainRoute() {
    KubernetesList list = Serialization.unmarshal(BuildRouteIT.class.getClassLoader().getResourceAsStream("META-INF/ap4k/openshift.yml"));
    assertNotNull(list);

    final Route route = findFirst(list, Route.class).orElseThrow(IllegalStateException::new);
    final Service service = findFirst(list, Service.class).orElseThrow(IllegalStateException::new);

    assertEquals(service.getMetadata().getName(), Main.APP_NAME);

    assertTrue(route.getSpec().getHost().isEmpty());
    assertEquals(Main.PORT_NB, route.getSpec().getPort().getTargetPort().getIntVal().intValue());
    assertEquals("Service", route.getSpec().getTo().getKind());
    assertEquals(Main.APP_NAME, route.getSpec().getTo().getName());
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(t::isInstance)
      .findFirst();
  }
}
