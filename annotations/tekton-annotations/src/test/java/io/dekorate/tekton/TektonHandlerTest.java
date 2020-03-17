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

package io.dekorate.tekton;

import io.dekorate.tekton.config.TektonConfig;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.dekorate.tekton.config.EditableTektonConfig;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.tekton.handler.TektonHandler;
import org.junit.jupiter.api.Test;

class TektonHandlerTest {

  @Test
  public void shouldAcceptTektonConfig() {
    TektonHandler generator = new TektonHandler();
    assertTrue(generator.canHandle(TektonConfig.class));
  }

  @Test
  public void shouldAcceptEditableTektonConfig() {
    TektonHandler generator = new TektonHandler();
    assertTrue(generator.canHandle(EditableTektonConfig.class));
  }

  @Test
  public void shouldNotAcceptKubernetesConfig() {
    TektonHandler generator = new TektonHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
