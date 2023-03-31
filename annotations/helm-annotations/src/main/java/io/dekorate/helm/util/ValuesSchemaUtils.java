package io.dekorate.helm.util;

import static io.dekorate.helm.util.HelmConfigUtils.deductProperty;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;

import io.dekorate.ConfigReference;
import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.helm.model.ValuesSchema;
import io.dekorate.helm.model.ValuesSchemaProperty;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Strings;

public final class ValuesSchemaUtils {
  private ValuesSchemaUtils() {

  }

  public static Map<String, Object> createSchema(HelmChartConfig helmConfig,
      Map<String, ValuesHolder.HelmValueHolder> prodValues) {
    ValuesSchema schema = new ValuesSchema();
    schema.setTitle(helmConfig.getValuesSchema().getTitle());

    // from value references
    for (Map.Entry<String, ValuesHolder.HelmValueHolder> value : prodValues.entrySet()) {
      ConfigReference configReference = value.getValue().configReference;
      String[] tree = deductProperty(helmConfig, value.getKey()).split(Pattern.quote("."));
      ValuesSchemaProperty parent = null;
      Map<String, ValuesSchemaProperty> location = schema.getProperties();
      for (int index = 0; index < tree.length - 1; index++) {
        String part = tree[index];
        ValuesSchemaProperty next = location.get(part);
        if (next == null) {
          next = new ValuesSchemaProperty();
          next.setType("object");
          location.put(part, next);
        }

        parent = next;
        location = next.getProperties();
      }

      String propertyName = tree[tree.length - 1];
      Object propertyValue = value.getValue().value;

      ValuesSchemaProperty property = location.getOrDefault(propertyName, new ValuesSchemaProperty());
      property.setDescription(configReference.getDescription());
      property.setPattern(configReference.getPattern());
      property.setEnumValues(configReference.getEnumValues());
      property.setMaximum(configReference.getMaximum());
      property.setMinimum(configReference.getMinimum());
      if (configReference.isRequired()) {
        if (parent == null) {
          schema.getRequired().add(propertyName);
        } else {
          parent.getRequired().add(propertyName);
        }
      }
      if (propertyValue == null) {
        property.setType("null");
      } else if (propertyValue instanceof Integer) {
        property.setType("integer");
      } else if (propertyValue instanceof Number) {
        property.setType("number");
      } else if (propertyValue instanceof Collection) {
        property.setType("array");
      } else if (propertyValue instanceof Boolean) {
        property.setType("boolean");
      } else {
        property.setType("string");
      }

      location.put(propertyName, property);
    }

    // from properties
    for (io.dekorate.helm.config.ValuesSchemaProperty propertyFromConfig : helmConfig.getValuesSchema().getProperties()) {
      String[] tree = deductProperty(helmConfig, propertyFromConfig.getName()).split(Pattern.quote("."));
      ValuesSchemaProperty parent = null;
      Map<String, ValuesSchemaProperty> location = schema.getProperties();
      for (int index = 0; index < tree.length - 1; index++) {
        String part = tree[index];
        ValuesSchemaProperty next = location.get(part);
        if (next == null) {
          next = new ValuesSchemaProperty();
          next.setType("object");
          location.put(part, next);
        }

        parent = next;
        location = next.getProperties();
      }

      String propertyName = tree[tree.length - 1];
      ValuesSchemaProperty property = location.getOrDefault(propertyName, new ValuesSchemaProperty());
      if (Strings.isNotNullOrEmpty(propertyFromConfig.getDescription())) {
        property.setDescription(propertyFromConfig.getDescription());
      }

      if (Strings.isNotNullOrEmpty(propertyFromConfig.getPattern())) {
        property.setPattern(propertyFromConfig.getPattern());
      }

      if (propertyFromConfig.getMaximum() != Integer.MAX_VALUE) {
        property.setMaximum(propertyFromConfig.getMaximum());
      }

      if (propertyFromConfig.getMinimum() != Integer.MIN_VALUE) {
        property.setMinimum(propertyFromConfig.getMinimum());
      }

      if (propertyFromConfig.isRequired()) {
        if (parent == null) {
          schema.getRequired().add(propertyName);
        } else {
          parent.getRequired().add(propertyName);
        }
      }

      if (Strings.isNotNullOrEmpty(propertyFromConfig.getType())) {
        property.setType(propertyFromConfig.getType());
      }

      location.put(propertyName, property);
    }

    // convert to map
    return Serialization.unmarshal(Serialization.asJson(schema), new TypeReference<Map<String, Object>>() {
    });
  }
}
