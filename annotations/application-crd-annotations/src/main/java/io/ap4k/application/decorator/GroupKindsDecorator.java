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

package io.ap4k.application.decorator;

import io.ap4k.deps.applicationcrd.api.model.ApplicationSpecBuilder;
import io.ap4k.deps.applicationcrd.api.model.GroupKind;
import io.ap4k.deps.applicationcrd.api.model.GroupKindBuilder;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.doc.Description;
import io.ap4k.kubernetes.decorator.Decorator;

import java.util.ArrayList;
import java.util.List;

@Description("A decorator that adds GroupKinds to application resources.")
public class GroupKindsDecorator extends Decorator<KubernetesListBuilder> {

  public void visit(KubernetesListBuilder kubernetesList) {
    List<GroupKind> groupKinds = new ArrayList<>();

    for (HasMetadata h : kubernetesList.getItems()) {
      groupKinds.add(new GroupKindBuilder()
        .withKind(h.getKind())
        .withGroup(apiVersionGroup(h.getApiVersion()))
        .build());
    }

    kubernetesList.accept(new Decorator<ApplicationSpecBuilder>() {
      @Override
      public void visit(ApplicationSpecBuilder applicationSpec) {
       applicationSpec.withComponentKinds(groupKinds);
      }
    });
  }

  private static String apiVersionGroup(String apiVersion) {
    if (apiVersion == null)  {
      return null;
    }

    if (apiVersion.contains("/")) {
      return apiVersion.substring(0, apiVersion.indexOf("/"));
    }
    else return "core";
  }
}
