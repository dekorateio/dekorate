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

public @interface AzureDiskVolume {

  /**
   * The volumeName name.
   * @return  The volumeName name.
   */
  String volumeName();

  /**
   * The name of the disk to mount.
   * @return  The name.
   */
  String diskName();

  String diskURI();

  String kind() default "Managed";

  String cachingMode() default "ReadWrite";

  String fsType() default "ext4";

  /**
   * Wether the volumeName is read only or not.
   * @return  True if read only, False otherwise.
   */
  boolean readOnly() default false;

}
