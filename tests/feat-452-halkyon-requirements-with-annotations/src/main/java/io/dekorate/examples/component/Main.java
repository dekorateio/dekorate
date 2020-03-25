/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dekorate.examples.component;

import io.dekorate.halkyon.annotation.HalkyonCapability;
import io.dekorate.halkyon.annotation.HalkyonComponent;
import io.dekorate.halkyon.annotation.HalkyonRequiredCapability;
import io.dekorate.halkyon.annotation.Parameter;
import io.dekorate.kubernetes.annotation.Env;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@HalkyonComponent(name = "hello-world", buildType = "docker", exposeService = true, envs = @Env(name = "key1", value = "val1"), provides = @HalkyonCapability(name = "hello-world-endpoint", category = "api", type = "rest-component", version = "1"), requires = @HalkyonRequiredCapability(name = "db", category = "database", type = "postgres", boundTo = "postgres-db", autoBindable = true,
  parameters = @Parameter(name = "postgres-name", value = "postgres-value")))
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
