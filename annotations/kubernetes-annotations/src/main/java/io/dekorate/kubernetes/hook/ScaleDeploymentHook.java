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
package io.dekorate.kubernetes.hook;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.hook.ProjectHook;
import io.dekorate.project.Project;

public class ScaleDeploymentHook extends ProjectHook {

    private final String name;
    private final int replicas;
    private Logger LOGGER = LoggerFactory.getLogger();

    public ScaleDeploymentHook(Project project, String name, int replicas) {
        super(project);
        this.name = name;
        this.replicas = replicas;
    }

    @Override
    public void init() {

    }

    @Override
    public void warmup() {

    }

    @Override
    public void run() {
      LOGGER.info("Performing docker build.");
      exec("kubectl", "scale", "deployment/" + name, "--replicas="+replicas);
    }
}
