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
package io.dekorate.kubernetes.decorator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ApplyRegistryToImageDecoratorTest {

  @Test
  public void shouldRespectAfter() {
    ApplyRegistryToImageDecorator r = new ApplyRegistryToImageDecorator("docker.io", "test", "image", "latest");
    ApplyImageDecorator a = new ApplyImageDecorator("cnt", "image");

    assertEquals(1, r.compareTo(a));
    assertEquals(-1, a.compareTo(r));
  }

}
