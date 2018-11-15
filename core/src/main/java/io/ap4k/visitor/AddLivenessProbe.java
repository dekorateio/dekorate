package io.ap4k.visitor;

import io.ap4k.config.Probe;
import io.ap4k.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class AddLivenessProbe extends AbstractAddProbe {

    public AddLivenessProbe(Probe probe) {
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
        container.withNewLivenessProbe()
                .withExec(execAction(probe))
                .withHttpGet(httpGetAction(probe))
                .withTcpSocket(tcpSocketAction(probe))
                .withInitialDelaySeconds(probe.getInitialDelaySeconds())
                .withPeriodSeconds(probe.getPeriodSeconds())
                .withTimeoutSeconds(probe.getTimeoutSeconds())
                .endLivenessProbe();
    }

}
