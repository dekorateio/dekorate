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
package io.dekorate.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DekorateProfileAdditionalResourcesProvider implements AdditionalResourcesProvider {

  private final String DEKORATE_PROFILE = "dekorate.properties.profile";

  @Override
  public int order() {
    return 1;
  }

  /**
   * @return resource names when the property Dekorate profile is set.
   */
  @Override
  public List<String> getResourceNames() {
    String profile = System.getProperty(DEKORATE_PROFILE);
    if (profile == null || profile.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> resourceNames = new ArrayList<>();
    resourceNames.add("application-" + profile + ".properties");
    resourceNames.add("application-" + profile + ".yaml");
    resourceNames.add("application-" + profile + ".yml");
    return resourceNames;
  }
}
