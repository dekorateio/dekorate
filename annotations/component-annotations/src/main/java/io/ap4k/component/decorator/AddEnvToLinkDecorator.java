/**
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.ap4k.component.decorator;

import io.ap4k.component.model.LinkSpecBuilder;
import io.ap4k.doc.Description;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.kubernetes.decorator.Decorator;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@Description("Add environment variable to link.")
public class AddEnvToLinkDecorator extends Decorator<LinkSpecBuilder> {
  private final Env env;

  public AddEnvToLinkDecorator(Env env) {
    this.env = env;
  }

  @Override
  public void visit(LinkSpecBuilder link) {
    link.addNewEnv()
      .withName(env.getName())
      .withValue(env.getValue())
      .endEnv();
  }
}
