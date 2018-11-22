package io.ap4k.openshift.utils;

import io.ap4k.deps.kubernetes.api.builder.Visitor;
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
    final List<String> tags = new ArrayList<>();
    new KubernetesListBuilder()
      .withItems(items)
      .accept(new Visitor<SourceBuildStrategyFluent>() {
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
