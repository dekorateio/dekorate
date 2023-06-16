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

package io.dekorate.kubernetes.configurator;

import java.util.Arrays;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.utils.Strings;

public class ApplyImagePullSecretConfiguration extends Configurator<BaseConfigFluent> {

  private static final String EMPTY = "";
  private static final String COMA = ",";
  private static final String IMAGE_PULL_SECRETS = "dekorate.image-pull-secrets";

  @Override
  public void visit(BaseConfigFluent config) {
    Arrays.stream(System.getProperty(IMAGE_PULL_SECRETS, EMPTY).split(COMA))
        .map(String::trim)
        .filter(Strings::isNotNullOrEmpty)
        .forEach(s -> config.addToImagePullSecrets(s));
  }
}
