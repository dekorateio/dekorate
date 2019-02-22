package io.ap4k.prometheus.generator;

import javax.annotation.processing.SupportedAnnotationTypes;

@SupportedAnnotationTypes({"io.ap4k.prometheus.annotation.EnableServiceMonitor"})
public class DefaultServiceMonitorGenerator implements ServiceMonitorGenerator {
}
