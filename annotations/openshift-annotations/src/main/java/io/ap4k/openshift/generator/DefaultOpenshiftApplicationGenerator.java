package io.ap4k.openshift.generator;

import io.ap4k.Generator;
import io.ap4k.openshift.annotation.OpenshiftApplication;

import java.util.Collections;
import java.util.List;

public class DefaultOpenshiftApplicationGenerator implements OpenshiftApplicationGenerator {

    public DefaultOpenshiftApplicationGenerator () {
        Generator.registerAnnotationClass(OPENSHIFT, OpenshiftApplication.class); 
        Generator.registerGenerator(OPENSHIFT, this);
    }

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(OpenshiftApplication.class);
    }
}
