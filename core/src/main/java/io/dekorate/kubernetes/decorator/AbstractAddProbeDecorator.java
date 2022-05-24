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

import static io.dekorate.ConfigReference.joinProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.kubernetes.config.Probe;
import io.dekorate.utils.Ports;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.GRPCAction;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.TCPSocketAction;

/**
 * Base class for any kind of {@link Decorator} that acts on probes.
 */
public abstract class AbstractAddProbeDecorator extends ApplicationContainerDecorator<ContainerFluent<?>>
    implements WithConfigReferences {

  private static final String JSONPATH_CONTAINERS_EXPRESSION = "*.spec.containers.";
  private static final Object AUTO_DISCOVER = null;

  protected final Probe probe;

  abstract protected void doCreateProbe(ContainerFluent<?> container, Actions actions);

  abstract protected String getProbeName();

  public AbstractAddProbeDecorator(String containerName, Probe probe) {
    this(null, containerName, probe);
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

    ExecAction execAction = execAction(probe);
    TCPSocketAction tcpSocketAction = tcpSocketAction(probe);
    GRPCAction grpcAction = grpcAction(probe);
    HTTPGetAction httpGetAction = null;
    if (!isExecOrTcpOrGrpcActionSet()) {
      httpGetAction = httpGetAction(probe, container);
    }

    doCreateProbe(container, new Actions(execAction, tcpSocketAction, httpGetAction, grpcAction));
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    List<ConfigReference> configReferences = new ArrayList<>();
    configReferences.add(buildConfigReference("failureThreshold", probe.getFailureThreshold()));
    configReferences.add(buildConfigReference("initialDelaySeconds", probe.getInitialDelaySeconds()));
    configReferences.add(buildConfigReference("periodSeconds", probe.getPeriodSeconds()));
    configReferences.add(buildConfigReference("successThreshold", probe.getSuccessThreshold()));
    configReferences.add(buildConfigReference("timeoutSeconds", probe.getTimeoutSeconds()));
    if (isExecOrTcpOrGrpcActionSet()) {
      if (Strings.isNotNullOrEmpty(probe.getExecAction())) {
        configReferences.add(buildConfigReference("exec.command", AUTO_DISCOVER));
      } else if (Strings.isNotNullOrEmpty(probe.getGrpcAction())) {
        configReferences.add(buildConfigReference("grpc.port", AUTO_DISCOVER));
        configReferences.add(buildConfigReference("grpc.service", AUTO_DISCOVER));
      } else if (Strings.isNotNullOrEmpty(probe.getTcpSocketAction())) {
        configReferences.add(buildConfigReference("tcpSocket.host", AUTO_DISCOVER));
        configReferences.add(buildConfigReference("tcpSocket.port", AUTO_DISCOVER));
      }
    } else {
      // default to http action
      configReferences.add(buildConfigReference("httpGet.path", probe.getHttpActionPath()));
    }
    return configReferences;
  }

  private ConfigReference buildConfigReference(String propertyName, Object value) {
    String property = joinProperties(getProbeName(), propertyName);
    String jsonPath = JSONPATH_CONTAINERS_EXPRESSION + getProbeName() + "." + propertyName;
    return new ConfigReference(property, jsonPath, value);
  }

  private boolean isExecOrTcpOrGrpcActionSet() {
    return Strings.isNotNullOrEmpty(probe.getExecAction())
        || Strings.isNotNullOrEmpty(probe.getTcpSocketAction())
        || Strings.isNotNullOrEmpty(probe.getGrpcAction());
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

    int port = Ports.getHttpPort(container).get().getContainerPort();
    String schema = "HTTP";
    // Generally, if the port is either 443 or 8443, then we should use the schema HTTPS
    // TODO: However, we should let users deciding what schema to use.
    if (Ports.isHttps(port)) {
      schema = "HTTPS";
    }

    return new HTTPGetAction(null, Collections.emptyList(), probe.getHttpActionPath(), new IntOrString(port), schema);
  }

  private TCPSocketAction tcpSocketAction(Probe probe) {
    if (Strings.isNullOrEmpty(probe.getTcpSocketAction())) {
      return null;
    }

    String[] parts = probe.getTcpSocketAction().split(":");
    if (parts.length != 2) {
      throw new RuntimeException(
          "Invalid format for tcp socket action! Expected: <host>:<port>. Found:" + probe.getTcpSocketAction() + ".");
    }

    return new TCPSocketAction(parts[0], new IntOrString(parts[1]));
  }

  private GRPCAction grpcAction(Probe probe) {
    String grpcActionExpression = probe.getGrpcAction();
    if (Strings.isNullOrEmpty(grpcActionExpression)) {
      return null;
    }

    try {
      GRPCAction grpcAction;
      if (grpcActionExpression.contains(":")) {
        // both port and service is provided
        String[] parts = grpcActionExpression.split(":");
        grpcAction = new GRPCAction(Integer.valueOf(parts[0]), parts[1]);
      } else {
        grpcAction = new GRPCAction(Integer.valueOf(grpcActionExpression), null);
      }

      return grpcAction;
    } catch (NumberFormatException ex) {
      throw new RuntimeException("Wrong port format set in the gRPC probe. Got: " + grpcActionExpression, ex);
    }
  }

  protected static class Actions {
    protected final ExecAction execAction;
    protected final TCPSocketAction tcpSocketAction;
    protected final HTTPGetAction httpGetAction;
    protected final GRPCAction grpcAction;

    protected Actions(ExecAction execAction, TCPSocketAction tcpSocketAction,
        HTTPGetAction httpGetAction, GRPCAction grpcAction) {
      this.execAction = execAction;
      this.tcpSocketAction = tcpSocketAction;
      this.httpGetAction = httpGetAction;
      this.grpcAction = grpcAction;
    }
  }
}
