package io.ap4k.openshift;

import io.ap4k.AbstractKubernetesGenerator;
import io.ap4k.Resources;
import io.ap4k.openshift.config.OpenshiftConfig;

import static io.ap4k.openshift.Constants.SOURCE_TO_IMAGE_CONFIG;

public class OpenshiftGenerator  extends AbstractKubernetesGenerator<OpenshiftConfig> {

        private static final String OPENSHIFT = "openshift";

        public OpenshiftGenerator(Resources resources) {
            super(resources);
        }

        public void generate(OpenshiftConfig config) {
                resources.add(OPENSHIFT, OpenshiftResources.createDeploymentConfig(config));
                if (config.hasAttribute(SOURCE_TO_IMAGE_CONFIG)) {
                    resources.add(OPENSHIFT, OpenshiftResources.createBuilderImageStream(config));
                    resources.add(OPENSHIFT, OpenshiftResources.createProjectImageStream(config));
                    resources.add(OPENSHIFT, OpenshiftResources.createBuildConfig(config));
                }
                addVisitors(OPENSHIFT, config);
        }

  @Override
  public Class<? extends OpenshiftConfig> getType() {
    return OpenshiftConfig.class;
  }
}
