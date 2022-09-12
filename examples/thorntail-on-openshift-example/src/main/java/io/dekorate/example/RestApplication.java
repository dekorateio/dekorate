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
package io.dekorate.example;

import io.dekorate.kubernetes.annotation.Probe;
import io.dekorate.openshift.annotation.OpenshiftApplication;
import io.dekorate.openshift.annotation.Route;
import io.dekorate.option.annotation.JvmOptions;
import io.dekorate.option.annotation.SecureRandomSource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@OpenshiftApplication(
  livenessProbe = @Probe(httpActionPath = "/health", initialDelaySeconds = 180),
  readinessProbe = @Probe(httpActionPath = "/health", initialDelaySeconds = 20),
  route = @Route(expose = true)
)
@JvmOptions(server = true, preferIPv4Stack = true, secureRandom = SecureRandomSource.NonBlocking)
@ApplicationPath("/")
public class RestApplication extends Application {
}
