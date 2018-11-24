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
      "image",
      "env",
      "feature",
      "link"
      })
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
      public class ComponentSpec {

        private String name;
        private String packagingMode;
        private String type;
        private DeploymentType deploymentMode;
        private String runtime;
        private String version;
        private boolean exposeService;
        private String cpu;
        private Storage storage;
        private Image[] image;
        private Env[] env;
        private Service[] service;
        private Feature[] feature;
        private Link[] link;

        public ComponentSpec() {
        }

        public ComponentSpec(String name, String packagingMode, String type, DeploymentType deploymentMode, String runtime, String version, boolean exposeService, String cpu, Storage storage, Image[] image, Env[] env, Service[] service, Feature[] feature, Link[] link) {
          this.name = name;
          this.packagingMode = packagingMode;
          this.type = type;
          this.deploymentMode = deploymentMode;
          this.runtime = runtime;
          this.version = version;
          this.exposeService = exposeService;
          this.cpu = cpu;
          this.storage = storage;
          this.image = image;
          this.env = env;
          this.service = service;
          this.feature = feature;
          this.link = link;
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

        public DeploymentType getDeploymentMode() {
          return deploymentMode;
        }

        public void setDeploymentMode(DeploymentType deploymentMode) {
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

        public Image[] getImage() {
          return image;
        }

        public void setImage(Image[] image) {
          this.image = image;
        }

        public Env[] getEnv() {
          return env;
        }

        public void setEnv(Env[] env) {
          this.env = env;
        }

        public Service[] getService() {
          return service;
        }

        public void setService(Service[] service) {
          this.service = service;
        }

        public Feature[] getFeature() {
          return feature;
        }

        public void setFeature(Feature[] feature) {
          this.feature = feature;
        }

        public Link[] getLink() {
          return link;
        }

        public void setLink(Link[] link) {
          this.link = link;
        }
      }
