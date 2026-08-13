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

package io.dekorate.jaxrs.adapter;

import java.util.Map;

import io.dekorate.jaxrs.config.JaxrsConfig;
import io.dekorate.jaxrs.config.JaxrsConfigBuilder;

public class JaxrsConfigAdapter {

  private static final String PATH_KEY = "path";

  public static JaxrsConfigBuilder newBuilder(JaxrsConfig jaxrsConfig) {
    return new JaxrsConfigBuilder().withPath(jaxrsConfig.getPath());
  }

  public static JaxrsConfigBuilder newBuilder(Map map) {
    Object path = map.get(PATH_KEY);
    return new JaxrsConfigBuilder().withPath(path instanceof String ? (String) path : null);
  }

  public static JaxrsConfig adapt(Map map) {
    return newBuilder(map).build();
  }
}
