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

package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.ServiceSpecFluent;
import io.dekorate.doc.Description;

@Description("Make the service headless.")
public class ApplyHeadlessDecorator extends NamedResourceDecorator<ServiceSpecFluent> {

  public ApplyHeadlessDecorator(String resourceName) {
    super(resourceName);
  }

  @Override
  public void andThenVisit(ServiceSpecFluent spec, ObjectMeta resourceMeta) {
    spec.withClusterIP("None");
  }
}
