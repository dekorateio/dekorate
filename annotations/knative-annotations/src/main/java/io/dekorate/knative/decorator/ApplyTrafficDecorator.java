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
package io.dekorate.knative.decorator;

import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.knative.serving.v1.ServiceSpecFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ApplyTrafficDecorator extends NamedResourceDecorator<ServiceSpecFluent<?>> {

  private final String revisionName;
  private final boolean latestRevision;
  private final long percentage;
  private final String tag;

 	public ApplyTrafficDecorator(String name, String revisionName, boolean latestRevision, long percentage, String tag) {
		super(name);
		this.revisionName = revisionName;
		this.latestRevision = latestRevision;
		this.percentage = percentage;
		this.tag = tag;
	}

 	public ApplyTrafficDecorator(String name, long percentage, String tag) {
    this(ANY, null, true, percentage, tag);
  }

 	public ApplyTrafficDecorator(String name, long percentage) {
    this(ANY, null, true, percentage, null);
  }


	public ApplyTrafficDecorator(String revisionName, boolean latestRevision, long percentage, String tag) {
    this(ANY, revisionName, latestRevision, percentage, tag);
	}

	public ApplyTrafficDecorator(long percentage, String tag) {
    this(ANY, null, true, percentage, tag);
	}

	public ApplyTrafficDecorator(long percentage) {
    this(ANY, null, true, percentage, null);
	}

	@Override
	public void andThenVisit(ServiceSpecFluent<?> spec, ObjectMeta resourceMeta) {
    spec.addNewTraffic()
      .withRevisionName(revisionName)
      .withLatestRevision(latestRevision)
      .withPercent(percentage)
      .withTag(tag)
      .endTraffic();
	}
}
