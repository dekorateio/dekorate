package io.ap4k.spring.config;

import io.ap4k.kubernetes.config.Configuration;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder", refs = @BuildableReference(Configuration.class))
public class SpringApplicationConfig extends Configuration {
}
