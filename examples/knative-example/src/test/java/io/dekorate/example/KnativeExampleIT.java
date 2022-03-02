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

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KnativeIntegrationTest;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Route;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.api.model.KubernetesList;

@KnativeIntegrationTest
class KnativeExampleIT {

  @Inject
  private KnativeClient client;

  @Inject
  private KubernetesList list;

  @Inject
  private Service service;

  @Inject
  private Route route;

  @Inject
  private URL routeUrl;

  @Test
  public void shouldInjectResources() {
    Assertions.assertNotNull(client);
    Assertions.assertNotNull(list);
    Assertions.assertNotNull(service);
    Assertions.assertNotNull(route);
    Assertions.assertNotNull(routeUrl);
  }
}
