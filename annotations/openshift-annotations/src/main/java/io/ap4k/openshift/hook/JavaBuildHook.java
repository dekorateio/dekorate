package io.ap4k.openshift.hook;

import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.builder.Visitor;
import io.ap4k.deps.kubernetes.api.model.HasMetadata;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.deps.kubernetes.api.model.ObjectReference;
import io.ap4k.deps.kubernetes.client.utils.ResourceCompare;
import io.ap4k.deps.openshift.api.model.Build;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.api.model.ImageStreamTag;
import io.ap4k.deps.openshift.api.model.SourceBuildStrategyFluent;
import io.ap4k.deps.openshift.client.DefaultOpenShiftClient;
import io.ap4k.deps.openshift.client.OpenShiftClient;
import io.ap4k.deps.openshift.client.dsl.internal.BuildOperationsImpl;
import io.ap4k.hook.ProjectHook;
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
    ResourceCompare.class
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


  private void waitForImageStreamTags() {
    final List<String> tags = new ArrayList<>();
    new KubernetesListBuilder()
      .withItems(items)
      .accept(new Visitor<SourceBuildStrategyFluent>() {
        @Override
        public void visit(SourceBuildStrategyFluent strategy) {
          ObjectReference from = strategy.buildFrom();
          if (from.getKind().equals("ImageStreamTag")) {
            tags.add(from.getName());
          }
        }
      }).build();

      boolean tagsMissing = true;
      while (tagsMissing && !Thread.interrupted()) {
        tagsMissing = false;
        for (String tag : tags) {
          ImageStreamTag t = client.imageStreamTags().withName(tag).get();
          if (t == null) {
            tagsMissing = true;
          }
        }

        if (tagsMissing) {
          try {
             Thread.sleep(1000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
  }

  /**
   * Deploy the generated resources.
   */
  private void deploy() {
    try (FileInputStream fis = new FileInputStream(manifest)) {
        List<HasMetadata> items = client.resourceList(Serialization.unmarshal(fis, KubernetesList.class)).createOrReplace();
        items.stream().forEach(i -> System.out.println("Applied: "+ i.getMetadata().getName()));
      } catch (FileNotFoundException e) {
        throw Ap4kException.launderThrowable(e);
      } catch (IOException e) {
        throw Ap4kException.launderThrowable(e);
      }

      waitForImageStreamTags();
  }
}
