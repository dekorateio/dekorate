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

import io.dekorate.kubernetes.decorator.Decorator;

public final class Decorators {

  private Decorators() {
    //Utility classs
  }

  public static Class<? extends Decorator>[] append(Class<? extends Decorator>[] original,
      Class<? extends Decorator>... additional) {
    Class<? extends Decorator>[] result = new Class[original.length + additional.length];
    System.arraycopy(original, 0, result, 0, original.length);
    for (int i = 0; i < additional.length; i++) {
      result[original.length + i] = additional[i];
    }
    return result;
  }
}
