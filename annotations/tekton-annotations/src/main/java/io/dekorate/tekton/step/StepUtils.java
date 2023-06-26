package io.dekorate.tekton.step;

public final class StepUtils {
  private static final String PARAMS_FORMAT = "$(params.%s)";

  private StepUtils() {

  }

  public static String param(String name) {
    return String.format(PARAMS_FORMAT, name);
  }
}
