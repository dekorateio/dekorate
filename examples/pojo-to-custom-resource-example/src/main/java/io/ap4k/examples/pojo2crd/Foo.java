package io.ap4k.examples.pojo2crd;

import io.ap4k.crd.annotation.CustomResource;
import io.ap4k.crd.confg.Scope;

@CustomResource(group = "ap4k.io", version = "v1", scope = Scope.Namespaced)
public class Foo {
  int size;
  boolean enabled;
  private Bar bar;
  private Baz[] bazes;
  private String[] tags;
}
