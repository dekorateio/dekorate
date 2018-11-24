package io.ap4k.config;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

public abstract class Configurator<T extends ConfigurationFluent> extends TypedVisitor<T> {
}
