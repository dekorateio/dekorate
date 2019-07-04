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
package io.dekorate.testing;

import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

public class Testing {

  /**
   * This is an {@link org.junit.jupiter.api.extension.ExtensionContext.Namespace} that is used to store all dekorate
   * related state.
   * Note: Not to be confused with KubernetesExtension namespaces, this is a junit5 construct.
   */
  public static Namespace Dekorate_STORE = Namespace.create("Dekorate_STORE");





}
