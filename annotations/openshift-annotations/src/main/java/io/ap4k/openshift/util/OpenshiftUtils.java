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
package io.ap4k.openshift.util;

import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.deps.kubernetes.api.model.ObjectReference;
import io.ap4k.deps.openshift.api.model.ImageStreamTag;
import io.ap4k.deps.openshift.api.model.SourceBuildStrategyFluent;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenshiftUtils {

  private static final OpenShiftClient client = new DefaultOpenShiftClient();

  /**
   * Wait for the references ImageStreamTags to become available.
   * @param items       A list of items, possibly referencing image stream tags.
   * @param amount      The max amount of time to wait.
   * @param timeUnit    The time unit of the time to wait.
   * @return            True if the items became available false otherwise.
   */
  public static boolean waitForImageStreamTags(List<HasMetadata> items, long amount, TimeUnit timeUnit) {
    if (items == null || items.isEmpty()) {
      return true;
    }
    final List<String> tags = new ArrayList<>();
    new KubernetesListBuilder()
      .withItems(items)
      .accept(new Decorator<SourceBuildStrategyFluent>() {
          @Override
          public void visit(SourceBuildStrategyFluent strategy) {
            ObjectReference from = strategy.buildFrom();
            if (from.getKind().equals("ImageStreamTag")) {
              tags.add(from.getName());
            }
          }
        }).build();

    boolean tagsMissing = true;
    long started = System.currentTimeMillis();
    long elapsed = 0;

    while (tagsMissing && elapsed < timeUnit.toMillis(amount) && !Thread.interrupted()) {
      tagsMissing = false;
      for (String tag : tags) {
        ImageStreamTag t = client.imageStreamTags().withName(tag).get();
        if (t == null) {
          tagsMissing = true;
        }
      }

      if (tagsMissing) {
        try {
          Thread.sleep(1000);
          elapsed = System.currentTimeMillis() - started;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
    return !tagsMissing;
  }
}
