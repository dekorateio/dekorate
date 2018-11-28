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
 * 
**/
package io.ap4k.utils;

import io.ap4k.deps.jackson.core.JsonProcessingException;
import io.ap4k.deps.jackson.core.type.TypeReference;
import io.ap4k.deps.jackson.databind.ObjectMapper;
import io.ap4k.deps.jackson.databind.SerializationFeature;
import io.ap4k.deps.jackson.dataformat.yaml.YAMLFactory;
import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.model.KubernetesResource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Serialization {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper() {{
    configure(SerializationFeature.INDENT_OUTPUT, true);
    configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
  }};
  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())  {{
    configure(SerializationFeature.INDENT_OUTPUT, true);
    configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
  }};

  private static final String DOCUMENT_DELIMITER = "---";

  public static ObjectMapper jsonMapper() {
    return JSON_MAPPER;
  }

  public static ObjectMapper yamlMapper() {
    return YAML_MAPPER;
  }

  public static <T> String asJson(T object) {
    try {
      return JSON_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  public static <T> String asYaml(T object) {
    try {
      return YAML_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a stream.
   * @param is    The {@link InputStream}.
   * @param <T>   The target type.
   * @return
   */
  public static <T> T unmarshal(InputStream is)  {
    return unmarshal(is, JSON_MAPPER);
  }

  /**
   * Unmarshals a stream.
   * @param is      The {@link InputStream}.
   * @param mapper  The {@link ObjectMapper} to use.
   * @param <T>     The target type.
   * @return
   */
  public static <T> T unmarshal(InputStream is, ObjectMapper mapper) {
    return unmarshal(is, mapper, Collections.emptyMap());
  }

  /**
   * Unmarshals a stream optionally performing placeholder substitution to the stream.
   * @param is          The {@link InputStream}.
   * @param mapper      The {@link ObjectMapper} to use.
   * @param parameters  A {@link Map} with parameters for placeholder substitution.
   * @param <T>         The target type.
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
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals a {@link String} optionally performing placeholder substitution to the String.
   * @param str   The {@link String}.
   * @param type  The target type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(String str, final Class<T> type)  {
    try (InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
      return unmarshal(is, new TypeReference<T>() {
          @Override
          public Type getType() {
            return type;
          }
        });
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  /**
   * Unmarshals an {@link InputStream} optionally performing placeholder substitution to the stream.
   * @param is              The {@link InputStream}.
   * @param type            The type.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(InputStream is, final Class<T> type)  {
    return unmarshal(is, new TypeReference<T>() {
        @Override
        public Type getType() {
          return type;
        }
      } );
  }



  /**
   * Unmarshals an {@link InputStream} optionally performing placeholder substitution to the stream.
   *
   * @param is            The {@link InputStream}.
   * @param type          The {@link TypeReference}.
   * @param <T>
   * @return
   */
  public static <T> T unmarshal(InputStream is, TypeReference<T> type)  {
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
      throw Ap4kException.launderThrowable(e);
    }
  }


  private static List<KubernetesResource> getKubernetesResourceList(String specFile) {
    List<KubernetesResource> documentList = new ArrayList<>();
    String[] documents = splitSpecFile(specFile);
    for (String document : documents) {
      if (validate(document)) {
        ByteArrayInputStream documentInputStream = new ByteArrayInputStream(document.getBytes());
        Object resource = Serialization.unmarshal(documentInputStream);
        documentList.add((KubernetesResource) resource);
      }
    }
    return documentList;
  }

  private static boolean containsMultipleDocuments(String specFile) {
    String[] documents = splitSpecFile(specFile);
    int nValidDocuments = 0;
    for(String document : documents) {
      if(validate(document))
        nValidDocuments++;
    }

    return nValidDocuments > 1;
  }

  private static String[] splitSpecFile(String aSpecFile) {
    List<String> documents = new ArrayList<>();
    String[] lines = aSpecFile.split(System.lineSeparator());
    int nLine = 0;
    StringBuilder builder = new StringBuilder();

    while(nLine < lines.length) {
      if((lines[nLine].length() >= DOCUMENT_DELIMITER.length()
          && !lines[nLine].substring(0, DOCUMENT_DELIMITER.length()).equals(DOCUMENT_DELIMITER)) || (lines[nLine].length() < DOCUMENT_DELIMITER.length())) {
        builder.append(lines[nLine] + System.lineSeparator());
      } else {
        documents.add(builder.toString());
        builder.setLength(0);
      }

      nLine++;
    }

    if(!builder.toString().isEmpty())
      documents.add(builder.toString());
    return documents.toArray(new String[documents.size()]);
  }

  private static boolean validate(String document) {
    Matcher keyValueMatcher = Pattern.compile("(\\S+):\\s(\\S*)(?:\\b(?!:)|$)").matcher(document);
    return !document.isEmpty() && keyValueMatcher.find();
  }

  private static String readSpecFileFromInputStream(InputStream inputStream) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    try {
      while ((length = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }
      return outputStream.toString();
    } catch (IOException e) {
      throw new RuntimeException("Unable to read InputStream." + e);
    }
  }

}
