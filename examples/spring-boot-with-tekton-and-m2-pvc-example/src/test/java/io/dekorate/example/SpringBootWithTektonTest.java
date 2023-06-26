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
package io.dekorate.example;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;
import io.fabric8.tekton.pipeline.v1beta1.PipelineRun;
import io.fabric8.tekton.pipeline.v1beta1.PipelineTask;
import io.fabric8.tekton.pipeline.v1beta1.Task;
import io.fabric8.tekton.pipeline.v1beta1.TaskRun;
import io.fabric8.tekton.pipeline.v1beta1.WorkspaceBinding;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringBootWithTektonTest {

  @Test
  public void shouldContainPipelineWithM2Workspace() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-pipeline.yml"));
    assertNotNull(list);
    Pipeline p = findFirst(list, Pipeline.class);
    assertTrue(p.getSpec().getWorkspaces().stream().filter(w -> w.getName().equals("m2")).findAny().isPresent(), "Pipeline should contain workspace named 'm2'");
    Optional<PipelineTask> buildTask = findTask("project-build", p);
    assertTrue(buildTask.isPresent());

    assertTrue(buildTask.get().getWorkspaces().stream().filter(w -> w.getName().equals("m2") && w.getWorkspace().equals("m2")).findAny().isPresent());
  }

  @Test
  public void shouldContainPipelineRun() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-pipeline-run.yml"));
    assertNotNull(list);
    PipelineRun p = findFirst(list, PipelineRun.class);
    Optional<WorkspaceBinding> binding  = p.getSpec().getWorkspaces().stream().filter(w -> w.getName().equals("m2")).findAny();
    assertTrue(binding.isPresent(), "PipelineRun should contain workspace binding named 'm2'");
    assertEquals("m2-pvc", binding.get().getPersistentVolumeClaim().getClaimName());
  }

  @Test
  public void shouldContainTaskWithM2Workspace() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-task.yml"));
    assertNotNull(list);
    Task t = findFirst(list, Task.class);
    assertTrue(t.getSpec().getWorkspaces().stream().filter(w -> w.getName().equals("m2")).findAny().isPresent());
  }

  @Test
  public void shouldContainTaskRunWithM2PvcBinding() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-task-run.yml"));
    assertNotNull(list);
    TaskRun t = findFirst(list, TaskRun.class);

    Optional<WorkspaceBinding> binding  = t.getSpec().getWorkspaces().stream().filter(w -> w.getName().equals("m2")).findAny();
    assertTrue(binding.isPresent(), "PipelineRun should contain workspace binding named 'm2'");
    assertEquals(binding.get().getPersistentVolumeClaim().getClaimName(), "m2-pvc");
  }

  @Test
  public void shouldContainPersistentVolumeClaimInPipeline() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-pipeline.yml"));
    assertNotNull(list);
    assertGeneratedPersistentVolumeClaim(list);
  }

  @Test
  public void shouldContainPersistentVolumeClaimInTask() {
    KubernetesList list = Serialization.unmarshalAsList(getClass().getClassLoader().getResourceAsStream("META-INF/dekorate/tekton-task.yml"));
    assertNotNull(list);
    assertGeneratedPersistentVolumeClaim(list);
  }

  private void assertGeneratedPersistentVolumeClaim(KubernetesList list) {
    PersistentVolumeClaim v = findFirst(list, PersistentVolumeClaim.class);
    assertEquals("m2-pvc", v.getMetadata().getName());
    assertEquals("m2", v.getSpec().getSelector().getMatchLabels().get("volume-type"));
    assertEquals("standard", v.getSpec().getStorageClassName());
    assertEquals("1Gi", v.getSpec().getResources().getRequests().get("storage").toString());
    assertTrue(v.getSpec().getAccessModes().contains("ReadWriteOnce"));
  }

  Optional<PipelineTask> findTask(String name, Pipeline p) {
    return p.getSpec().getTasks().stream().filter(t -> name.equals(t.getName())).findFirst();
  }

  <T extends HasMetadata> T findFirst(KubernetesList list, Class<T> t) {
    return list.getItems().stream()
      .filter(i -> t.isInstance(i))
      .map(t::cast)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException());
  }
}
