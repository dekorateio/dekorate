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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.dekorate.utils.Strings;

public class AddBasicAuthSecretDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private static final String KUBERNETES_IO_BASIC_AUTH = "kubernetes.io/basic-auth";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";

  private final String name;
  private final String username;
  private final String password;
  private final Map<String, String> annotations;

  public AddBasicAuthSecretDecorator(String username, String password) {
    this(null, username, password, Collections.emptyMap());
  }
  public AddBasicAuthSecretDecorator(String username, String password, Map<String, String> annotations) {
    this(null, username, password, annotations);
  }

  public AddBasicAuthSecretDecorator(String name, String username, String password) {
    this(name, username, password, Collections.emptyMap());
  }

  public AddBasicAuthSecretDecorator(String name, String username, String password, Map<String, String> annotations) {
    this.name = name;
    this.username = username;
    this.password = password;
    this.annotations = annotations;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);
    String name = Strings.isNullOrEmpty(this.name) ? meta.getName() : this.name;

    Map<String, String> data = new HashMap<String, String>() {
      {
        put(USERNAME, username);
        put(PASSWORD, password);
      }
    };

    list.addToItems(new SecretBuilder()
                    .withNewMetadata()
                      .withName(name)
                    .withAnnotations(annotations)
                    .endMetadata()
        .withType(KUBERNETES_IO_BASIC_AUTH).addToStringData(data).build());
  }

}
