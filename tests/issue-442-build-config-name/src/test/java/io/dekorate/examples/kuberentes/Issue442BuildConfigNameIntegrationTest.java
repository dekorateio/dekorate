package io.dekorate.examples.kubernetes;

import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;

@OpenshiftIntegrationTest
public class Issue442BuildConfigNameIntegrationTest {

  @Test
  void shouldBeUpAndRunning() {
    //We just want to check that the image is build. An empty test is good enough for this.
  }
}
