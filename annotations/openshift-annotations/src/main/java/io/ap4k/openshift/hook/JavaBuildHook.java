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
package io.ap4k.openshift.hook;

import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.client.utils.ResourceCompare;
import io.ap4k.deps.okhttp3.Callback;
import io.ap4k.deps.openshift.api.model.Build;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.deps.openshift.client.dsl.internal.BuildOperationsImpl;
import io.ap4k.hook.ProjectHook;
import io.ap4k.openshift.utils.OpenshiftUtils;
import io.ap4k.project.Project;
import io.ap4k.utils.Packaging;
import io.ap4k.utils.Serialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JavaBuildHook extends ProjectHook {

  private static final String TARGET = "target";
  private static final String CLASSES = "classes";
  private static final String META_INF = "META-INF";
  private static final String AP4K = "ap4k";
  private static final String OPENSHIFT_YML = "openshift.yml";

  private final File manifest;
  private final OpenShiftClient client = new DefaultOpenShiftClient();
  private final List<HasMetadata> items = new ArrayList<>();

  private Class[] requirements = new Class[] {
    BuildOperationsImpl.class,
    ResourceCompare.class,
    Callback.class
  };

  public JavaBuildHook(Project project) {
    super(project);
    this.manifest = project.getRoot().resolve(TARGET).resolve(CLASSES).resolve(META_INF).resolve(AP4K).resolve(OPENSHIFT_YML).toFile();
  }

  public void init () {
    if (manifest.exists()) {
      try (FileInputStream fis = new FileInputStream(manifest)) {
        items.addAll(Serialization.unmarshal(fis, KubernetesList.class).getItems());
      } catch (IOException e) {
        Ap4kException.launderThrowable(e);
      }
    }
  }

  @Override
  public void warmup() {
    if (manifest.exists()) {
      File warmup = Packaging.packageFile(manifest.getAbsolutePath());
      deploy();
    }
  }

  @Override
  public void run() {
    deploy();
    File tar = Packaging.packageFile(project.getRoot().resolve(TARGET).resolve(project.getBuildInfo().getOutputFileName()).toAbsolutePath().toString());
    Build build = client.buildConfigs().withName(project.getBuildInfo().getName()).instantiateBinary().fromFile(tar);
    try  (BufferedReader reader = new BufferedReader(client.builds().withName(build.getMetadata().getName()).getLogReader())) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        System.out.println(line);
      }
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }


  /**
   * Deploy the generated resources.
   */
  private void deploy() {
    try (FileInputStream fis = new FileInputStream(manifest)) {
      List<HasMetadata> items = client.resourceList(Serialization.unmarshal(fis, KubernetesList.class)).createOrReplace();
      items.stream().forEach(i -> System.out.println("Applied: "+ i.getKind()+ " "+ i.getMetadata().getName()+"."));
    } catch (FileNotFoundException e) {
      throw Ap4kException.launderThrowable(e);
    } catch (IOException e) {
      throw Ap4kException.launderThrowable(e);
    }

    OpenshiftUtils.waitForImageStreamTags(items, 2, TimeUnit.MINUTES);
  }
}
