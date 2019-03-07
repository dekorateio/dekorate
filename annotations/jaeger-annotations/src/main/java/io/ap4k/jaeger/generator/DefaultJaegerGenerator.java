package io.ap4k.jaeger.generator;

import io.ap4k.jaeger.annotation.EnableJaegerAgent;

import java.util.Collections;
import java.util.List;

public class DefaultJaegerGenerator implements JaegerAgentGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(EnableJaegerAgent.class);
    }
}
