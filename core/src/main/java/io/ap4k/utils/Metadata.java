package io.ap4k.utils;

import io.ap4k.deps.kubernetes.api.builder.Builder;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class Metadata {

  public static Optional<ObjectMeta> getMetadata(Builder builder) {
    try {
      Method method = builder.getClass().getMethod("buildMetadata");
      Object o = method.invoke(builder);
      if (o instanceof ObjectMeta) {
        return Optional.of((ObjectMeta)o);
      }
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      //ignore
    }
    return Optional.empty();
  }
}
