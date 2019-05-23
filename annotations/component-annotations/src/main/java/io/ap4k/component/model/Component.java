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

import io.ap4k.deps.jackson.annotation.JsonIgnore;
import io.ap4k.deps.jackson.annotation.JsonInclude;
import io.ap4k.deps.jackson.annotation.JsonProperty;
import io.ap4k.deps.jackson.annotation.JsonPropertyOrder;
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.javax.validation.Valid;
import io.ap4k.deps.javax.validation.constraints.NotNull;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;
import io.ap4k.deps.kubernetes.api.model.validators.CheckObjectMeta;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;
import io.sundr.builder.annotations.Inline;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "apiVersion",
      "kind",
      "metadata",
      "spec",
      "status"
      })
      @JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"), refs = @BuildableReference(ObjectMeta.class))
      public class Component implements HasMetadata {

        /**
         *
         * (Required)
         *
         */
        @NotNull
        @JsonProperty("apiVersion")
        private String apiVersion = "devexp.runtime.redhat.com/v1alpha2";
        /**
         *
         * (Required)
         *
         */
        @NotNull
        @JsonProperty("kind")
        private String kind = "Component";
        /**
         *
         *
         */
        @JsonProperty("metadata")
        @Valid
        @CheckObjectMeta(regexp = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$", max = 253)
        private ObjectMeta metadata;
        /**
         *
         *
         */
        @JsonProperty("spec")
        @Valid
        private ComponentSpec spec;
        /**
         *
         *
         */
        @JsonProperty("status")
        @Valid
        private ComponentStatus status;

        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public Component() {
        }

        public Component(String apiVersion, String kind, ObjectMeta metadata, ComponentSpec spec, ComponentStatus status, Map<String, Object> additionalProperties) {
          this.apiVersion = apiVersion;
          this.kind = kind;
          this.metadata = metadata;
          this.spec = spec;
          this.status = status;
          this.additionalProperties = additionalProperties;
        }

        @Override
        public String getApiVersion() {
          return apiVersion;
        }

        @Override
        public void setApiVersion(String apiVersion) {
          this.apiVersion = apiVersion;
        }

        @Override
        public String getKind() {
          return kind;
        }

        public void setKind(String kind) {
          this.kind = kind;
        }

        @Override
        public ObjectMeta getMetadata() {
          return metadata;
        }

        @Override
        public void setMetadata(ObjectMeta metadata) {
          this.metadata = metadata;
        }

        public ComponentSpec getSpec() {
          return spec;
        }

        public void setSpec(ComponentSpec spec) {
          this.spec = spec;
        }

        public ComponentStatus getStatus() {
          return status;
        }

        public void setStatus(ComponentStatus status) {
          this.status = status;
        }

        public Map<String, Object> getAdditionalProperties() {
          return additionalProperties;
        }

        public void setAdditionalProperties(Map<String, Object> additionalProperties) {
          this.additionalProperties = additionalProperties;
        }
      }
