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

import java.util.Arrays;
import java.util.Collections;

import io.dekorate.deps.kubernetes.api.model.Container;
import io.dekorate.deps.kubernetes.api.model.ContainerFluent;
import io.dekorate.deps.kubernetes.api.model.ExecAction;
import io.dekorate.deps.kubernetes.api.model.HTTPGetAction;
import io.dekorate.deps.kubernetes.api.model.IntOrString;
import io.dekorate.deps.kubernetes.api.model.TCPSocketAction;
import io.dekorate.kubernetes.config.Probe;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;

/**
 * Base class for any kind of {@link Decorator} that acts on probes.
 */
public abstract class AbstractAddProbeDecorator extends ApplicationContainerDecorator<ContainerFluent<?>> {

  protected final Probe probe;

  abstract protected void doCreateProbe(ContainerFluent<?> container, Actions actions);

  public AbstractAddProbeDecorator(String containerName, Probe probe) {
    super(null, containerName);
    this.probe = probe;
  }

  public AbstractAddProbeDecorator(String deploymentName, String containerName, Probe probe) {
    super(deploymentName, containerName);
    this.probe = probe;
  }

  @Override
  public void andThenVisit(ContainerFluent<?> container) {
    if (probe == null) {
      return;
    }

    final ExecAction execAction = execAction(probe);
    final TCPSocketAction tcpSocketAction = tcpSocketAction(probe);
    final boolean defaultToHttpGetAction = (execAction == null) && (tcpSocketAction == null);
    final HTTPGetAction httpGetAction = defaultToHttpGetAction ? httpGetAction(probe, container) : null;
    if (defaultToHttpGetAction && (httpGetAction == null)) {
      return;
    }

    doCreateProbe(container, new Actions(execAction, tcpSocketAction, httpGetAction));
  }

  private ExecAction execAction(Probe probe) {
    if (Strings.isNullOrEmpty(probe.getExecAction())) {
      return null;
    }
    return new ExecAction(Arrays.asList(probe.getExecAction().split(" ")));
  }

  private HTTPGetAction httpGetAction(Probe probe, ContainerFluent<?> container) {
    if (!container.hasPorts()) {
      return new HTTPGetAction(null, Collections.emptyList(), probe.getHttpActionPath(), new IntOrString(8080), "HTTP");
    }

    return new HTTPGetAction(null, Collections.emptyList(), probe.getHttpActionPath(), new IntOrString(Ports.getHttpPort(container).get().getContainerPort()), "HTTP");
  }

  private TCPSocketAction tcpSocketAction(Probe probe) {
    if (Strings.isNullOrEmpty(probe.getTcpSocketAction()))  {
      return null;
    }

    String[] parts = probe.getTcpSocketAction().split(":");
    if (parts.length != 2) {
      throw  new RuntimeException("Invalid format for tcp socket action! Expected: <host>:<port>. Found:"+probe.getTcpSocketAction()+".");
    }

    return new TCPSocketAction(parts[0], new IntOrString(parts[1]));
  }

  protected static class Actions {
    protected final ExecAction execAction;
    protected final TCPSocketAction tcpSocketAction;
    protected final HTTPGetAction httpGetAction;

    protected Actions(ExecAction execAction, TCPSocketAction tcpSocketAction, HTTPGetAction httpGetAction) {
      this.execAction = execAction;
      this.tcpSocketAction = tcpSocketAction;
      this.httpGetAction = httpGetAction;
    }
  }
}
