package io.ap4k.jaeger.config;

import io.ap4k.kubernetes.annotation.Protocol;
import io.ap4k.kubernetes.config.Port;
import io.ap4k.kubernetes.config.PortBuilder;

public class Defaults {

  public static final String AGENT_NAME = "jaeger-agent";
  public static final String AGENT_IMAGE = "jaegertracing/jaeger-agent";

  public static final Port[] AGENT_PORTS = new Port[] {
    new PortBuilder().withName("zipkin-compact").withContainerPort(5775).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("jaeger-compact").withContainerPort(6831).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("jaeger-binary").withContainerPort(6832).withProtocol(Protocol.UDP).build(),
    new PortBuilder().withName("save-config-sampling-strategies").withContainerPort(5778).withProtocol(Protocol.TCP).build()
  };
}
