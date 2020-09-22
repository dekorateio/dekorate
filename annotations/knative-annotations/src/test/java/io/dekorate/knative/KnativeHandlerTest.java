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

package io.dekorate.knative;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import io.dekorate.knative.config.EditableKnativeConfig;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.handler.KnativeHandler;
import io.dekorate.kubernetes.config.BaseConfig;

class KnativeHandlerTest {

  @Test
  public void shouldAcceptKnativeConfig() {
    KnativeHandler generator = new KnativeHandler();
    assertTrue(generator.canHandle(KnativeConfig.class));
  }

  @Test
  public void shouldAcceptEditableKnativeConfig() {
    KnativeHandler generator = new KnativeHandler();
    assertTrue(generator.canHandle(EditableKnativeConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfig() {
    KnativeHandler generator = new KnativeHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
