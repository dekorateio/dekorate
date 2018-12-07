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

package io.ap4k.project;

import io.ap4k.utils.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static io.ap4k.project.BuildInfo.DEFAULT_PACKAGING;

public class MavenInfoReader implements BuildInfoReader {

  private static final String ARTIFACT_ID = "artifactId";
  private static final String VERSION = "version";
  private static final String PACKAGING = "packaging";
  private static final String PARENT = "parent";
  private static final String POM_XML = "pom.xml";

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean isApplicable(Path root) {
    return root.resolve(POM_XML).toFile().exists();
  }

  @Override
  public BuildInfo getInfo(Path root) {
    Document document = parse(root.resolve(POM_XML));
    String name = getArtifactId(document);
    String version = getVersion(document);
    String packaging = getPackaging(document);
    return new BuildInfo(name, version, packaging, root.resolve(TARGET).resolve(String.format(OUTPUTFILE_FORMAT, name, version, packaging)));
  }


  protected static Document parse(Path pom) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(pom.toFile());
    } catch (IOException e) {
      throw new RuntimeException(("Failed to read: " + pom.toAbsolutePath()));
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(("Failed to parse: " + pom.toAbsolutePath()));
    } catch (SAXException e) {
      throw new RuntimeException(("Failed to parse: " + pom.toAbsolutePath()));
    }
  }

  /**
   * Read the artifactId from the document.
   * @param document      The document.
   * @return              The artifactId.
   */
  public static String getArtifactId(Document document) {
    return getElement(document.getDocumentElement(), ARTIFACT_ID)
      .map(e -> e.getTextContent())
      .orElseThrow(() -> new RuntimeException("Failed to read artifact id from maven project."));
  }


  /**
   * Read the version form the document.
   * @pairam document     The document.
   * @return              The version, if exists, the parent version otherwise.
   */
  public static String getVersion(Document document) {
    String version = getElement(document.getDocumentElement(), VERSION).map(e -> e.getTextContent()).orElse(null);
    return Strings.isNotNullOrEmpty(version) ? version : getParentVersion(document);
  }

  /**
   * Read the packaging form the document.
   * @pairam document     The document.
   * @return              The version, if exists, the parent version otherwise.
   */
  public static String getPackaging(Document document) {
    return getElement(document.getDocumentElement(), PACKAGING)
      .map(e -> e.getTextContent())
      .orElse(DEFAULT_PACKAGING);
  }

  /**
   * Read the parent version from the document.
   * @param document      The document.
   * @return              The parent version, if exists. Throws IllegalStateException otherwise.
   */
  private static String getParentVersion(Document document) {
    return getElement(document.getDocumentElement(), PARENT, VERSION)
      .map(e -> e.getTextContent())
      .orElseThrow(() -> new RuntimeException("Failed to read parent version from maven project."));
  }


  /**
   * Get the child {@link Element} that matches the specified name.
   * @param element   The element.
   * @param name      The name.
   * @return          An {@link Optional} element.
   */
  private static Optional<Element> getChildElement(Element element, String name) {
    NodeList list = element.getElementsByTagName(name);
    for (int i=0; i < list.getLength(); i++) {
      Node n = list.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE && n.getParentNode().equals(element)) {
        return Optional.of((Element)n);
      }
    }
    return Optional.empty();
  }


  /**
   * Get the child {@link Element} that matches the specified names.
   * This method will recurisively go through the names.
   * @param element   The element.
   * @param names     The name array.
   * @return          An {@link Optional} element.
   */
  private static Optional<Element> getElement(Element element, String... names) {
    if (names.length > 1) {
      String[] remaining = new String[names.length - 1];
      System.arraycopy(names, 1, remaining, 0, names.length - 1);
      String first = names[0];
      return getElement(getChildElement(element, first).orElseThrow(()-> new IllegalStateException("Could not read child element: " + first)), remaining);
    } else if (names.length == 1){
      return getChildElement(element, names[0]);
    }
    throw new IllegalStateException("Could not read element.");
  }
}
