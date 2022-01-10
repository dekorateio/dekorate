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

package io.dekorate.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.dekorate.helm.model.Chart;
import io.dekorate.utils.Serialization;

class HelmExampleTest {

  @Test
  public void shouldHelmManifestsBeGenerated() throws IOException {
    Chart chart = Serialization.yamlMapper().readValue(Main.class.getClassLoader().getResourceAsStream("META-INF/dekorate/Chart.yaml"), Chart.class);
    assertNotNull(chart, "Chart is null!");
    // Should be the same as in `dekorate.helm.chart` from properties.
    assertEquals("myChart", chart.getName());
    // Values.yaml manifest
    assertNotNull(Main.class.getClassLoader().getResourceAsStream("META-INF/dekorate/values.yaml"));
    // templates
    assertNotNull(Main.class.getClassLoader().getResourceAsStream("META-INF/dekorate/templates/kubernetes.yml"));
    // zip manifest
    String zipName = String.format("META-INF/dekorate/%s-%s-helm.tar.gz", chart.getName(), chart.getVersion());
    assertNotNull(Main.class.getClassLoader().getResourceAsStream(zipName), "File '" + zipName + "' not found!");
  }
}
