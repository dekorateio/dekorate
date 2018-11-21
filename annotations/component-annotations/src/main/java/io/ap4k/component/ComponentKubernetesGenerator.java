package io.ap4k.component;

import io.ap4k.Generator;
import io.ap4k.Resources;
import io.ap4k.component.model.Component;
import io.ap4k.component.model.ComponentBuilder;
import io.ap4k.component.model.DeploymentType;
import io.ap4k.component.visitor.AddEnvToComponent;
import io.ap4k.config.Configuration;
import io.ap4k.config.Env;
import io.ap4k.config.KubernetesConfig;
import io.ap4k.config.EditableKubernetesConfig;

public class ComponentKubernetesGenerator implements Generator<KubernetesConfig> {

  private static final String COMPONENT = "component";

  private final Resources resources;

  public ComponentKubernetesGenerator () {
    this(new Resources());
  }
  public ComponentKubernetesGenerator(Resources resources) {
    this.resources = resources;
  }


  @Override
  public void generate(KubernetesConfig config) {
    resources.addExplicit(COMPONENT, createComponent(config));
    addVisitors(config);
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(KubernetesConfig.class) ||
      type.equals(EditableKubernetesConfig.class);
  }

  /**
   * Create a {@link Component} from a {@link KubernetesConfig}.
   * @param config  The config.
   * @return        The component.
   */
  private Component createComponent(KubernetesConfig config) {
    return new ComponentBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withDeploymentMode(DeploymentType.innerloop)
      .endSpec()
      .build();
  }


  private void addVisitors(KubernetesConfig config) {
   for (Env env : config.getEnvVars()) {
     resources.accept(COMPONENT, new AddEnvToComponent(env));
   }
  }
}
