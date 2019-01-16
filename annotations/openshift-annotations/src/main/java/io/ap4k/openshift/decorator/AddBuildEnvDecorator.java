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
package io.ap4k.openshift.decorator;

import io.ap4k.deps.openshift.api.model.SourceBuildStrategyFluent;
import io.ap4k.doc.Description;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.kubernetes.decorator.Decorator;

@Description("Add environment variable to to build.")
public class AddBuildEnvDecorator extends Decorator<SourceBuildStrategyFluent<?>> {

  private final Env env;

  public AddBuildEnvDecorator(Env env) {
    this.env = env;
  }

  @Override
  public void visit(SourceBuildStrategyFluent<?> sourceBuildStrategy) {
   sourceBuildStrategy.addNewEnv()
    .withName(env.getName())
    .withValue(env.getValue())
   .endEnv();
  }
}
