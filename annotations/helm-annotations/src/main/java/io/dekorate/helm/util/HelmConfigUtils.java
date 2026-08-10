package io.dekorate.helm.util;

import static io.github.yamlpath.utils.StringUtils.EMPTY;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.utils.Strings;

public final class HelmConfigUtils {

  private static final String ROOTLESS_PROPERTY = "@.";

  private HelmConfigUtils() {

  }

  public static String deductProperty(HelmChartConfig helmConfig, String property) {
    if (property.startsWith(ROOTLESS_PROPERTY)) {
      return property.replaceFirst(Pattern.quote(ROOTLESS_PROPERTY), EMPTY);
    }

    if (!startWithDependencyPrefix(property, helmConfig.getDependencies())) {
      String prefix = helmConfig.getValuesRootAlias() + ".";
      if (!property.startsWith(prefix)) {
        property = prefix + property;
      }
    }

    return property;
  }

  private static boolean startWithDependencyPrefix(String property, io.dekorate.helm.config.HelmDependency[] dependencies) {
    if (dependencies == null || dependencies.length == 0) {
      return false;
    }

    String[] parts = property.split(Pattern.quote("."));
    if (parts.length <= 1) {
      return false;
    }

    String name = parts[0];
    return Stream.of(dependencies)
        .map(d -> Strings.defaultIfEmpty(d.getAlias(), d.getName()))
        .anyMatch(d -> Strings.equals(d, name));
  }
}
