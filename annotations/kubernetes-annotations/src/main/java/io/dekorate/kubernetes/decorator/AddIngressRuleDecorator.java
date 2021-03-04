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

import io.dekorate.kubernetes.config.Port;
import io.dekorate.utils.Strings;

import java.util.function.Predicate;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecBuilder;

public class AddIngressRuleDecorator extends NamedResourceDecorator<IngressSpecBuilder> {

  private final String host;
  private final Port port;

  public AddIngressRuleDecorator(String name, String host, Port port) {
    super(name);
    this.host = host;
    this.port = port;
  }

  @Override
  public void andThenVisit(IngressSpecBuilder spec, ObjectMeta meta) {
    Predicate<IngressRuleBuilder> matchingHost = r -> r.getHost() != null && r.getHost().equals(host);

    if (!spec.hasMatchingRule(matchingHost)) {
      spec.addNewRule().withHost(host).withNewHttp().addNewPath().withPathType("Prefix").withPath(port.getPath()).withNewBackend()
        .withNewService()
        .withName(name)
        .withNewPort().withName(port.getName())
        .withNumber(Strings.isNullOrEmpty(port.getName()) ? port.getHostPort() : null).endPort()
        .endService()
        .endBackend()
        .endPath()
        .endHttp()
        .endRule();
    } else {
      spec.accept(new HostVisitor(meta));
    }
  }

  private class HostVisitor extends TypedVisitor<IngressRuleBuilder> {

    private final ObjectMeta meta;

    public HostVisitor(ObjectMeta meta) {
      this.meta = meta;
    }

    @Override
    public void visit(IngressRuleBuilder rule) {
      Predicate<HTTPIngressPathBuilder> mathcingPath = r -> r.getPath().equals(port.getPath());
      if (rule.getHost().equals(host)) {
        if (!rule.editOrNewHttp().hasMatchingPath(mathcingPath)) {
          rule.editHttp().addNewPath().withNewBackend()
            .withNewService()
            .withName(name)
            .withNewPort().withName(port.getName())
            .withNumber(Strings.isNullOrEmpty(port.getName()) ? port.getHostPort() : null).endPort()
            .endService()
            .endBackend()
            .endPath().endHttp();
        } else {
          rule.accept(new PathVisitor(meta));
        }
      }
    }
  }

  private class PathVisitor extends TypedVisitor<HTTPIngressPathBuilder> {

    private final ObjectMeta meta;

    public PathVisitor(ObjectMeta meta) {
      this.meta = meta;
    }

    public void visit(HTTPIngressPathBuilder path) {
      path.withNewBackend()
        .withNewService()
        .withName(name)
        .withNewPort().withName(port.getName())
        .withNumber(Strings.isNullOrEmpty(port.getName()) ? port.getHostPort() : null).endPort()
        .endService()
        .endBackend();
    }
  }
}
