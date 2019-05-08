package io.ap4k.kubernetes.decorator;

import io.ap4k.Resources;
import io.ap4k.kubernetes.decorator.ApplyImageDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApplyRegistryToImageDecoratorTest {

  @Test
  public void shouldRespectAfter() {
    Resources resources = new Resources();
    resources.setGroup("test");
    resources.setName("image");
    resources.setVersion("latest");
    ApplyRegistryToImageDecorator r = new ApplyRegistryToImageDecorator(resources, "docker.io");
    ApplyImageDecorator a = new ApplyImageDecorator("cnt", "image");

    assertEquals(1, r.compareTo(a));
    assertEquals(-1, a.compareTo(r));
  }

}
