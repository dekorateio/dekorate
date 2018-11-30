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
package io.ap4k.testing.openshift;

import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.utils.Serialization;

public interface WithS2iBuildConfig {

  String S2Ι_CONFIG_PATH = "META-INF/ap4k/.config/s2i.yml";


  default S2iConfig getSourceToImageConfig() {
    return  Serialization.unmarshal(WithS2iBuildConfig.class.getClassLoader().getResourceAsStream(S2Ι_CONFIG_PATH), S2iConfig.class);
  }

}
