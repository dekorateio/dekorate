package io.ap4k.openshift;

import io.ap4k.config.ConfigKey;
import io.ap4k.openshift.config.SourceToImageConfig;
import io.ap4k.openshift.config.SourceToImageConfigBuilder;

public class Constants {

    public static String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";


    public static SourceToImageConfig DEFAULT_SOURCE_TO_IMAGE_CONFIG = new SourceToImageConfigBuilder()
            .withBuilderImage(DEFAULT_S2I_BUILDER_IMAGE)
            .build();

    public static ConfigKey<SourceToImageConfig> SOURCE_TO_IMAGE_CONFIG = new ConfigKey<SourceToImageConfig>("SOURCE_TO_IMAGE_CONFIG", SourceToImageConfig.class, DEFAULT_SOURCE_TO_IMAGE_CONFIG);
}
