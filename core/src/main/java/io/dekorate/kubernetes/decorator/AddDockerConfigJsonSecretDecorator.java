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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;

public class AddDockerConfigJsonSecretDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  public static final String DOT_DOCKER_CONFIG_JSON = ".dockerconfigjson";
  private static final String TYPE = "kubernetes.io/dockerconfigjson";
  private static final String DOCKER_CONFIG_STR = "{\n \t\"auths\": {\n\t\t\"%s\": {\n \t\t\t\"auth\":\"%s\"\n\t\t}\n\t}\n}";
  private static final Charset CHARSET = Charset.defaultCharset();

  private final String name;
  private final String content;
  private final Map<String, String> annotations;

  public AddDockerConfigJsonSecretDecorator(String name, String registry, String username, String password) {
    this(name, registry, username, password, Collections.emptyMap());
  }

  public AddDockerConfigJsonSecretDecorator(String name, String registry, String username, String password,
      Map<String, String> annotations) {
    this.name = name;
    this.annotations = annotations;
    this.content = new String(Base64.getEncoder()
        .encode(String
            .format(DOCKER_CONFIG_STR, registry,
                new String(Base64.getEncoder().encode((username + ":" + password).getBytes(CHARSET)), CHARSET))
            .getBytes(CHARSET)),
        CHARSET);
  }

  public AddDockerConfigJsonSecretDecorator(String name, Path existingConfigJson) {
    this(name, existingConfigJson, Collections.emptyMap());
  }

  public AddDockerConfigJsonSecretDecorator(String name, Path existingConfigJson, Map<String, String> annotations) {
    this.name = name;
    this.annotations = annotations;
    this.content = new String(Base64.getEncoder().encode(Strings.read(existingConfigJson).getBytes(CHARSET)), CHARSET);
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    String name = Strings.isNotNullOrEmpty(this.name) ? this.name : getMandatoryDeploymentMetadata(list, ANY).getName();
    Secret secret = new SecretBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(this.annotations)
        .endMetadata()
        .withType(TYPE)
        .addToData(DOT_DOCKER_CONFIG_JSON, this.content)
        .build();

    list.addToItems(secret);
  }

}
