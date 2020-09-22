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

package io.dekorate.tekton.decorator;

import static io.dekorate.utils.Metadata.getMetadata;

import java.util.Optional;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.PipelineSpecFluent;

public abstract class NamedPipelineDecorator extends Decorator<VisitableBuilder> {

  protected static final String ANY = null;

  private final String pipelineName;

  private final PipelineVisitor pipelineVisitor = new PipelineVisitor();

  public NamedPipelineDecorator(String pipelineName) {
    this.pipelineName = pipelineName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(pipelineName)
        || objectMeta.map(m -> m.getName()).filter(s -> s.equals(pipelineName)).isPresent()) {
      builder.accept(pipelineVisitor);
    }
  }

  public abstract void andThenVisit(PipelineSpecFluent<?> pipelineSpec);

  private class PipelineVisitor extends TypedVisitor<PipelineSpecFluent<?>> {

    @Override
    public void visit(PipelineSpecFluent<?> pipelineSpec) {
      andThenVisit(pipelineSpec);
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

}
