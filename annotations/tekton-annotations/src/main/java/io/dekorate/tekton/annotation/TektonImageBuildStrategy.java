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

package io.dekorate.tekton.annotation;

import io.dekorate.tekton.step.BuildahBuildStep;
import io.dekorate.tekton.step.DockerBuildStep;
import io.dekorate.tekton.step.ImageBuildStep;
import io.dekorate.tekton.step.KanikoBuildStep;

public enum TektonImageBuildStrategy {

  kaniko(new KanikoBuildStep()),
  docker(new DockerBuildStep()),
  buildah(new BuildahBuildStep());

  ImageBuildStep<?> step;

  private TektonImageBuildStrategy(ImageBuildStep<?> step) {
    this.step = step;
  }

  public ImageBuildStep<?> getStep() {
    return this.step;
  }
}
