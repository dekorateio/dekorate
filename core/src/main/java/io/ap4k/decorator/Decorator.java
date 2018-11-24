package io.ap4k.decorator;


import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

public abstract class Decorator<T> extends TypedVisitor<T> {
}
