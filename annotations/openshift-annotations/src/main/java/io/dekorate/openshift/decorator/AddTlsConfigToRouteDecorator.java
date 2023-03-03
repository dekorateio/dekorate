/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dekorate.openshift.decorator;

import static io.dekorate.openshift.decorator.AddRouteDecorator.KIND_ROUTE;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.TLSConfig;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.RouteSpecFluent;

@Description("Add the TLS configuration to the Route resource.")
public class AddTlsConfigToRouteDecorator extends NamedResourceDecorator<RouteSpecFluent<?>> {

  private final OpenshiftConfig config;

  public AddTlsConfigToRouteDecorator(OpenshiftConfig config) {
    super(KIND_ROUTE, config.getName());
    this.config = config;
  }

  @Override
  public void andThenVisit(RouteSpecFluent<?> spec, ObjectMeta resourceMeta) {
    if (config.getRoute() == null
        || config.getRoute().getTls() == null
        || !anyFieldsSet(config.getRoute().getTls())) {
      return;
    }

    RouteSpecFluent.TlsNested<?> tlsSpec = spec.editOrNewTls();
    TLSConfig tls = config.getRoute().getTls();
    if (Strings.isNotNullOrEmpty(tls.getCaCertificate())) {
      tlsSpec.withCaCertificate(tls.getCaCertificate());
    }

    if (Strings.isNotNullOrEmpty(tls.getCertificate())) {
      tlsSpec.withCertificate(tls.getCertificate());
    }

    if (Strings.isNotNullOrEmpty(tls.getKey())) {
      tlsSpec.withKey(tls.getKey());
    }

    if (Strings.isNotNullOrEmpty(tls.getDestinationCACertificate())) {
      tlsSpec.withDestinationCACertificate(tls.getDestinationCACertificate());
    }

    if (Strings.isNotNullOrEmpty(tls.getTermination())) {
      tlsSpec.withTermination(tls.getTermination());
    }

    if (Strings.isNotNullOrEmpty(tls.getInsecureEdgeTerminationPolicy())) {
      tlsSpec.withInsecureEdgeTerminationPolicy(tls.getInsecureEdgeTerminationPolicy());
    }

    tlsSpec.endTls();
  }

  private boolean anyFieldsSet(TLSConfig tls) {
    return Strings.isNotNullOrEmpty(tls.getCaCertificate())
        || Strings.isNotNullOrEmpty(tls.getCertificate())
        || Strings.isNotNullOrEmpty(tls.getKey())
        || Strings.isNotNullOrEmpty(tls.getDestinationCACertificate())
        || Strings.isNotNullOrEmpty(tls.getTermination())
        || Strings.isNotNullOrEmpty(tls.getInsecureEdgeTerminationPolicy());
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddRouteDecorator.class };
  }
}
