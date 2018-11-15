package io.ap4k.openshift.visitor;

import io.ap4k.config.ConfigurationFluent;
import io.ap4k.openshift.config.SourceToImageConfig;
import io.fabric8.kubernetes.api.builder.TypedVisitor;

import static io.ap4k.openshift.Constants.SOURCE_TO_IMAGE_CONFIG;

public class ApplySourceToImageConfig extends TypedVisitor<ConfigurationFluent> {

    private final SourceToImageConfig sourceToImageConfig;

    public ApplySourceToImageConfig(SourceToImageConfig sourceToImageConfig) {
        this.sourceToImageConfig = sourceToImageConfig;
    }

    @Override
    public void visit(ConfigurationFluent configuration) {
        configuration.addToAttributes(SOURCE_TO_IMAGE_CONFIG, sourceToImageConfig);
    }
}
