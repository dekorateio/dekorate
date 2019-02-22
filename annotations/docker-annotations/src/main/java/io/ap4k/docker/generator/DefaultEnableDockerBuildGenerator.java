package io.ap4k.docker.generator;

import io.ap4k.docker.annotation.EnableDockerBuild;
import io.ap4k.docker.registrar.EnableDockerBuildRegistrar;

import java.util.Collections;
import java.util.List;

public class DefaultEnableDockerBuildGenerator implements EnableDockerBuildRegistrar {

    @Override
    public List<Class> getSupportedAnnotations() {
        return Collections.singletonList(EnableDockerBuild.class);
    }
}
