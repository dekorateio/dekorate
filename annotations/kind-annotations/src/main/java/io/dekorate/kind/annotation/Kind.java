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
package io.dekorate.kind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.annotation.ServiceType;
import io.dekorate.kubernetes.config.BaseConfig;
import io.dekorate.project.BuildInfo;
import io.dekorate.project.Project;
import io.sundr.builder.annotations.Adapter;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder", refs = {
    @BuildableReference(Project.class),
    @BuildableReference(BuildInfo.class),
    @BuildableReference(io.dekorate.kubernetes.config.Label.class),
    @BuildableReference(io.dekorate.kubernetes.config.Annotation.class),
    @BuildableReference(io.dekorate.kubernetes.config.Env.class),
    @BuildableReference(io.dekorate.kubernetes.config.PersistentVolumeClaimVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.SecretVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.ConfigMapVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.EmptyDirVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.GitRepoVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.AzureDiskVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.AzureFileVolume.class),
    @BuildableReference(io.dekorate.kubernetes.config.Mount.class),
    @BuildableReference(io.dekorate.kubernetes.config.RollingUpdate.class),
    @BuildableReference(io.dekorate.kubernetes.config.HostAlias.class),
    @BuildableReference(io.dekorate.kubernetes.config.Container.class),
    @BuildableReference(io.dekorate.kubernetes.config.Job.class),
    @BuildableReference(io.dekorate.kubernetes.config.CronJob.class)
})
@Pojo(name = "KindConfig", relativePath = "../config", autobox = true, mutable = true, superClass = BaseConfig.class, withStaticBuilderMethod = true, withStaticAdapterMethod = false, adapter = @Adapter(name = "KindConfigAdapter", relativePath = "../adapter", withMapAdapterMethod = true))
@Target({ ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Kind {

  boolean enabled() default true;

  /**
   * Image pull policy.
   *
   * @return The image pull policy.
   */
  ImagePullPolicy imagePullPolicy() default ImagePullPolicy.IfNotPresent;

  /**
   * The application ports.
   */
  Port[] ports() default {};

  /**
   * The type of service that will be generated for the application.
   */
  ServiceType serviceType() default ServiceType.NodePort;

}
