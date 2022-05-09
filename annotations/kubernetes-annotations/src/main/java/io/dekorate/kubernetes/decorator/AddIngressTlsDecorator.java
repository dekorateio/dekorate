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
package io.dekorate.kubernetes.decorator;

import io.dekorate.kubernetes.config.Ingress;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLSBuilder;

public class AddIngressTlsDecorator extends NamedResourceDecorator<IngressSpecBuilder> {

  private final Ingress ingress;

  public AddIngressTlsDecorator(String name, Ingress ingress) {
    super(name);
    this.ingress = ingress;
  }

  @Override
  public void andThenVisit(IngressSpecBuilder spec, ObjectMeta meta) {
    IngressTLSBuilder builder = new IngressTLSBuilder().withSecretName(ingress.getTlsSecretName());
    if (ingress.getTlsHosts() != null && ingress.getTlsHosts().length > 0) {
      builder.withHosts(ingress.getTlsHosts());
    }

    spec.addToTls(builder.build());
  }
}
