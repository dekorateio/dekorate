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
 */

package io.ap4k.examples.component;

import io.ap4k.component.annotation.CompositeApplication;
import io.ap4k.component.annotation.Link;
import io.ap4k.kubernetes.annotation.Env;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@CompositeApplication(name = "hello-spring-boot", exposeService = true, envs = @Env(name = "key1", value = "val1"))
@Link(name = "should-be-overridden-by-properties", componentName = "should-be-overridden-by-properties")
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
