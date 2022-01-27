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
package io.dekorate.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import io.dekorate.DekorateException;
import io.dekorate.utils.serialization.Features;
import io.dekorate.utils.serialization.SerializationFeatures;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.KubernetesResource;

public class Serialization {

  private static final String MINIMIZE_QUOTES = "MINIMIZE_QUOTES";
  private static final String ALWAYS_QUOTE_NUMBERS_AS_STRINGS = "ALWAYS_QUOTE_NUMBERS_AS_STRINGS";
  private static final String INDENT_ARRAYS_WITH_INDICATOR = "INDENT_ARRAYS_WITH_INDICATOR";

  private static final String INDENT_OUTPUT = "INDENT_OUTPUT";
  private static final String WRITE_NULL_MAP_VALUES = "WRITE_NULL_MAP_VALUES";
  private static final String WRITE_EMPTY_JSON_ARRAYS = "WRITE_EMPTY_JSON_ARRAYS";

  public static YAMLFactory createYamlFactory(String[] features) {
    YAMLFactory result = new YAMLFactory();
    for (String name : features) {
      Optional<Feature> feature = Features.find(name);
      if (feature.isPresent()) {
        result = result.enable(feature.get());
      }
    }
    return result;
  }

  public static ObjectMapper createYamlMapper(String[] generatorFeatures, String[] enabledFeatures, String[] disabledFeatures) {
    return new ObjectMapper(createYamlFactory(generatorFeatures)) {
      {

        for (String name : enabledFeatures) {
          Optional<SerializationFeature> feature = SerializationFeatures.find(name);
          if (feature.isPresent()) {
            configure(feature.get(), true);
          }
        }
        for (String name : disabledFeatures) {
          Optional<SerializationFeature> feature = SerializationFeatures.find(name);
          if (feature.isPresent()) {
            configure(feature.get(), false);
          }
        }

      }
    };
  }

  public static ObjectMapper createJsonMapper(String[] enabledFeatures, String[] disabledFeatures) {
    return new ObjectMapper() {
      {

        for (String name : enabledFeatures) {
          Optional<SerializationFeature> feature = SerializationFeatures.find(name);
          if (feature.isPresent()) {
            configure(feature.get(), true);
          }
        }
        for (String name : disabledFeatures) {
          Optional<SerializationFeature> feature = SerializationFeatures.find(name);
          if (feature.isPresent()) {
            configure(feature.get(), false);
          }
        }

      }
    };
  }

  private static final ObjectMapper JSON_MAPPER = createJsonMapper(new String[] { INDENT_OUTPUT },
      new String[] { WRITE_NULL_MAP_VALUES, WRITE_EMPTY_JSON_ARRAYS });

  private static final ObjectMapper YAML_MAPPER = createYamlMapper(
      new String[] { MINIMIZE_QUOTES, ALWAYS_QUOTE_NUMBERS_AS_STRINGS, INDENT_ARRAYS_WITH_INDICATOR },
      new String[] { INDENT_OUTPUT },
      new String[] { WRITE_NULL_MAP_VALUES, WRITE_EMPTY_JSON_ARRAYS });

  private static final JavaPropsMapper PROPERTIES_MAPPER = new JavaPropsMapper();

  private static final String DOCUMENT_DELIMITER = "---";

  public static ObjectMapper jsonMapper() {
    return JSON_MAPPER;
  }

  public static ObjectMapper yamlMapper() {
    return YAML_MAPPER;
  }

  public static JavaPropsMapper propertiesMapper() {
    return PROPERTIES_MAPPER;
  }

