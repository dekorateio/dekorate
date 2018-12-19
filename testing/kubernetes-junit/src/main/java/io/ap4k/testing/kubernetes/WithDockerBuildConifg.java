/**
 * Copyright 2018 The original authors.
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
package io.ap4k.testing.kubernetes;

import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.utils.Serialization;

public interface WithDockerBuildConifg {
  String DOCKER_CONFIG_PATH = "META-INF/ap4k/.config/dockerbuild.yml";

  default boolean hasDockerBuildConfig()  {
    return WithDockerBuildConifg.class.getClassLoader().getResource(DOCKER_CONFIG_PATH) != null;
  }

  default DockerBuildConfig getDockerBuildConfig() {
    return  Serialization.unmarshal(WithDockerBuildConifg.class.getClassLoader().getResourceAsStream(DOCKER_CONFIG_PATH), DockerBuildConfig.class);
  }

}
