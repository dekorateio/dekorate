/**
 * Copyright 2019 The original authors.
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
 */
package io.dekorate.thorntail.configurator;

import java.util.Arrays;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;

/**
 * For OpenShift deployments, we typically use a base image that is similar to Fabric8 image.
 * It contains a Prometheus agent which messes up the java.util.logging configuration, so we switch it off here.
 * See <a href="http://issues.jboss.org/browse/THORN-1859">THORN-1859</a> for more details.
 */
public class ThorntailPrometheusAgentConfigurator extends Configurator<BaseConfigFluent<?>> {
  private static final String AB_PROMETHEUS_OFF = "AB_PROMETHEUS_OFF";

  @Override
  public void visit(BaseConfigFluent<?> openshiftConfig) {
    boolean alreadyExists = Arrays.stream(openshiftConfig.buildEnvVars())
        .anyMatch(e -> AB_PROMETHEUS_OFF.equals(e.getName()));

    if (alreadyExists) {
      openshiftConfig.editMatchingEnvVar(e -> e.getName().equals(AB_PROMETHEUS_OFF))
          .withValue("true")
          .endEnvVar();
    } else {
      openshiftConfig.addNewEnvVar()
          .withName(AB_PROMETHEUS_OFF)
          .withValue("true")
          .endEnvVar();
    }
  }
}
