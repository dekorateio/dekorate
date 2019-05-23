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
package io.ap4k.component.model;

import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

import javax.annotation.Generated;

/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "name",
      "type",
      "packagingMode",
      "deploymentMode",
      "runtime",
      "version",
      "exposeService",
      "cpu",
      "strorage",
      "images",
      "envs",
      "features",
      "links"
      })
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
      public class ComponentSpec {

        private String name;
        private String packagingMode;
        private String type;
        private DeploymentMode deploymentMode;
        private String runtime;
        private String version;
        private boolean exposeService;
        private String cpu;
        private Storage storage;
        private Image[] images;
        private Env[] envs;
        private Service[] services;
        private Feature[] features;
        private Link[] links;

        public ComponentSpec() {
        }

        public ComponentSpec(String name, String packagingMode, String type, DeploymentMode deploymentMode, String runtime, String version, boolean exposeService, String cpu, Storage storage, Image[] images, Env[] envs, Service[] services, Feature[] features, Link[] links) {
          this.name = name;
          this.packagingMode = packagingMode;
          this.type = type;
          this.deploymentMode = deploymentMode;
          this.runtime = runtime;
          this.version = version;
          this.exposeService = exposeService;
          this.cpu = cpu;
          this.storage = storage;
          this.images = images;
          this.envs = envs;
          this.services = services;
          this.features = features;
          this.links = links;
        }

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getPackagingMode() {
          return packagingMode;
        }

        public void setPackagingMode(String packagingMode) {
          this.packagingMode = packagingMode;
        }

        public String getType() {
          return type;
        }

        public void setType(String type) {
          this.type = type;
        }

        public DeploymentMode getDeploymentMode() {
          return deploymentMode;
        }

        public void setDeploymentMode(DeploymentMode deploymentMode) {
          this.deploymentMode = deploymentMode;
        }

        public String getRuntime() {
          return runtime;
        }

        public void setRuntime(String runtime) {
          this.runtime = runtime;
        }

        public String getVersion() {
          return version;
        }

        public void setVersion(String version) {
          this.version = version;
        }

        public boolean isExposeService() {
          return exposeService;
        }

        public void setExposeService(boolean exposeService) {
          this.exposeService = exposeService;
        }

        public String getCpu() {
          return cpu;
        }

        public void setCpu(String cpu) {
          this.cpu = cpu;
        }

        public Storage getStorage() {
          return storage;
        }

        public void setStorage(Storage storage) {
          this.storage = storage;
        }

        public Image[] getImages() {
          return images;
        }

        public void setImages(Image[] images) {
          this.images = images;
        }

        public Env[] getEnvs() {
          return envs;
        }

        public void setEnvs(Env[] envs) {
          this.envs = envs;
        }

        public Service[] getServices() {
          return services;
        }

        public void setServices(Service[] services) {
          this.services = services;
        }

        public Feature[] getFeatures() {
          return features;
        }

        public void setFeatures(Feature[] features) {
          this.features = features;
        }

        public Link[] getLinks() {
          return links;
        }

        public void setLinks(Link[] links) {
          this.links = links;
        }
      }
