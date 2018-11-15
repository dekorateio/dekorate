/**
 * Copyright (C) 2018 Ioannis Canellos 
 *     
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
**/
package io.ap4k.project;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

public class MavenInfoReader implements BuildInfoReader<MavenInfo> {

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
    public MavenInfo getInfo(Path root) {
        return new MavenInfo(root, parse(root.resolve(POM_XML)));
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

}
