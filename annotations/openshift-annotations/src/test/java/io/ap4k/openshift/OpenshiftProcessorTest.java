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
import io.ap4k.config.KubernetesConfig;
import org.junit.jupiter.api.Test;

class OpenshiftProcessorTest {

  @Test
  public void shouldAccpetOpenshiftConfig() {
    OpenshiftProcessor generator = new OpenshiftProcessor();
    assertTrue(generator.accepts(OpenshiftConfig.class));
  }

  @Test
  public void shouldAccpetEditableOpenshiftConfig() {
    OpenshiftProcessor generator = new OpenshiftProcessor();
    assertTrue(generator.accepts(EditableOpenshiftConfig.class));
  }

  @Test
  public void shouldNotAccpetKubernetesConfig() {
    OpenshiftProcessor generator = new OpenshiftProcessor();
    assertFalse(generator.accepts(KubernetesConfig.class));
  }
}
