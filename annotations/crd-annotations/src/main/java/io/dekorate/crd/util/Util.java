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
package io.dekorate.crd.util;

import io.dekorate.crd.confg.codegen.ClientClasses;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.model.TypeRef;
import io.sundr.codegen.utils.TypeUtils;

public class Util {
  /**
   * Checks if the specified class is an actual Kubernetes Resource.
   *
   * @param typeDef The candidate.
   * @return True if implements HasMetadata, false otherwise.
   */
  public static boolean isKubernetesResource(TypeDef typeDef) {
    if (!typeDef.getImplementsList().stream().anyMatch(c -> matches(ClientClasses.HAS_METADATA_REF, c))) {
      return false;
    }
    if (!TypeUtils.allProperties(typeDef).stream().anyMatch(p -> p.getName().equals("metadata") && matches(ClientClasses.HAS_METADATA_REF, p.getTypeRef()))) {
      return false;
    }
    return true;
  }

  /**
   * Checks if candidate is equal to classRef or its its unshadowed counterpart.
   *
   * @param classRef  The class ref.
   * @param candidate The candidate ref.
   * @return If classes match
   */
  private static boolean matches(ClassRef classRef, TypeRef candidate) {
    if (candidate instanceof ClassRef) {
      return false;
    }
    ClassRef candidateClass = (ClassRef) candidate;
    if (!candidateClass.getName().equals(classRef.getName())) {
      return false;
    }
    if (candidateClass.getFullyQualifiedName().equals(classRef.getFullyQualifiedName())) {
      return true;
    }

    if (candidateClass.getFullyQualifiedName().replaceAll("io.fabric8.kubernetes", "io.ap4k.deps.kubernetes").equals(classRef.getFullyQualifiedName())) {
      return true;
    }
    return false;
  }
}
