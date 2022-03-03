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

import static io.dekorate.testing.KubernetesResources.loadGenerated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import io.dekorate.utils.Labels;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;

public class Feat856Test {

  private static final String EXPECTED_JOB_NAME = "say-hello";
  private static final String EXPECTED_IMAGE_NAME = "hello";
  private static final String EXPECTED_IMAGE = "docker.io/user/" + EXPECTED_IMAGE_NAME;

  @Test
  public void shouldGenerateJobResource() {
    KubernetesList list = loadGenerated("kubernetes");
    assertNotNull(list);
    Job job = findFirst(list, Job.class).orElseThrow(() -> new IllegalStateException());
    assertNotNull(job, "Job was not generated!");
    Map<String, String> labels = job.getMetadata().getLabels();
    assertNotNull(labels);
    assertTrue(labels.containsKey(Labels.NAME));
    assertTrue(labels.containsKey(Labels.VERSION));
    assertEquals(EXPECTED_JOB_NAME, job.getMetadata().getName());
    assertEquals(EXPECTED_IMAGE_NAME, job.getSpec().getTemplate().getSpec().getContainers().get(0).getName());
    assertEquals(EXPECTED_IMAGE, job.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

    // verify volumes
    assertVolume(job, "secret", v -> v.getSecret().getSecretName().equals("secretName"));
    assertVolume(job, "pvc", v -> v.getPersistentVolumeClaim().getClaimName().equals("claimName"));
    assertVolume(job, "configMap", v -> v.getConfigMap().getName().equals("configMapName"));
    assertVolume(job, "awsElastic", v -> v.getAwsElasticBlockStore().getVolumeID().equals("volumeId")
        && v.getAwsElasticBlockStore().getPartition().equals(3));
    assertVolume(job, "azureDisk", v -> v.getAzureDisk().getDiskName().equals("diskName")
        && v.getAzureDisk().getDiskURI().equals("diskURI"));
    assertVolume(job, "azureFile", v -> v.getAzureFile().getShareName().equals("shareName")
        && v.getAzureFile().getSecretName().equals("secretName"));
  }

  private void assertVolume(Job job, String name, Predicate<Volume> assertion) {
    assertTrue(job.getSpec().getTemplate().getSpec().getVolumes().stream()
        .anyMatch(v -> v.getName().equals(name) && assertion.test(v)));
  }

  <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream()
        .filter(i -> t.isInstance(i))
        .findFirst();
  }
}
