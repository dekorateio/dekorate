package io.dekorate.servicebinding.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.dekorate.servicebinding.config.EditableServiceBindingConfig;
import io.dekorate.servicebinding.config.ServiceBindingConfig;

public class ServiceBindingHandlerTest {

  @Test
  public void shouldAcceptServiceBindingConfig() {
    ServiceBindingHandler handler = new ServiceBindingHandler();
    assertTrue(handler.canHandle(ServiceBindingConfig.class));
  }

  @Test
  public void shouldAcceptEditableServiceBindingConfig() {
    ServiceBindingHandler handler = new ServiceBindingHandler();
    assertTrue(handler.canHandle(EditableServiceBindingConfig.class));
  }

}
