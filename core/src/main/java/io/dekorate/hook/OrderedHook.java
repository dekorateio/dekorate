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
package io.dekorate.hook;

import io.dekorate.project.Project;

/**
 * Shutdown hooks are not ordered, so we use this hook, in order to ensure execution order.
 */
public class OrderedHook extends ProjectHook {

  private final ProjectHook[] hooks;

  public static OrderedHook create(ProjectHook... hooks)  {
    return new OrderedHook(hooks);
  }

  public OrderedHook(ProjectHook[] hooks) {
    super(projectOf(hooks));
    this.hooks = hooks;
  }

  @Override
  public void init() {
    for(ProjectHook h : hooks) {
      h.init();
    }
  }

  @Override
  public void warmup() {
    for(ProjectHook h : hooks) {
      h.warmup();
    }
  }

  @Override
  public void run() {
    for(ProjectHook h : hooks) {
      h.run();
    }
  }

  /**
   * Get the first non-null project found in the specified hooks.
   * @param hooks The hooks.
   * @return      The project or null, if no project found.
   */
  private static Project projectOf(ProjectHook... hooks)  {
    for (ProjectHook h : hooks) {
      if (h.project != null) {
        return h.project;
      }
    }
    return null;
  }
}
