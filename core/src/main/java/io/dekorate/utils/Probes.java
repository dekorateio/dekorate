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
package io.dekorate.utils;

import io.dekorate.kubernetes.config.Probe;

public class Probes {

  /**
     Checks if {@link Probe} has been configured.
   * @return true if {@link Probe} has been explicitly configured.
   */
  public static boolean isConfigured(Probe probe) {
    return probe != null
      && (Strings.isNotNullOrEmpty(probe.getHttpActionPath())
          || Strings.isNotNullOrEmpty(probe.getExecAction())
          || Strings.isNotNullOrEmpty(probe.getTcpSocketAction()));
  }
}