  public static <T> String asJson(T object) {
    try {
      if (object instanceof KubernetesList) {
        KubernetesList list = (KubernetesList) object;
        return list.getItems().stream()
            .map(Serialization::writeValueAsJsonSafe)
            .collect(Collectors.joining(",", "[", "]"));
      }
      return JSON_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  public static <T> String asYaml(T object) {
    try {
      if (object instanceof KubernetesList) {
        KubernetesList list = (KubernetesList) object;
        if (list.getItems().size() == 1) {
          return YAML_MAPPER.writeValueAsString(list.getItems().get(0));
        }

        return list.getItems().stream()
            .map(Serialization::writeValueAsYamlSafe)
            .collect(Collectors.joining());
      }
      return YAML_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a stream.
   *
   * @param is The {@link InputStream}.
   * @return
   */
  public static KubernetesList unmarshalAsList(InputStream is) {
    String content = Strings.read(is);
    String[] parts = splitDocument(content);
    List<HasMetadata> items = new ArrayList<>();
    for (String part : parts) {
      if (part.trim().isEmpty()) {
        continue;
      }
      Object resource = unmarshal(part);
      if (resource instanceof KubernetesList) {
        items.addAll(((KubernetesList) resource).getItems());
      } else if (resource instanceof HasMetadata) {
        items.add((HasMetadata) resource);
      } else if (resource instanceof HasMetadata[]) {
        Arrays.stream((HasMetadata[]) resource).forEach(r -> items.add(r));
      }
    }
    return new KubernetesListBuilder().withItems(items).build();
  }

  /**
   * Unmarshals a stream.
   *
   * @param is The {@link InputStream}.
   * @param <T> The target type.
   * @return
   */
  public static <T> T unmarshal(InputStream is) {
    return unmarshal(is, JSON_MAPPER);
  }

  /**
   * Unmarshals a stream.
   *
   * @param is The {@link InputStream}.
   * @param mapper The {@link ObjectMapper} to use.
   * @param <T> The target type.
   * @return
   */
  public static <T> T unmarshal(InputStream is, ObjectMapper mapper) {
    return unmarshal(is, mapper, Collections.emptyMap());
  }

  /**
   * Unmarshals a stream optionally performing placeholder substitution to the stream.
   *
   * @param is The {@link InputStream}.
   * @param mapper The {@link ObjectMapper} to use.
   * @param parameters A {@link Map} with parameters for placeholder substitution.
   * @param <T> The target type.
   * @return
   */
  public static <T> T unmarshal(InputStream is, ObjectMapper mapper, Map<String, String> parameters) {
    try (BufferedInputStream bis = new BufferedInputStream(is)) {
      bis.mark(-1);
      int intch;
      do {
        intch = bis.read();
      } while (intch > -1 && Character.isWhitespace(intch));
      bis.reset();

      if (intch != '{') {
        mapper = YAML_MAPPER;
      }
      return mapper.readerFor(KubernetesResource.class).readValue(bis);
    } catch (Exception e) {
      e.printStackTrace();
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a {@link String} optionally performing placeholder substitution to the String.
   *
   * @param str The {@link String}.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(String str) {
    try (InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
      return unmarshal(is);
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a {@link String} optionally performing placeholder substitution to the String.
   *
   * @param str The {@link String}.
   * @param type The target type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(String str, final Class<T> type) {
    try (InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
      return unmarshal(is, new TypeReference<T>() {
        @Override
        public Type getType() {
          return type;
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals an {@link InputStream} optionally performing placeholder substitution to the stream.
   *
   * @param is The {@link InputStream}.
   * @param type The type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(InputStream is, final Class<T> type) {
    return unmarshal(is, new TypeReference<T>() {
      @Override
      public Type getType() {
        return type;
      }
    });
  }

  /**
   * Unmarshals a {@link File} optionally performing placeholder substitution to the stream.
   *
   * @param f The {@link File}.
   * @param type The type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(File f, final Class<T> type) {
    try (FileInputStream is = new FileInputStream(f)) {
      return unmarshal(is, new TypeReference<T>() {
        @Override
        public Type getType() {
          return type;
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a {@link URL} optionally performing placeholder substitution to the stream.
   *
   * @param u The {@link URL}.
   * @param type The type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(URL u, final Class<T> type) {
    try (InputStream is = u.openStream()) {
      return unmarshal(is, new TypeReference<T>() {
        @Override
        public Type getType() {
          return type;
        }
      });
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals an {@link InputStream} optionally performing placeholder substitution to the stream.
   *
   * @param is The {@link InputStream}.
   * @param type The {@link TypeReference}.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(InputStream is, TypeReference<T> type) {
    try (BufferedInputStream bis = new BufferedInputStream(is)) {
      bis.mark(-1);
      int intch;
      do {
        intch = bis.read();
      } while (intch > -1 && Character.isWhitespace(intch));
      bis.reset();

      ObjectMapper mapper = JSON_MAPPER;
      if (intch != '{') {
        mapper = YAML_MAPPER;
      }
      return mapper.readValue(bis, type);
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  private static String[] splitDocument(String aSpecFile) {
    List<String> documents = new ArrayList<>();
    String[] lines = aSpecFile.split("\\r?\\n");
    int nLine = 0;
    StringBuilder builder = new StringBuilder();

    while (nLine < lines.length) {
      if ((lines[nLine].length() >= DOCUMENT_DELIMITER.length()
          && !lines[nLine].substring(0, DOCUMENT_DELIMITER.length()).equals(DOCUMENT_DELIMITER))
          || (lines[nLine].length() < DOCUMENT_DELIMITER.length())) {
        builder.append(lines[nLine] + System.lineSeparator());
      } else {
        documents.add(builder.toString());
        builder.setLength(0);
        //To have meaningfull line numbers, in jackson error messages, we need each resource
        //to retain its original position in the document.
        for (int i = 0; i <= nLine; i++) {
          builder.append(System.lineSeparator());
        }
      }
      nLine++;
    }

    if (!builder.toString().isEmpty())
      documents.add(builder.toString());
    return documents.toArray(new String[documents.size()]);
  }

  private static <T> String writeValueAsYamlSafe(T object) {
    try {
      return YAML_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  private static <T> String writeValueAsJsonSafe(T object) {
    try {
      return JSON_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
}
