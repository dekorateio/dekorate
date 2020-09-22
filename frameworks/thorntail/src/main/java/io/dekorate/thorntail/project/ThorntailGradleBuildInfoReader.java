package io.dekorate.thorntail.project;

import java.nio.file.Path;

import io.dekorate.project.BuildInfo;
import io.dekorate.project.GradleInfoReader;

public class ThorntailGradleBuildInfoReader extends GradleInfoReader {
  private static final String THORNTAIL_JAR = "-thorntail.jar";

  @Override
  public int order() {
    // we only need to modify `order`, so that this class comes sooner than generic GradleInfoReader
    //
    // we don't have to modify `isApplicable`, because this class is only present when the Thorntail support
    // is explicitly requested by the user
    return super.order() - 10;
  }

  @Override
  public BuildInfo getInfo(Path root) {
    BuildInfo result = super.getInfo(root);
    String fileName = result.getOutputFile().getFileName().toString();
    String uberjar = fileName.replace("." + result.getPackaging(), THORNTAIL_JAR);
    return result.edit().withOutputFile(result.getOutputFile().getParent().resolve(uberjar)).build();
  }
}
