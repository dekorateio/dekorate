package io.dekorate.helm.util;

import java.util.Set;
import java.util.regex.Pattern;

import io.github.yamlpath.YamlExpressionParser;

public final class YamlExpressionParserUtils {

  public static final String SEPARATOR_TOKEN = ":LINE_SEPARATOR:";
  public static final String SEPARATOR_QUOTES = ":DOUBLE_QUOTES";
  public static final String START_EXPRESSION_TOKEN = ":START:";
  public static final String END_EXPRESSION_TOKEN = ":END:";

  private YamlExpressionParserUtils() {

  }

  public static void set(YamlExpressionParser parser, String path, String expression) {
    parser.write(path, adaptExpression(expression));
  }

  public static Object read(YamlExpressionParser parser, String path) {
    Set<Object> found = parser.read(path);
    return found.stream().findFirst().orElse(null);
  }

  public static Object readAndSet(YamlExpressionParser parser, String path, String expression) {
    Set<Object> found = parser.readAndReplace(path, adaptExpression(expression));
    return found.stream().findFirst().orElse(null);
  }

  private static String adaptExpression(String expression) {
    return START_EXPRESSION_TOKEN +
        expression.replaceAll(Pattern.quote(System.lineSeparator()), SEPARATOR_TOKEN)
            .replaceAll(Pattern.quote("\""), SEPARATOR_QUOTES)
        + END_EXPRESSION_TOKEN;
  }
}
