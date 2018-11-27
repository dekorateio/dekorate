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

import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.config.S2iConfigBuilder;

public class Constants {

  public static String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";


  public static S2iConfig DEFAULT_SOURCE_TO_IMAGE_CONFIG = new S2iConfigBuilder()
    .withBuilderImage(DEFAULT_S2I_BUILDER_IMAGE)
    .build();

  public static ConfigKey<S2iConfig> SOURCE_TO_IMAGE_CONFIG = new ConfigKey<S2iConfig>("SOURCE_TO_IMAGE_CONFIG", S2iConfig.class, DEFAULT_SOURCE_TO_IMAGE_CONFIG);
}
