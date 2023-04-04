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

package io.dekorate.knative.helm;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class KnativeWithHelmTest {

  @Test
  public void shouldHaveProbesMappedInValuesData() throws IOException {
    KubernetesList list = Serialization.unmarshalAsList(getResource("/knative.yml"));
    assertNotNull(list);

    Map<String, Object> values = Serialization.yamlMapper()
        .readValue(getResource("/helm/knative/example/values.yaml"), Map.class);
    Map<String, Object> app = (Map<String, Object>) values.get("app");
    assertNotNull(app);
    assertPortIsNotNullForProbe(app, "livenessProbe");
    assertPortIsNotNullForProbe(app, "readinessProbe");
  }

  private static void assertPortIsNotNullForProbe(Map<String, Object> app, String probeName) {
    Map<String, Object> probe = (Map<String, Object>) app.get(probeName);
    assertNotNull(probe);
    Map<String, Object> httpGet = (Map<String, Object>) probe.get("httpGet");
    assertNotNull(httpGet.get("port"));
  }

  private static InputStream getResource(String resource) {
    return KnativeWithHelmTest.class.getClassLoader().getResourceAsStream("META-INF/dekorate" + resource);
  }
}
