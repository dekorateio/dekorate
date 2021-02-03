/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.crd.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.fabric8.kubernetes.api.model.Duration;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.JSONSchemaProps;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.JSONSchemaPropsBuilder;
import io.fabric8.kubernetes.client.CustomResource;
import io.sundr.builder.internal.functions.TypeAs;
import io.sundr.codegen.functions.ClassTo;
import io.sundr.codegen.model.ClassRef;
import io.sundr.codegen.model.PrimitiveRefBuilder;
import io.sundr.codegen.model.Property;
import io.sundr.codegen.model.TypeDef;
import io.sundr.codegen.model.TypeRef;
import io.sundr.codegen.utils.TypeUtils;

public class JsonSchema {

  private static final TypeDef CUSTOM_RESOURCE = ClassTo.TYPEDEF.apply(CustomResource.class);
  private static final TypeDef QUANTITY = ClassTo.TYPEDEF.apply(Quantity.class);
  private static final TypeDef DURATION = ClassTo.TYPEDEF.apply(Duration.class);
  private static final TypeDef INT_OR_STRING = ClassTo.TYPEDEF.apply(IntOrString.class);

  private static final TypeDef BOOLEAN = ClassTo.TYPEDEF.apply(Boolean.class);
  private static final TypeDef STRING = ClassTo.TYPEDEF.apply(String.class);
  private static final TypeDef INT = ClassTo.TYPEDEF.apply(Integer.class);
  private static final TypeDef LONG = ClassTo.TYPEDEF.apply(Long.class);
  private static final TypeDef DOUBLE = ClassTo.TYPEDEF.apply(Double.class);
  private static final TypeDef DATE = ClassTo.TYPEDEF.apply(Date.class);
  private static final TypeDef OPTIONAL = ClassTo.TYPEDEF.apply(Optional.class);

  private static final TypeRef BOOLEAN_REF = BOOLEAN.toReference();
  private static final TypeRef STRING_REF = STRING.toReference();
  private static final TypeRef INT_REF = INT.toReference();
  private static final TypeRef LONG_REF = LONG.toReference();
  private static final TypeRef DOUBLE_REF = DOUBLE.toReference();
  private static final TypeRef DATE_REF = DATE.toReference();
  private static final TypeRef QUANTITY_REF = QUANTITY.toReference();
  private static final TypeRef DURATION_REF = DURATION.toReference();
  private static final TypeRef INT_OR_STRING_REF = INT_OR_STRING.toReference();

  private static final TypeRef P_INT_REF = new PrimitiveRefBuilder().withName("int").build();
  private static final TypeRef P_LONG_REF = new PrimitiveRefBuilder().withName("long").build();
  private static final TypeRef P_DOUBLE_REF = new PrimitiveRefBuilder().withName("double").build();
  private static final TypeRef P_BOOLEAN_REF = new PrimitiveRefBuilder().withName("boolean").build();
  private static final TypeRef OPTIONAL_REF = OPTIONAL.toReference();

  public static final JSONSchemaProps JSON_SCHEMA_STRING = new JSONSchemaPropsBuilder().withType("string").build();
  public static final JSONSchemaProps JSON_SCHEMA_INTEGER = new JSONSchemaPropsBuilder().withType("integer").build();
  public static final JSONSchemaProps JSON_SCHEMA_NUMBER = new JSONSchemaPropsBuilder().withType("number").build();
  public static final JSONSchemaProps JSON_SCHEMA_BOOLEAN = new JSONSchemaPropsBuilder().withType("boolean").build();
  public static final JSONSchemaProps JSON_SCHEMA_OBJECT = new JSONSchemaPropsBuilder().withType("object").build();
  public static final JSONSchemaProps JSON_SCHEMA_INT_OR_STRING = new JSONSchemaPropsBuilder().withAnyOf(JSON_SCHEMA_INTEGER, JSON_SCHEMA_STRING).build();

  public static final Map<TypeRef, JSONSchemaProps> TYPE_MAP = new HashMap<TypeRef, JSONSchemaProps>() {
    {
      put(STRING_REF, JSON_SCHEMA_STRING);
      put(DATE_REF, JSON_SCHEMA_STRING);
      put(INT_REF, JSON_SCHEMA_INTEGER);
      put(P_INT_REF, JSON_SCHEMA_INTEGER);
      put(LONG_REF, JSON_SCHEMA_NUMBER);
      put(P_LONG_REF, JSON_SCHEMA_NUMBER);
      put(DOUBLE_REF, JSON_SCHEMA_NUMBER);
      put(P_DOUBLE_REF, JSON_SCHEMA_NUMBER);
      put(BOOLEAN_REF, JSON_SCHEMA_BOOLEAN);
      put(P_BOOLEAN_REF, JSON_SCHEMA_BOOLEAN);

      put(QUANTITY_REF, JSON_SCHEMA_INT_OR_STRING);
      put(INT_OR_STRING_REF, JSON_SCHEMA_INT_OR_STRING);
      put(DURATION_REF, JSON_SCHEMA_STRING);
    }
  };

  /**
   * Creates the JSON schema for the particular {@TypeDef}.
   * 
   * @param definition The definition.
   * @return The schema.
   */
  public static JSONSchemaProps from(TypeDef definition, String ... ignore) {
    JSONSchemaPropsBuilder builder = new JSONSchemaPropsBuilder();
    builder.withType("object");
    Set<String> ignores  = ignore.length > 0 ? new LinkedHashSet<>(Arrays.asList(ignore)) : Collections.emptySet();
    List<String> required = new ArrayList<>();


    for (Property property : Types.allProperties(definition)) {
      if (property.isStatic()) {
        continue;
      }

      if (ignores.contains(property.getName())) {
        continue;
      }

      JSONSchemaProps schema = from(property.getTypeRef());
      if (property.getAnnotations()
          .stream()
          .anyMatch(a -> a.getClassRef().getFullyQualifiedName().equals("javax.validation.constraints.NotNull"))) {
        required.add(property.getName());
      }

      if (schema != null) {
        builder.addToProperties(property.getName(), from(property.getTypeRef()));
      }
    }
    builder.withRequired(required);
    return builder.build();
  }

  public static JSONSchemaProps from(TypeRef typeRef) {
    //1. Handle Collections and Arrays
    if (typeRef.getDimensions() > 0 || TypeUtils.isCollection(typeRef)) {
      return new JSONSchemaPropsBuilder()
          .withType("array")
          .withNewItems()
          .withSchema(from(TypeAs.combine(TypeAs.UNWRAP_ARRAY_OF, TypeAs.UNWRAP_COLLECTION_OF).apply(typeRef)))
          .and()
          .build();
      //2. Handle Standard Types
    } else if (TYPE_MAP.containsKey(typeRef)) {
      return TYPE_MAP.get(typeRef);
      //3. Handle Optionals
    } else if (TypeUtils.isOptional(typeRef)) {
      return from(TypeAs.UNWRAP_OPTIONAL_OF.apply(typeRef));
      //4. Handle complex types
    } else if (typeRef instanceof ClassRef) {
      ClassRef classRef = (ClassRef) typeRef;
      TypeDef def = classRef.getDefinition();
      return from(def);
    }
    return null;
  }
}
