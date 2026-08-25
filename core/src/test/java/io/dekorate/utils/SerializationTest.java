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

package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.KubernetesList;
import tools.jackson.core.JacksonException;

class SerializationTest {

  @Test
  public void shouldProduceMeaningFullErrorForSingleResource() throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("single-resource-with-errors.yml")) {
      KubernetesList list = Serialization.unmarshalAsList(is);
      fail();
    } catch (JacksonException e) {
      assertEquals(6, e.getLocation().getLineNr());
    }
  }

  @Test
  public void shouldProduceMeaningFullErrorForMultipleResource() throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("multiple-resources-with-errors.yml")) {
      KubernetesList list = Serialization.unmarshalAsList(is);
      fail();
    } catch (JacksonException e) {
      assertEquals(19, e.getLocation().getLineNr());
    }
  }

  @Test
  public void shouldHandleByteArrayInputStreamBufferOverflow() throws Exception {
    // The file contains enough initial whitespace to cause two buffer overflows
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("single-resource-bis-overflow.yml")) {
      KubernetesList list = Serialization.unmarshalAsList(is);
      ConfigMap configMap = (ConfigMap) list.getItems().get(0);
      assertEquals("dummyvalue", configMap.getData().get("dummykey"));
    }
  }

  @Test
  public void shouldProduceMeaningFullErrorWithByteArrayInputStreamBufferOverflow() throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("single-resource-with-bis-overflow-and-errors.yml")) {
      KubernetesList list = Serialization.unmarshalAsList(is);
      fail();
    } catch (JacksonException e) {
      assertEquals(18614, e.getLocation().getLineNr());
    }
  }

}
