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
import io.ap4k.deps.jackson.databind.annotation.JsonDeserialize;
import io.ap4k.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
/**
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
      "name",
      "description"
      })
      @JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
      public class Feature {

        private String id;

        private String name;

        private String description;

        public Feature() {
        }

        public Feature(String id, String name, String description) {
          this.id = id;
          this.name = name;
          this.description = description;
        }

        public String getId() {
          return id;
        }

        public void setId(String id) {
          this.id = id;
        }

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getDescription() {
          return description;
        }

        public void setDescription(String description) {
          this.description = description;
        }
      }
