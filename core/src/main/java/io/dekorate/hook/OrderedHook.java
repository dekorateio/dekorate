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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    doWithHooks(ProjectHook::init);
  }

  @Override
  public void warmup() {
    doWithHooks(ProjectHook::warmup);
  }

  @Override
  public void run() {
    doWithHooks(ProjectHook::run);
  }

  public void doWithHooks(Consumer<ProjectHook> c) {
    List<ProjectHook> all = Arrays.asList(hooks);
    List<ProjectHook> visited = new ArrayList<>();
    List<ProjectHook> pending = new ArrayList<>(all);
    for (ProjectHook h : all) {
      try {
        c.accept(h);
        visited.add(h);
        pending.remove(h);
      } catch (Exception e) {
        System.out.println("Error while calling hook:" + h.getClass().getTypeName()+". Message:" + e.getMessage());
        System.out.println("Aborting execution of hooks:" + pending.stream().map(a -> a.getClass().getTypeName()).collect(Collectors.joining(", "))+ ".");
        e.printStackTrace();
      }
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
