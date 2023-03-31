package io.dekorate.helm.util;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.utils.Strings;

public final class ReadmeBuilder {

  private static final String H1 = "# ";
  private static final String H2 = "## ";
  private static final String TABLE_SEPARATOR = "|";
  private static final String TABLE_HEADER_LINE = " --- ";
  private static final String SPACE = " ";
  private static final String CODE_BLOCK = "```";
  private static final String TIP = "> **Tip**: ";

  private final StringBuilder sb = new StringBuilder();

  private ReadmeBuilder() {

  }

  private void writeHeader(String level, String line, Object... args) {
    writeLine(level + line, args);
    writeLine();
  }

  private void writeTableHeader(String... columnNames) {
    StringBuilder header = new StringBuilder();
    StringBuilder line = new StringBuilder();

    for (String columnName : columnNames) {
      header.append(TABLE_SEPARATOR).append(SPACE).append(columnName).append(SPACE);
      line.append(TABLE_SEPARATOR).append(SPACE).append(TABLE_HEADER_LINE).append(SPACE);
    }

    sb.append(header).append(TABLE_SEPARATOR).append(System.lineSeparator());
    sb.append(line).append(TABLE_SEPARATOR).append(System.lineSeparator());
  }

  private void writeTableRow(Object... values) {
    StringBuilder row = new StringBuilder();
    for (Object value : values) {
      String actualValue = SPACE;
      if (value != null) {
        actualValue = Strings.defaultIfEmpty(value.toString(), SPACE);
      }

      row.append(TABLE_SEPARATOR).append(SPACE).append(actualValue).append(SPACE);
    }

    sb.append(row).append(TABLE_SEPARATOR).append(System.lineSeparator());
  }

  private void writeLine(String line, Object... args) {
    if (Strings.isNotNullOrEmpty(line)) {
      sb.append(String.format(line, args));
    }

    writeLine();
  }

  private void writeLine() {
    sb.append(System.lineSeparator());
  }

  private void writeCodeBlock(String... lines) {
    writeLine(CODE_BLOCK);
    for (String line : lines) {
      writeLine(line);
    }

    writeLine(CODE_BLOCK);
  }

  private void writeTip(String message) {
    writeLine(TIP + message);
  }

  public static String build(HelmChartConfig helmConfig, Map<String, ValuesHolder.HelmValueHolder> values) {
    ReadmeBuilder builder = new ReadmeBuilder();
    // Title:
    // # {chart.name}
    builder.writeHeader(H1, helmConfig.getName());
    if (Strings.isNotNullOrEmpty(helmConfig.getDescription())) {
      builder.writeLine(helmConfig.getDescription());
    }

    // Configuration:
    builder.writeHeader(H2, "Configuration");
    builder.writeLine("The following table lists the configurable parameters and their default values.");
    builder.writeLine();
    builder.writeTableHeader("Parameter", "Description", "Default");
    SortedSet<String> keys = new TreeSet<>(values.keySet());
    for (String key : keys) {
      ValuesHolder.HelmValueHolder value = values.get(key);
      builder.writeTableRow(literal(key), value.configReference.getDescription(), value.value);
    }

    builder.writeLine();
    builder.writeLine("Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.");
    builder.writeLine("Alternatively, a YAML file that specifies the values for the above parameters can be provided while "
        + "installing the chart. For example,");

    builder.writeCodeBlock("$ helm install --name chart-name -f values.yaml .");
    builder.writeTip("You can use the default [values.yaml](values.yaml)");

    return builder.sb.toString();
  }

  private static String literal(Object value) {
    return "`" + value + "`";
  }
}
