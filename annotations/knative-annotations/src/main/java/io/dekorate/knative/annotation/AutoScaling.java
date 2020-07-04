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

package io.dekorate.knative.annotation;

import io.dekorate.knative.config.AutoScalerClass;
import io.dekorate.knative.config.AutoscalingMetric;

public @interface AutoScaling {

  /**
   * The Autoscaler class.
   * Knative Serving comes with its own autoscaler, the KPA (Knative Pod Autoscaler) but can also be configured to use Kubernetes’ HPA (Horizontal Pod Autoscaler) or even a custom third-party autoscaler.
   * Possible values (kpa, hpa, default: kpa).
   * @return The autoscaler class.
   */

  AutoScalerClass autoScalerClass() default AutoScalerClass.kpa;


  /**
   * The autoscaling metric to use. 
   * Possible values (concurency, rps, cpu).
   * @return The cpu metric or NONE if no metric has been selected.
   */
  AutoscalingMetric metric() default AutoscalingMetric.concurrency;

  /**
   * The autoscaling target. 
   * @reutrn the selected target or zero if no target is selected.
   */
  int target() default 0;

  /**
   * The exact amount of requests allowed to the replica at a time.
   * Its default value is “0”, which means an unlimited number of requests are allowed to flow into the replica. 
   * @return the container concurrenct or zero if its not bound.
   */
  int containerConcurrency() default 0;

  /**
   * This value specifies a percentage of the target to actually be targeted by the autoscaler.
   */
  int targetUtilizationPercentage() default 70;
}
