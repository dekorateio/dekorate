package io.ap4k.component.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.component.config.CompositeConfig;
import io.ap4k.component.config.EditableCompositeConfig;
import io.ap4k.component.config.EditableLinkConfig;
import io.ap4k.component.config.LinkConfig;
import io.ap4k.component.model.*;
import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.utils.Strings;

public class LinkHandler implements Handler<LinkConfig> {

  private static final String LINK = "link";
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);

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
    resources.addCustom(LINK, createLink(config));
//    addVisitors(config);
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(LinkConfig.class) ||
      type.equals(EditableLinkConfig.class);
  }

  private void addVisitors(LinkConfig config) {
//    String type = config.getAttribute(RUNTIME_TYPE);
//    String version = config.getAttribute(RUNTIME_VERSION);
//
//    if (type != null) {
//      resources.decorateCustom(LINK,new AddRuntimeTypeToComponentDecorator(type));
//    }
//
//    if (version != null) {
//      resources.decorateCustom(LINK,new AddRuntimeVersionToComponentDecorator(version));
//    }
//    for (Env env : config.getEnvVars()) {
//      resources.decorateCustom(LINK, new AddEnvToComponentDecorator(env));
//    }
////    for (Link link : config.getLinks()) {
////      resources.decorateCustom(LINK, new AddLinkToComponentDecorator(link));
////    }
  }

  /**
   * Create a {@link Component} from a {@link CompositeConfig}.
   * @param config  The config.
   * @return        The component.
   */
  private Link createLink(LinkConfig config) {
    return new LinkBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withName(config.getName())
      .withKind(config.getKind())
      .withNewRef(config.getRef())
      .endSpec()
      .build();
  }
}
