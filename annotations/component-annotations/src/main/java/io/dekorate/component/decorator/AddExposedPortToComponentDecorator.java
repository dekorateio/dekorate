package io.dekorate.component.decorator;

import io.dekorate.component.model.ComponentSpecBuilder;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddExposedPortToComponentDecorator extends Decorator<ComponentSpecBuilder> {

    private final int port;

    public AddExposedPortToComponentDecorator(int port) {
        this.port = port;
    }

    @Override
    public void visit(ComponentSpecBuilder componentSpecBuilder) {
        componentSpecBuilder.withPort(port);
    }
}
