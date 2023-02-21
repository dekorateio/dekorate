package io.dekorate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.dekorate.LoggerFactory;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.logger.NoopLogger;

public class TopologicalSortTest {

  private List<Decorator> sortedDecorators;

  @BeforeAll
  public static void initLogger() {
    LoggerFactory.setLogger(new NoopLogger());
  }

  @BeforeEach
  public void setup() {
    sortedDecorators = null;
  }

  @Test
  public void shouldGroupDecoratorsWithSameTypeAndShouldPreserveOrder() {
    FooDecorator a1 = new FooDecorator();
    DummyDecorator b = new DummyDecorator().withAfter(FooDecorator.class);
    FooDecorator a2 = new FooDecorator();

    whenSort(b, a1, a2);
    thenDecoratorsAre(a1, a2, b);
  }

  @Test
  public void shouldThrowCycleException() {
    FooDecorator foo = new FooDecorator();
    DummyDecorator bar = new BarDecorator().withAfter(FooDecorator.class);
    DummyDecorator special = new SpecialFooDecorator().withAfter(FooDecorator.class, BarDecorator.class);

    // it should fail because:
    // bar -> foo
    // special (it's also a foo) -> bar
    assertThrows(RuntimeException.class, () -> whenSort(foo, bar, special));
  }

  @Test
  public void shouldAvoidCycleErrorWhenDecoratorHasSameParent() {
    FooDecorator foo = new FooDecorator();
    DummyDecorator bar = new BarDecorator().withBefore(SpecialFooDecorator.class).withAfter(FooDecorator.class);
    DummyDecorator special = new SpecialFooDecorator().withAfter(FooDecorator.class, BarDecorator.class);

    whenSort(foo, bar, special);
    thenDecoratorsAre(foo, bar, special);
  }

  private void whenSort(Decorator decorator, Decorator... decorators) {
    List<Decorator> unsortedDecorators = new ArrayList<>();
    unsortedDecorators.add(decorator);
    if (decorators != null) {
      for (Decorator rest : decorators) {
        unsortedDecorators.add(rest);
      }
    }

    sortedDecorators = TopologicalSort.sortDecorators(unsortedDecorators);
  }

  private void thenDecoratorsAre(Decorator... expectedDecorators) {
    for (int position = 0; position < expectedDecorators.length; position++) {
      Decorator expected = expectedDecorators[position];
      assertEquals(sortedDecorators.get(position), expected, "Unexpected order in decorators: " + sortedDecorators);
    }
  }

  static class FooDecorator extends DummyDecorator {

  }

  static class SpecialFooDecorator extends FooDecorator {

  }

  static class BarDecorator extends DummyDecorator {

  }

  static class DummyDecorator extends Decorator {

    private Class[] before;
    private Class[] after;

    public DummyDecorator withAfter(Class... classes) {
      after = classes;
      return this;
    }

    public DummyDecorator withBefore(Class... classes) {
      before = classes;
      return this;
    }

    @Override
    public void visit(Object o) {

    }

    @Override
    public int compareTo(Object o) {
      return 0;
    }

    @Override
    public Class<? extends Decorator>[] before() {
      return before;
    }

    @Override
    public Class<? extends Decorator>[] after() {
      return after;
    }
  }
}
