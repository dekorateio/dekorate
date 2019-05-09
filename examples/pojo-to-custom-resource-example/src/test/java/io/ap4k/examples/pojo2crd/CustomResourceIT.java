package io.ap4k.examples.pojo2crd;

import io.ap4k.testing.WithProject;
import io.ap4k.testing.annotation.KubernetesIntegrationTest;
import io.ap4k.utils.Exec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@KubernetesIntegrationTest(deployEnabled = false)
class CustomResourceIT implements WithProject {

  Path testResources = getProject().getRoot().resolve("src").resolve("test").resolve("resources");
  Path crdPath =getProject().getBuildInfo().getResourceOutputDir().resolve(getProject().getAp4kOutputDir()).resolve("kubernetes.yml");

  @Test
  void shouldApplyFoo() {
    assertTrue(Exec.inProject(getProject()).commands("oc", "apply", "-f", crdPath.toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "create", "-f", testResources.resolve("test-foo.yml").toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "delete", "-f", testResources.resolve("test-foo.yml").toAbsolutePath().toString()));
    assertTrue(Exec.inProject(getProject()).commands("oc", "delete", "-f", crdPath.toAbsolutePath().toString()));
  }
}
