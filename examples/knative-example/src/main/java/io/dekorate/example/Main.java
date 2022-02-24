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
package io.dekorate.example;

import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.kubernetes.annotation.Probe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@KnativeApplication(minScale = 1, maxScale = 5, scaleToZeroEnabled = false,
  readinessProbe = @Probe(httpActionPath = "/readiness", periodSeconds = 30, timeoutSeconds = 10),
  livenessProbe = @Probe(httpActionPath = "/liveness", periodSeconds = 31, timeoutSeconds = 11),
  startupProbe = @Probe(httpActionPath = "/startup", periodSeconds = 32, timeoutSeconds = 12)
)
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
