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
package io.ap4k.testing.openshift;

import io.ap4k.Ap4kException;
import io.ap4k.kubernetes.annotation.Internal;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.openshift.api.model.Build;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.openshift.utils.OpenshiftUtils;
import io.ap4k.project.FileProjectFactory;
import io.ap4k.project.Project;
import io.ap4k.utils.Packaging;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Internal
public class SourceToImageExtension implements BeforeAllCallback, AfterAllCallback {

  private static final String MANIFEST_PATH = "META-INF/ap4k/openshift.yml";
  private static final String TARGET = "target";
  private final OpenShiftClient client = new DefaultOpenShiftClient();

  private final List<HasMetadata> created = new ArrayList<>();

  private Project project;

  @Override
  public void afterAll(ExtensionContext context) {
    System.out.println("Deleting test resources");
    created.stream().forEach(r -> {
        System.out.println("Deleting: " + r.getKind() + " name:" +r.getMetadata().getName()+ " status:"+ client.resource(r).cascading(true).delete());
      });
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    URL manifestUrl = SourceToImageExtension.class.getClassLoader().getResource(MANIFEST_PATH);
    if (manifestUrl != null)  {
      File manifestFile = toFile(manifestUrl);
      System.out.println("Apply test resources from:" + manifestFile.getAbsolutePath());
      this.project = FileProjectFactory.create(manifestFile);
      try (InputStream is = manifestUrl.openStream()) {
        created.addAll(Serialization.unmarshal(is, KubernetesList.class).getItems());
        List<HasMetadata> items = client.resourceList(created).createOrReplace();
        for (HasMetadata i : items) {
          System.out.println("Created: " + i.getKind() + " name:" +i.getMetadata().getName()+ ".");
        }
      }
      build();
      client.resourceList(created).waitUntilReady(5, TimeUnit.MINUTES);
    }
  }

  public void build() {
    Path path = project.getRoot().resolve(TARGET).resolve(project.getBuildInfo().getOutputFileName());
    File tar = Packaging.packageFile(path.toAbsolutePath().toString());
    OpenshiftUtils.waitForImageStreamTags(created, 2, TimeUnit.MINUTES);
    created.stream()
      .filter(i -> i instanceof BuildConfig)
      .map(i -> (BuildConfig)i)
      .forEach( bc -> binaryBuild(bc, tar) );
  }

  /**
   * Performs the binary build of the specified {@link BuildConfig} with the given binary input.
   * @param buildConfig The build config.
   * @param binaryFile  The binary file.
   */
  private void binaryBuild(BuildConfig buildConfig, File binaryFile) {
    System.out.println("Running binary build:"+buildConfig.getMetadata().getName()+ " for:" +binaryFile.getAbsolutePath());
    Build build = client.buildConfigs().withName(buildConfig.getMetadata().getName()).instantiateBinary().fromFile(binaryFile);
    try  (BufferedReader reader = new BufferedReader(client.builds().withName(build.getMetadata().getName()).getLogReader())) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        System.out.println(line);
      }
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  private static File toFile(URL url) {
    String path = url.getPath();
    if  (path.contains("!")) {
      path = path.substring(0, path.indexOf("!"));
    }
    if (path.startsWith("jar:")) {
      path = path.substring(4);
    }
    if (path.startsWith("file:")) {
      path = path.substring(5);
    }
    return new File(path);
  }
}
