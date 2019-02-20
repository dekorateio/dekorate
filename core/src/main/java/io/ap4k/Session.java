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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
  private final AtomicBoolean generated = new AtomicBoolean();

  private final Set<Handler> handlers = new LinkedHashSet<>();
  private final Configurators configurators = new Configurators();
  private final Resources resources = new Resources();

  private final Map<String, KubernetesList> generatedResources= new HashMap<>();
  private final AtomicReference<SessionWriter> writer = new AtomicReference<>();
  private final Set<SessionListener> listeners = new LinkedHashSet<>();


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

  public Set<Handler> handlers() {
    return handlers;
  }

  public Map<String, KubernetesList> getGeneratedResources() {
    return generatedResources;
  }

  public void setWriter(SessionWriter resourceWriter) {
    this.writer.set(resourceWriter);
  }

  public boolean hasWriter() {
    return this.writer.get() != null;
  }

  public void addListener(SessionListener listener) {
    listeners.add(listener);
  }

  public void close() {
    if (closed.compareAndSet(false, true)) {
      generate();
      SessionWriter w = writer.get();
      if (w == null) {
        throw new IllegalStateException("No writer has been specified!");
      }
      w.write(this);
      listeners.forEach(SessionListener::onClosed);
    }
  }

  /**
   * Close the session an get all resource groups.
   * @return A map of {@link KubernetesList} by group name.
   */
  private Map<String, KubernetesList> generate() {
    if (generated.compareAndSet(false, true)) {
      closed.set(true);
        handlers.forEach(g ->
          configurators.stream().forEach(c -> {
          if (g.canHandle(c.getClass())) {
            g.handle(c);
          }
        }));
      this.generatedResources.putAll(resources.generate());
    }
    return Collections.unmodifiableMap(generatedResources);
  }

}
