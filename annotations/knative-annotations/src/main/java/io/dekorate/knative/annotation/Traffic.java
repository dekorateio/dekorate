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
package io.dekorate.knative.annotation;

public @interface Traffic {

  /**
   * Tag is optionally used to expose a dedicated url for referencing
   * this target exclusively.
   */
  String tag() default "";

  /**
   * RevisionName of a specific revision to which to send this portion of traffic.
   */
  String revisionName() default "";

  /**
   * LatestRevision may be optionally provided to indicate that the latest
   * ready Revision of the Configuration should be used for this traffic
   * target. When provided LatestRevision must be true if RevisionName is
   * empty.
   */
  boolean latestRevision() default false;

  /**
   * Percent indicates that percentage based routing should be used and
   * the value indicates the percent of traffic that is be routed to this
   * Revision or Configuration. `0` (zero) mean no traffic, `100` means all
   * traffic.
   */
  int percentage() default 100;

}
