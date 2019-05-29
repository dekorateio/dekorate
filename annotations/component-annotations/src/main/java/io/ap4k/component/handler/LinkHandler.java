package io.ap4k.component.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.component.config.EditableLinkConfig;
import io.ap4k.component.config.LinkConfig;
import io.ap4k.component.model.Link;
import io.ap4k.component.model.LinkBuilder;
import io.ap4k.component.model.LinkFluent;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.utils.Strings;

public class LinkHandler implements Handler<LinkConfig> {
  private final Resources resources;

  public LinkHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1100;
  }

  @Override
  public void handle(LinkConfig config) {
    if (Strings.isNullOrEmpty(resources.getName())) {
      resources.setName(config.getName());
    }
    resources.addCustom(ResourceGroup.NAME, createLink(config));
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(LinkConfig.class) ||
      type.equals(EditableLinkConfig.class);
  }

  /**
   * Create a {@link Link} from a {@link LinkConfig}.
   *
   * @param config The config.
   * @return The link.
   */
  private Link createLink(LinkConfig config) {
    final LinkFluent.SpecNested<LinkBuilder> linkSpec = new LinkBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withName(config.getName())
      .withKind(config.getKind())
      .withNewRef(config.getRef());
    for (Env env : config.getEnvVars()) {
      linkSpec.addNewEnv(env.getName(), env.getValue());
    }
    return linkSpec
      .endSpec()
      .build();
  }
}
