/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.crd.confg.codegen;

import java.util.Map;

import io.fabric8.kubernetes.api.KubernetesResourceMappingProvider;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.ResourceHandler;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.HasMetadataOperation;
import io.sundr.codegen.functions.ClassTo;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.TypeDef;
import okhttp3.OkHttpClient;

public class ClientClasses {
  public static TypeDef HAS_METADATA = ClassTo.TYPEDEF.apply(HasMetadata.class);
  public static ClassRef HAS_METADATA_REF = HAS_METADATA.toReference();

  public static TypeDef OBJECT_META = ClassTo.TYPEDEF.apply(ObjectMeta.class);
  public static ClassRef OBJECT_META_REF = OBJECT_META.toReference();

  public static TypeDef RESOURCE = ClassTo.TYPEDEF.apply(Resource.class);
  public static TypeDef RESOURCE_HANDLER = ClassTo.TYPEDEF.apply(ResourceHandler.class);
  public static TypeDef KUBERNETES_RESOURCE_MAPPING_PROVIDER = ClassTo.TYPEDEF.apply(KubernetesResourceMappingProvider.class);
  public static TypeDef KUBERNETES_RESOURCE = ClassTo.TYPEDEF.apply(KubernetesResource.class);

  public static TypeDef HASMETADATA_OPERATION = ClassTo.TYPEDEF.apply(HasMetadataOperation.class);

  public static TypeDef OKHTTP_CLIENT = ClassTo.TYPEDEF.apply(OkHttpClient.class);
  public static TypeDef CONFIG = ClassTo.TYPEDEF.apply(Config.class);


  TypeDef MAP = ClassTo.TYPEDEF.apply(Map.class);
}
