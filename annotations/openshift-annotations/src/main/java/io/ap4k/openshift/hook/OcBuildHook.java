package io.ap4k.openshift.hook;

import io.ap4k.hook.ProjectHook;
import io.ap4k.project.Project;

import java.io.File;

public class OcBuildHook extends ProjectHook {

  private final String name;

  public OcBuildHook(String name, Project project) {
    super(project);
    this.name = name;
  }

  public void init () {
    File yml = project.getRoot().resolve("target").resolve("classes").resolve("META-INF").resolve("ap4k").resolve("openshift.yml").toFile();
    if (yml.exists()) {
      // We don't want to delete the build configs as this will reset the build counter.
      // So our best bet is to just replace everything and if there is something missing, delete it.
      if (!exec("oc", "replace", "-f", yml.getAbsolutePath())) {
        exec("oc", "create", "-f", yml.getAbsolutePath());
      }
    }
  }

  @Override
  public void warmup() {

  }

  @Override
  public void run() {
      exec("oc", "start-build", name, "--from-dir=./target", "--follow");
  }
}
