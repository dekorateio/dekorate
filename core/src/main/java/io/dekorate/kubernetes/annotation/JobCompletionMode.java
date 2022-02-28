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
package io.dekorate.kubernetes.annotation;

public enum JobCompletionMode {
  /**
   * NonIndexed means that the Job is considered complete when there have been .spec.completions successfully completed Pods.
   * Each Pod completion is homologous to each other.
   */
  NonIndexed,

  /**
   * Indexed means that the Pods of a Job get an associated completion index from 0 to (.spec.completions - 1), available in
   * the annotation batch.kubernetes.io/job-completion-index. The Job is considered complete when there is one successfully
   * completed Pod for each index. When value is Indexed, .spec.completions must be specified and .spec.parallelism must be
   * less than or equal to 10^5. In addition, The Pod name takes the form $(job-name)-$(index)-$(random-string),
   * the Pod hostname takes the form $(job-name)-$(index).
   */
  Indexed
}
