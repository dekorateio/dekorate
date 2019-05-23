package io.ap4k.crd.codegen;

import io.ap4k.deps.kubernetes.api.KubernetesResourceMappingProvider;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;
import io.ap4k.deps.kubernetes.client.Config;
import io.ap4k.deps.kubernetes.client.ResourceHandler;
import io.ap4k.deps.kubernetes.client.dsl.Resource;
import io.ap4k.deps.kubernetes.client.dsl.base.HasMetadataOperation;
import io.ap4k.deps.okhttp3.OkHttpClient;
import io.sundr.codegen.functions.ClassTo;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.TypeDef;

import java.util.Map;

public class ClientClasses {

  public static TypeDef HAS_METADATA = ClassTo.TYPEDEF.apply(HasMetadata.class);
  public static ClassRef HAS_METADATA_REF = HAS_METADATA.toReference();

  public static TypeDef OBJECT_META = ClassTo.TYPEDEF.apply(ObjectMeta.class);
  public static ClassRef OBJECT_META_REF = OBJECT_META.toReference();

  public static TypeDef RESOURCE = ClassTo.TYPEDEF.apply(Resource.class);
  public static TypeDef RESOURCE_HANDLER = ClassTo.TYPEDEF.apply(ResourceHandler.class);
  public static TypeDef KUBERNETES_RESOURCE_MAPPING_PROVIDER = ClassTo.TYPEDEF.apply(KubernetesResourceMappingProvider.class);
  public static  TypeDef KUBERNETES_RESOURCE = ClassTo.TYPEDEF.apply(KubernetesResource.class);

  public static TypeDef HASMETADATA_OPERATION = ClassTo.TYPEDEF.apply(HasMetadataOperation.class);

  public static TypeDef OKHTTP_CLIENT = ClassTo.TYPEDEF.apply(OkHttpClient.class);
  public static TypeDef CONFIG = ClassTo.TYPEDEF.apply(Config.class);


  TypeDef MAP = ClassTo.TYPEDEF.apply(Map.class);
}
