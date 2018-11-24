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

package io.ap4k;

import io.ap4k.deps.kubernetes.api.model.KubernetesList;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The object that holds the state used by all processors.
 * When the state is closed, the session returns multiple {@link KubernetesList} that are created using the following rules:
 *
 * 1. For each named group created by processors create a list will all items assigned explicitly to the group.
 * 2. Items added to no particular group, are appended to all groups.
 * 3. Visitors are applied to each group.
 *
 */
public class Session {

  private static Session INSTANCE;

  private final AtomicBoolean closed = new AtomicBoolean();
  private final Set<Processor> processors = new LinkedHashSet<>();
  private final Configurators configurators = new Configurators();
  private final Resources resources = new Resources();

  /**
   * Creates or resues a single instance of Session.
   * @return  The Session.
   */
  public static Session getSession() {
    if (INSTANCE != null) {
      return INSTANCE;
    }
    synchronized (Session.class) {
      if (INSTANCE == null) {
        INSTANCE = new Session();
      }
    }
    return INSTANCE;
  }

  public Configurators configurators() {
    return configurators;
  }

  public Resources resources() {
    return resources;
  }

  public Set<Processor> generators() {
    return processors;
  }

  public void onClose(Consumer<Session> consumer) {
    if (closed.compareAndSet(false, true)) {
      close();
      consumer.accept(this);
    }
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  public Map<String, KubernetesList> close() {
    this.closed.set(true);
    configurators.stream().forEach(c ->
                                   processors.forEach(g ->  {
                                       if (g.accepts(c.getClass())) {
                                         g.process(c);
                                       }
                                     }));
    return resources.generate();
  }
}
