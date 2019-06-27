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
package io.ap4k.kubernetes.adapter;

import io.ap4k.kubernetes.annotation.Protocol;
import io.ap4k.kubernetes.config.KubernetesConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KubernetesConfigAdapterTest {

  @Test
  public void testKubernetesAppWithPorts() {
    final Map<String, Object> ports[] = new Map[1];
    ports[0] = new HashMap<String, Object>() {{
      put("name", "http");
      put("containerPort", 8080);
      put("protocol", Protocol.TCP);

    }};

    Map<String, Object> map = new HashMap<String, Object>() {{
        put("name", "generator-test");
        put("group", "generator-test-group");
        put("version", "latest");
        put("replicas", 2);
        put("ports", ports);
    }};

    KubernetesConfig config = KubernetesConfigAdapter.adapt(map);
    assertNotNull(config);
    assertEquals(1, config.getPorts().length);
    assertEquals(8080, config.getPorts()[0].getContainerPort());
  }

}
