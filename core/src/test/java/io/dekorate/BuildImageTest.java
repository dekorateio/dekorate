
package io.dekorate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class BuildImageTest {

  @Test
  public void shouldReturnMavenJdk8() throws Exception {
    Optional<BuildImage> image = BuildImage.find("maven", "3.6.3", 8, null);
    assertTrue(image.isPresent());
    assertEquals("docker.io/maven:3.6.3-jdk-8", image.get().getImage());
  }

  @Test
  public void shouldReturnMavenJdk11() throws Exception {
    Optional<BuildImage> image = BuildImage.find("maven", "3.6.3", 11, null);
    assertTrue(image.isPresent());
    assertEquals("docker.io/maven:3.6.3-jdk-11", image.get().getImage());
  }
}

