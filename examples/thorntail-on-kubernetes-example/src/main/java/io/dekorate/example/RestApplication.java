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

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.annotation.Probe;
import io.dekorate.option.annotation.JvmOptions;

@KubernetesApplication(livenessProbe = @Probe(httpActionPath = "/health", initialDelaySeconds = 180), readinessProbe = @Probe(httpActionPath = "/health", initialDelaySeconds = 20))
@JvmOptions(preferIPv4Stack = true)
@ApplicationPath("/")
public class RestApplication extends Application {
}
