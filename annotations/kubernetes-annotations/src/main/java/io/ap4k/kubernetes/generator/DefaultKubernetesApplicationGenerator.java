package io.ap4k.kubernetes.generator;

import io.ap4k.kubernetes.annotation.KubernetesApplication;

import java.util.Collections;
import java.util.List;

public class DefaultKubernetesApplicationGenerator implements KubernetesApplicationGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(KubernetesApplication.class);
    }
}
