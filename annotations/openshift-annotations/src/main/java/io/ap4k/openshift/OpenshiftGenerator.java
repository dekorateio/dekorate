/**
 * Copyright 2015 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.openshift;

import io.ap4k.AbstractKubernetesGenerator;
import io.ap4k.Resources;
import io.ap4k.config.Configuration;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.EditableOpenshiftConfig;

import static io.ap4k.openshift.Constants.SOURCE_TO_IMAGE_CONFIG;

public class OpenshiftGenerator  extends AbstractKubernetesGenerator<OpenshiftConfig> {

        private static final String OPENSHIFT = "openshift";

        public OpenshiftGenerator() {
          super(new Resources());
        }
        public OpenshiftGenerator(Resources resources) {
            super(resources);
        }

        public void generate(OpenshiftConfig config) {
                resources.add(OPENSHIFT, OpenshiftResources.createDeploymentConfig(config));
                addVisitors(OPENSHIFT, config);
        }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(OpenshiftConfig.class) ||
      type.equals(EditableOpenshiftConfig.class);
  }
  
}
