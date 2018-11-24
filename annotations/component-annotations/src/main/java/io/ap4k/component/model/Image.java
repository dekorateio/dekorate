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
    "name",
      "annotationCmds",
      "repo",
      "tag",
      "dockerImage"
      })
      @JsonDeserialize(using = io.ap4k.deps.jackson.databind.JsonDeserializer.None.class)
      @Buildable(editableEnabled = false, validationEnabled = false, generateBuilderPackage = false, builderPackage = "io.ap4k.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
      public class Image {

        private String name;
        private boolean annotationCmds;
        private String repo;
        private String tag;
        private String dockerImage;

        public Image() {
        }

        public Image(String name, boolean annotationCmds, String repo, String tag, String dockerImage) {
          this.name = name;
          this.annotationCmds = annotationCmds;
          this.repo = repo;
          this.tag = tag;
          this.dockerImage = dockerImage;
        }

        public String getName() {
          return name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public boolean isAnnotationCmds() {
          return annotationCmds;
        }

        public void setAnnotationCmds(boolean annotationCmds) {
          this.annotationCmds = annotationCmds;
        }

        public String getRepo() {
          return repo;
        }

        public void setRepo(String repo) {
          this.repo = repo;
        }

        public String getTag() {
          return tag;
        }

        public void setTag(String tag) {
          this.tag = tag;
        }

        public String getDockerImage() {
          return dockerImage;
        }

        public void setDockerImage(String dockerImage) {
          this.dockerImage = dockerImage;
        }
      }
