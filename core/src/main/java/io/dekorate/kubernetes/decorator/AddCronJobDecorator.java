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

package io.dekorate.kubernetes.decorator;

import static io.dekorate.kubernetes.adapter.ContainerAdapter.applyContainerToBuilder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.kubernetes.config.CronJob;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobFluent;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobSpecFluent;
import io.fabric8.kubernetes.api.model.batch.v1.JobTemplateSpecFluent;

@Description("Add a job to the list.")
public class AddCronJobDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private static final String API_VERSION = "batch/v1";
  private static final String KIND = "CronJob";

  private final BaseConfig config;
  private final CronJob job;

  public AddCronJobDecorator(BaseConfig config, CronJob job) {
    this.config = config;
    this.job = job;

    if (Strings.isNullOrEmpty(job.getSchedule())) {
      throw new RuntimeException("CronJob schedule attribute is mandatory");
    }

    if (job.getContainers() == null || job.getContainers().length == 0) {
      throw new RuntimeException("No containers set in the Job definition.");
    }
  }

  public void visit(KubernetesListBuilder list) {
    String name = Strings.defaultIfEmpty(job.getName(), config.getName());
    if (contains(list, API_VERSION, KIND, name)) {
      return;
    }

    CronJobFluent.SpecNested<CronJobBuilder> cronJobBuilder = new CronJobBuilder()
        .withApiVersion(API_VERSION)
        .withNewMetadata()
        .withName(name)
        .withLabels(Labels.createLabelsAsMap(config, KIND))
        .endMetadata()
        .withNewSpec()
        .withSuspend(job.getSuspend())
        .withSchedule(job.getSchedule())
        .withSuccessfulJobsHistoryLimit(job.getSuccessfulJobsHistoryLimit())
        .withConcurrencyPolicy(job.getConcurrencyPolicy().name())
        .withFailedJobsHistoryLimit(job.getFailedJobsHistoryLimit());

    if (job.getStartingDeadlineSeconds() >= 0) {
      cronJobBuilder = cronJobBuilder.withStartingDeadlineSeconds(job.getStartingDeadlineSeconds());
    }

    JobTemplateSpecFluent.SpecNested<CronJobSpecFluent.JobTemplateNested<CronJobFluent.SpecNested<CronJobBuilder>>> jobBuilder = cronJobBuilder
        .withNewJobTemplate()
        .withNewSpec()
        .withCompletionMode(job.getCompletionMode().name());

    if (job.getParallelism() >= 0) {
      jobBuilder = jobBuilder.withParallelism(job.getParallelism());
    }

    if (job.getCompletions() >= 0) {
      jobBuilder = jobBuilder.withCompletions(job.getCompletions());
    }

    if (job.getBackoffLimit() >= 0) {
      jobBuilder = jobBuilder.withBackoffLimit(job.getBackoffLimit());
    }

    if (job.getActiveDeadlineSeconds() >= 0) {
      jobBuilder = jobBuilder.withActiveDeadlineSeconds(job.getActiveDeadlineSeconds());
    }

    if (job.getTtlSecondsAfterFinished() >= 0) {
      jobBuilder = jobBuilder.withTtlSecondsAfterFinished(job.getTtlSecondsAfterFinished());
    }

    list.addToItems(jobBuilder
        .withNewTemplate()
        .withNewSpec()
        .addAllToContainers(Stream.of(job.getContainers()).map(this::toKubernetesContainer).collect(Collectors.toSet()))
        .withRestartPolicy(job.getRestartPolicy().name())
        .endSpec()
        .endTemplate()
        .endSpec()
        .endJobTemplate()
        .endSpec()
        .build());
  }

  private io.fabric8.kubernetes.api.model.Container toKubernetesContainer(Container container) {
    ContainerBuilder builder = new ContainerBuilder();
    applyContainerToBuilder(builder, container);
    if (Strings.isNullOrEmpty(builder.getName())) {
      builder = builder.withName(Images.getName(container.getImage()));
    }

    return builder.build();
  }
}
