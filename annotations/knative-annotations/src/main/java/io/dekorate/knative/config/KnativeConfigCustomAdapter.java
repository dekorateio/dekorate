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
package io.dekorate.knative.config;

import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;

public class KnativeConfigCustomAdapter {

  public static KnativeConfigBuilder newBuilder(Project project, KnativeApplication knativeApplication) {
    if (knativeApplication != null) {
      return KnativeConfigAdapter.newBuilder(knativeApplication)
        .accept(new ApplyProjectInfo(project));
    } else  {
      return new KnativeConfigBuilder()
        .accept(new ApplyProjectInfo(project));
    }
  }
}
