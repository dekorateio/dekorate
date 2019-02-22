package io.ap4k.openshift.generator;

import io.ap4k.openshift.annotation.EnableS2iBuild;

import java.util.Collections;
import java.util.List;

public class DefaultS2iBuildGenerator implements S2iBuildGenerator {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(EnableS2iBuild.class);
    }
}
