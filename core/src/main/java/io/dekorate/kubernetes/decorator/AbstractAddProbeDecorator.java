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
import io.fabric8.kubernetes.api.model.ContainerPort;
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

  private static final String PATH_ALL_EXPRESSION = "*.spec.containers.";
  private static final String PATH_DEPLOYMENT_CONTAINER_EXPRESSION = "(metadata.name == %s).spec.template.spec.containers.(name == %s).";
  private static final String PATH_DEPLOYMENT_EXPRESSION = "(metadata.name == %s).spec.template.spec.containers.";
  private static final String PATH_CONTAINER_EXPRESSION = "*.spec.containers.(name == %s).";
  private static final Object AUTO_DISCOVER = null;

  protected final Probe probe;

  abstract protected void doCreateProbe(ContainerFluent<?> container, Actions actions);

  abstract protected String getProbeName();

  private ContainerPort httpPort;

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

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class, AddPortDecorator.class,
        ApplyPortNameDecorator.class };
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    List<ConfigReference> configReferences = new ArrayList<>();
    configReferences
        .add(buildConfigReference("failureThreshold", probe.getFailureThreshold(), "The failure threshold to use."));
    configReferences.add(buildConfigReference("initialDelaySeconds", probe.getInitialDelaySeconds(),
        "The amount of time to wait before starting to probe."));
    configReferences.add(
        buildConfigReference("periodSeconds", probe.getPeriodSeconds(), "The period in which the action should be called."));
    configReferences
        .add(buildConfigReference("successThreshold", probe.getSuccessThreshold(), "The success threshold to use."));
    configReferences
        .add(buildConfigReference("timeoutSeconds", probe.getTimeoutSeconds(), "The amount of time to wait for each action."));
    if (isExecOrTcpOrGrpcActionSet()) {
      if (Strings.isNotNullOrEmpty(probe.getExecAction())) {
        configReferences.add(buildConfigReference("exec.command", AUTO_DISCOVER, "The command to use for the probe."));
      } else if (Strings.isNotNullOrEmpty(probe.getGrpcAction())) {
        configReferences.add(buildConfigReference("grpc.port", AUTO_DISCOVER, "The grpc port to use for the probe."));
        configReferences.add(buildConfigReference("grpc.service", AUTO_DISCOVER, "The grpc service to use for the probe."));
      } else if (Strings.isNotNullOrEmpty(probe.getTcpSocketAction())) {
        configReferences
            .add(buildConfigReference("tcpSocket.host", AUTO_DISCOVER, "The tcp host socket to use for the probe."));
        configReferences
            .add(buildConfigReference("tcpSocket.port", AUTO_DISCOVER, "The tcp port socket to use for the probe."));
      }
    } else {
      // default to http action
      configReferences
          .add(buildConfigReference("httpGet.path", probe.getHttpActionPath(), "The http path to use for the probe."));
      configReferences.add(buildConfigReference("httpGet.scheme", AUTO_DISCOVER, "The http schema to use for the probe."));
      if (httpPort != null) {
        configReferences.add(buildConfigReference(joinProperties("ports." + httpPort.getName()),
            "httpGet.port", httpPort.getContainerPort(), "The http port to use for the probe."));
      } else {
        configReferences.add(buildConfigReference("httpGet.port", AUTO_DISCOVER, "The http port to use for the probe."));
      }
    }
    return configReferences;
  }

  private ConfigReference buildConfigReference(String propertyName, Object value, String description) {
    String property = joinProperties(getProbeName(), propertyName);
    return buildConfigReference(property, propertyName, value, description);
  }

  private ConfigReference buildConfigReference(String property, String probeField, Object value, String description) {
    String expression = PATH_ALL_EXPRESSION;
    if (Strings.isNotNullOrEmpty(getDeploymentName()) && Strings.isNotNullOrEmpty(getContainerName())) {
      expression = String.format(PATH_DEPLOYMENT_CONTAINER_EXPRESSION, getDeploymentName(), getContainerName());
    } else if (Strings.isNotNullOrEmpty(getDeploymentName())) {
      expression = String.format(PATH_DEPLOYMENT_EXPRESSION, getDeploymentName());
    } else if (Strings.isNotNullOrEmpty(getContainerName())) {
      expression = String.format(PATH_CONTAINER_EXPRESSION, getContainerName());
    }
    String yamlPath = expression + getProbeName() + "." + probeField;
    return new ConfigReference.Builder(property, yamlPath).withDescription(description).withValue(value).build();
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

    httpPort = Ports.getHttpPort(container).get();
    int port = httpPort.getContainerPort();
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
    if (parts.length == 1) {
      try {
        int port = Integer.parseInt(parts[0]);
        return new TCPSocketAction(null, new IntOrString(port));
      } catch (NumberFormatException e) {
        throw new RuntimeException(
            "Invalid port for tcp socket action! Expected: integer <port>. Found:" + probe.getTcpSocketAction() + ".");
      }
    } else if (parts.length == 2) {
      return new TCPSocketAction(parts[0], new IntOrString(parts[1]));
    }
    throw new RuntimeException(
        "Invalid format for tcp socket action! Expected: <port> or <host>:<port>. Found:" + probe.getTcpSocketAction() + ".");
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
