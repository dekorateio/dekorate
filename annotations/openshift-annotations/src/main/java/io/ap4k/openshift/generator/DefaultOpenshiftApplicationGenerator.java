package io.ap4k.openshift.generator;

import io.ap4k.openshift.annotation.OpenshiftApplication;

import java.util.Collections;
import java.util.List;

public class DefaultOpenshiftApplicationGenerator implements OpenshiftApplicationGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(OpenshiftApplication.class);
    }
}
