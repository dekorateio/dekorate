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
package io.dekorate.component.handler;

import io.dekorate.component.config.EditableLinkConfig;
import io.dekorate.component.config.LinkConfig;
import io.dekorate.kubernetes.config.BaseConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkHandlerTest {

  @Test
  public void shouldAcceptLinkConfig() {
    LinkHandler generator = new LinkHandler();
    assertTrue(generator.canHandle(LinkConfig.class));
  }

  @Test
  public void shouldAcceptEditableLinkConfig() {
    LinkHandler generator = new LinkHandler();
    assertTrue(generator.canHandle(EditableLinkConfig.class));
  }

  @Test
  public void shouldNotAcceptBaseConfig() {
    LinkHandler generator = new LinkHandler();
    assertFalse(generator.canHandle(BaseConfig.class));
  }
}
