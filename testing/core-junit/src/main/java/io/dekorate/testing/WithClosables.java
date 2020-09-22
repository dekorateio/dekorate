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

import static io.dekorate.testing.Testing.Dekorate_STORE;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.DekorateException;

public interface WithClosables {

  String CLOSABLES = "CLOSEABLES";

  default List<Closeable> getCloseables(ExtensionContext context) {
    Object closables = context.getStore(Dekorate_STORE).get(CLOSABLES);
    if (closables instanceof List) {
      return (List<Closeable>) closables;
    }

    closables = new ArrayList<>();
    context.getStore(Dekorate_STORE).put(CLOSABLES, closables);
    return (List<Closeable>) closables;
  }

  default void closeAll(ExtensionContext context) {
    getCloseables(context).forEach(c -> {
      try {
        c.close();
      } catch (IOException e) {
        throw DekorateException.launderThrowable(e);
      }
    });
  }
}
