package io.ap4k.decorator;

import io.ap4k.config.Probe;
import io.ap4k.utils.Strings;
import io.ap4k.deps.kubernetes.api.model.ContainerBuilder;

public class AddReadinessProbe extends AbstractAddProbe {

    public AddReadinessProbe(Probe probe) {
        super(probe);
    }

    @Override
    public void visit(ContainerBuilder container) {
        if (probe == null) {
            return;
        }
        if (Strings.isNullOrEmpty(probe.getExecAction()) &&
                Strings.isNullOrEmpty(probe.getHttpAction()) &&
                Strings.isNullOrEmpty(probe.getTcpSocketAction())) {
            return;
        }
        container.withNewReadinessProbe()
                .withExec(execAction(probe))
                .withHttpGet(httpGetAction(probe))
                .withTcpSocket(tcpSocketAction(probe))
                .withInitialDelaySeconds(probe.getInitialDelaySeconds())
                .withPeriodSeconds(probe.getPeriodSeconds())
                .withTimeoutSeconds(probe.getTimeoutSeconds())
                .endReadinessProbe();
    }

}
