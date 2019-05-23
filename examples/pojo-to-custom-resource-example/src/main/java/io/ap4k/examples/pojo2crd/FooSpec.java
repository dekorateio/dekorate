package io.ap4k.examples.pojo2crd;

import io.ap4k.crd.annotation.CustomResource;
import io.ap4k.crd.confg.Scope;

import java.util.List;

@CustomResource(group = "ap4k.io", version = "v1", kind = "Foo", scope = Scope.Namespaced)
public class FooSpec {
  int size;
  boolean enabled;
  private Bar bar;
  private List<Baz> bazes;
  private String[] tags;
}
