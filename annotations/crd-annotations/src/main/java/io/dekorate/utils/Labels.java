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

package io.dekorate.utils;

import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Label;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Labels {

  public static final String NAME = "app.kubernetes.io/name";
  public static final String VERSION = "app.kubernetes.io/version";
  public static final String PART_OF = "app.kubernetes.io/part-of";
  public static final String UNKNOWN = "<<unknown>>";

  /**
   * Creates a {@link Map} with the labels for the {@link BaseConfig}.
   * 
   * @param config The config.
   * @return A map containing the lables.
   */
  public static Set<Label> createLabels(BaseConfig config) {
    Set<Label> result = new HashSet<Label>() {
      {
        add(new Label(NAME, config.getName(), null));
        add(new Label(VERSION, config.getVersion(), null));
        if (Strings.isNotNullOrEmpty(config.getPartOf())) {
          add(new Label(PART_OF, config.getPartOf(), null));
        }
      }
    };

    for (Label label : config.getLabels()) {
      result.add(label);
    }
    return result;
  }


  public static Map<String, String> createLabelsAsMap(BaseConfig config, String kind) {
    return createLabels(config).stream().filter(l -> l.getKinds().length == 0 || Arrays.asList(l.getKinds()).contains(kind)).collect(Collectors.toMap(l -> l.getKey(), l -> l.getValue()));
  }
}
