package io.ap4k.prometheus.generator;

import io.ap4k.prometheus.annotation.EnableServiceMonitor;

import java.util.Collections;
import java.util.List;

public class DefaultServiceMonitorGenerator implements ServiceMonitorGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(EnableServiceMonitor.class);
    }
}
