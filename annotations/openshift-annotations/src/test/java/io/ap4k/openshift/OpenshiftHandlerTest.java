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


package io.ap4k.openshift;

import io.ap4k.openshift.config.OpenshiftConfig;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.ap4k.openshift.config.EditableOpenshiftConfig;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.openshift.handler.OpenshiftHandler;
import org.junit.jupiter.api.Test;

class OpenshiftHandlerTest {

  @Test
  public void shouldAccpetOpenshiftConfig() {
    OpenshiftHandler generator = new OpenshiftHandler();
    assertTrue(generator.canHandle(OpenshiftConfig.class));
  }

  @Test
  public void shouldAccpetEditableOpenshiftConfig() {
    OpenshiftHandler generator = new OpenshiftHandler();
    assertTrue(generator.canHandle(EditableOpenshiftConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    OpenshiftHandler generator = new OpenshiftHandler();
    assertFalse(generator.canHandle(KubernetesConfig.class));
  }
}
