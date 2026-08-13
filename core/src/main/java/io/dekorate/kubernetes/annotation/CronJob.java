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
package io.dekorate.kubernetes.annotation;

public @interface CronJob {

  /**
   * The cron job name.
   * 
   * @return The cron job name.
   */
  String name();

  /**
   * The schedule in Cron format, see https://en.wikipedia.org/wiki/Cron.
   *
   * @return The schedule cron expression.
   */
  String schedule();

  /**
   * ConcurrencyPolicy describes how the job will be handled.
   *
   * @return the concurrency policy mode.
   */
  CronJobConcurrencyPolicy concurrencyPolicy() default CronJobConcurrencyPolicy.Allow;

  /**
   * Deadline in seconds for starting the job if it misses scheduled time for any reason.
   * Missed jobs executions will be counted as failed ones.
   *
   * @return the starting deadline seconds attribute.
   */
  long startingDeadlineSeconds() default -1;

  /**
   * The number of failed finished jobs to retain. The default value is 1.
   *
   * @return the failed jobs history limit attribute.
   */
  int failedJobsHistoryLimit() default 1;

  /**
   * The number of successful finished jobs to retain. The default value is 3.
   *
   * @return the successful jobs history limit attribute.
   */
  int successfulJobsHistoryLimit() default 3;

  /**
   * Specifies the maximum desired number of pods the job should run at any given time.
   *
   * @return The desired number of pods.
   */
  int parallelism() default -1;

  /**
   * Specifies the desired number of successfully finished pods the job should be run with.
   *
   * @return The desired number of successfully finished pods.
   */
  int completions() default -1;

  /**
   * CompletionMode specifies how Pod completions are tracked.
   *
   * @return the completion mode.
   */
  JobCompletionMode completionMode() default JobCompletionMode.NonIndexed;

  /**
   * Specifies the number of retries before marking this job failed.
   *
   * @return The back-off limit.
   */
  int backoffLimit() default -1;

  /**
   * Specifies the duration in seconds relative to the startTime that the job may be continuously active before the system
   * tries to terminate it; value must be positive integer.
   *
   * @return the active deadline seconds.
   */
  long activeDeadlineSeconds() default -1;

  /**
   * Limits the lifetime of a Job that has finished execution (either Complete or Failed). If this
   * field is set, ttlSecondsAfterFinished after the Job finishes, it is eligible to be automatically deleted.
   *
   * @return the time to live seconds after finished.
   */
  int ttlSecondsAfterFinished() default -1;

  /**
   * Suspend specifies whether the Job controller should create Pods or not.
   *
   * @return the suspend job attribute.
   */
  boolean suspend() default false;

  /**
   * Restart policy when the job container fails.
   *
   * @return the restart policy.
   */
  JobRestartPolicy restartPolicy() default JobRestartPolicy.OnFailure;

  /**
   * PersistentVolumeClaim volumes to add to all containers.
   */
  PersistentVolumeClaimVolume[] pvcVolumes() default {};

  /**
   * Secret volumes to add to all containers.
   */
  SecretVolume[] secretVolumes() default {};

  /**
   * The ConfigMap volumes to add to all containers.
   */
  ConfigMapVolume[] configMapVolumes() default {};

  /**
   * The EmptyDir volumes to add to all containers.
   */
  EmptyDirVolume[] emptyDirVolumes() default {};

  /**
   * Aws elastic block store volumes to add to all containers
   */
  AwsElasticBlockStoreVolume[] awsElasticBlockStoreVolumes() default {};

  /**
   * Azure disk volumes to add
   */
  AzureDiskVolume[] azureDiskVolumes() default {};

  /**
   * Azure file volumes to add
   */
  AzureFileVolume[] azureFileVolumes() default {};

  /**
   * The containers to be run within the Job execution.
   *
   * @return the list of containers.
   */
  Container[] containers();

}
