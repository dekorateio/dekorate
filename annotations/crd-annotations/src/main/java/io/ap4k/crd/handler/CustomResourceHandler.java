package io.ap4k.crd.handler;

import io.ap4k.Ap4kException;
import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.crd.codegen.ClientClasses;
import io.ap4k.crd.confg.Keys;
import io.ap4k.crd.config.CustomResourceConfig;
import io.ap4k.crd.config.EditableCustomResourceConfig;
import io.ap4k.crd.util.JsonSchema;
import io.ap4k.deps.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.ap4k.deps.kubernetes.api.model.apiextensions.JSONSchemaProps;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.utils.Strings;
import io.sundr.codegen.functions.Sources;
import io.sundr.codegen.generator.CodeGeneratorBuilder;
import io.sundr.codegen.generator.CodeGeneratorContext;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.model.TypeDefBuilder;
import io.sundr.codegen.model.TypeRef;
import io.sundr.codegen.utils.TypeUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static io.ap4k.crd.util.Util.isKubernetesResource;

public class CustomResourceHandler implements Handler<CustomResourceConfig> {

  private final Resources resources;
  private final List<TypeDef> sources;

  private final CodeGeneratorContext context = new CodeGeneratorContext();

  public CustomResourceHandler(Resources resources, List<TypeDef> sources) {
    this.resources = resources;
    this.sources = sources;
  }

  @Override
  public int order() {
    return 400;
  }

  @Override
  public void handle(CustomResourceConfig config) {
    TypeDef typeDef = new TypeDefBuilder(config.getAttribute(Keys.TYPE_DEFINITION))
      .addToAttributes(Keys.CUSTOM_RESOURCE_CONFIG, config)
      .build();

    JSONSchemaProps schema = null;
    //Check if we need to also generate the Kubernetes resource.
    if (!isKubernetesResource(typeDef)) {
      if (Strings.isNullOrEmpty(config.getKind())) {
        throw new IllegalStateException("No kind has been specified and annotated class is not a Kubernetes resource (does not implement HasMetadata).");
      }

      sources.add(typeDef);
      schema = JsonSchema.newSpec(typeDef);
    } else {
      schema = JsonSchema.from(typeDef);
    }

    resources.add(new CustomResourceDefinitionBuilder()
      .withApiVersion("apiextensions.k8s.io/v1beta1")
      .withNewMetadata()
        .withName(config.getPlural() + "." + config.getGroup())
        .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withScope(config.getScope().name())
      .withGroup(config.getGroup())
      .withVersion(config.getVersion())
      .withNewNames()
        .withKind(config.getKind())
        .withShortNames(config.getShortName())
        .withPlural(config.getPlural())
        .withSingular(config.getKind().toLowerCase())
      .endNames()
      .withNewValidation()
        .withOpenAPIV3Schema(schema)
      .endValidation()
      .endSpec()
    .build());
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> config) {
    return CustomResourceConfig.class.equals(config) || EditableCustomResourceConfig.class.equals(config);
  }
}
