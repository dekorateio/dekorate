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

package io.dekorate.tekton.decorator;

import io.dekorate.kubernetes.decorator.AddDockerConfigJsonSecretDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.SecretFluent;

/**
 * In Tekton, there are some steps that additionally need the content of the docker config in `config.json` key.
 * For Kubernetes, it's still required to be present in the standard key `.dockerconfigjson`.
 *
 * For example, the kaniko image explicitly requires the config.json file. See more in
 * https://artifacthub.io/packages/tekton-task/tekton-tasks/kaniko?modal=manifest.
 */
public class AddConfigJsonToDockerConfigJsonSecretDecorator extends NamedResourceDecorator<SecretFluent<?>> {

  private static final String CONFIG_JSON = "config.json";

  public AddConfigJsonToDockerConfigJsonSecretDecorator(String name) {
    super(name);
  }

  @Override
  public void andThenVisit(SecretFluent<?> secret, ObjectMeta resourceMeta) {
    String content = secret.getData().get(AddDockerConfigJsonSecretDecorator.DOT_DOCKER_CONFIG_JSON);
    if (Strings.isNotNullOrEmpty(content)) {
      secret.addToData(CONFIG_JSON, content);
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddDockerConfigJsonSecretDecorator.class };
  }
}
