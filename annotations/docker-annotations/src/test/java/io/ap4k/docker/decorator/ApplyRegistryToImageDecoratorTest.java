package io.ap4k.docker.decorator;

import io.ap4k.kubernetes.decorator.ApplyImageDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApplyRegistryToImageDecoratorTest {

  @Test
  public void shouldRespectAfter() {
    ApplyRegistryToImageDecorator r = new ApplyRegistryToImageDecorator("docker.io", "test","image", "latest");
    ApplyImageDecorator a = new ApplyImageDecorator("cnt", "image");

    assertEquals(1, r.compareTo(a));
    assertEquals(-1, a.compareTo(r));
  }

}
