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
package io.dekorate.kubernetes.configurator;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.BaseConfigFluent;

@Description("sets the hostname based on system property: `dekorate.host`.")
public class ApplyHostBuild extends Configurator<BaseConfigFluent> {

  public static final String Dekorate_HOST = "dekorate.host";

  @Override
  public void visit(BaseConfigFluent config) {
    config.withHost(System.getProperty(Dekorate_HOST, String.valueOf(config.getHost())));
  }
}
