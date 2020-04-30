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
package io.dekorate.example.sbonkubernetes;

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.deps.tekton.pipeline.v1beta1.Pipeline;
import io.dekorate.deps.tekton.pipeline.v1beta1.PipelineRun;
import io.dekorate.deps.tekton.pipeline.v1beta1.Task;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskRun;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpringBootWithTektonTest {

  @Test
  public void shouldContainPipeline() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-pipeline.yml"));
    assertNotNull(list);
    Pipeline p = findFirst(list, Pipeline.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(p);
  }

  @Test
  public void shouldContainPipelineRun() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-pipeline-run.yml"));
    assertNotNull(list);
    PipelineRun p = findFirst(list, PipelineRun.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(p);
  }

  @Test
  public void shouldContainTask() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-task.yml"));
    assertNotNull(list);
    Task p = findFirst(list, Task.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(p);
  }

  @Test
  public void shouldContainTaskRun() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-task-run.yml"));
    assertNotNull(list);
    TaskRun p = findFirst(list, TaskRun.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(p);
  }


  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .findFirst();
  }
}
